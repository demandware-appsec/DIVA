package com.demandware.vulnapp.challenge.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.xalan.xsltc.compiler.CompilerException;

import com.demandware.vulnapp.challenge.AbstractChallenge;
import com.demandware.vulnapp.challenge.ChallengePlan;
import com.demandware.vulnapp.challenge.impl.ChallengeFactory.ChallengeType;
import com.demandware.vulnapp.servlet.DIVAServletRequestWrapper;
import com.demandware.vulnapp.servlet.Dictionary;
import com.demandware.vulnapp.servlet.DivaApp;
import com.demandware.vulnapp.sessionmgmt.SessionStorage;
import com.demandware.vulnapp.util.SecureRandomUtil;

/**
 * Very difficult challenge. need to work backwards to generate 
 * valid java to inject into dynamic compiler to steal flag.
 *  
 * @author Chris Smith
 *
 */
public class DynamicCompilerChallenge extends AbstractChallenge {

	public static final String COMPILER_PARAM = "expression";

	private File classOutputFolder;
	private String challengeDirName = null;
	
	protected DynamicCompilerChallenge(String name) {
		super(name);
	}

	/**
	 * Listener allows us to parse and understand compiler errors 
	 * This info is sent to users
	 * 
	 * @author Chris Smith
	 *
	 */
	private class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		private StringBuilder sb = new StringBuilder();

		private boolean hasReport(){
			return this.sb.length() > 0;
		}

		public String getReport(){
			return this.sb.toString();
		}

		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append("Line Number->" + diagnostic.getLineNumber() + " \n");
			sb2.append("Code->" + diagnostic.getCode() + " \n");
			sb2.append("Message->" + diagnostic.getMessage(Locale.ENGLISH) + " \n");
			sb2.append("Source->" + diagnostic.getSource() + " \n");
			sb2.append(" \n\n");
			this.sb.append(sb2);
		}
	}

	/** 
	 * Java File Object represents an in-memory java source file <br>
	 * so there is no need to put the source file on hard disk (class is still
	 * written to disk) 
	 */
	private class InMemoryJavaFileObject extends SimpleJavaFileObject {
		private String contents = null;

		private InMemoryJavaFileObject(String className, String contents) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.contents = contents;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return contents;
		}
	}

	/**
	 * creates a randomly generated output folder location for classfile output
	 */
	private void setOutputLocation(String id){
		File basedir = new File(DivaApp.getInstance().getInformation(Dictionary.SERVLET_ROOT));
		if(this.challengeDirName == null){
			this.challengeDirName = ChallengePlan.getInstance().getChallengeForType(ChallengeType.COMPILER).getName();
		}
		try {
			this.classOutputFolder = Paths.get(	basedir.getCanonicalPath(), 
					"challenge8", 
					id, 
					SecureRandomUtil.generateRandomHexString(10)
					).toFile();
			this.classOutputFolder.mkdirs();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the fake java file for compilation and running
	 * 
	 * @param expression the input to be evaluated
	 * @param flagValue the flag value users are attempting to recover
	 * @return Java File containing valid java with flag value hidden and expression written
	 */
	private JavaFileObject getJavaFileObject(String expression, String flagValue) {
		
		String exp = "\"" + expression + "\"";
		StringBuilder contents = new StringBuilder();
		contents.append("package challenge;\n");
		contents.append("import javax.script.*;\n");
		contents.append("public class DynamicCompilerChallenge {\n");
		contents.append("	class FlagClass{\n");
		contents.append("		private String flag = \""+ flagValue +"\";\n");
		contents.append("		public String getFlag(){\n");
		contents.append("			return this.flag;\n");
		contents.append("		}\n");
		contents.append("	}\n");
		contents.append("	public Object execute() throws Exception{\n");
		contents.append("		String expr = " + exp + ";\n");
		contents.append("		ScriptEngineManager manager = new ScriptEngineManager();\n");
		contents.append("		ScriptEngine engine = manager.getEngineByName(\"js\");\n");
		contents.append("		Object ret = engine.eval(expr);\n");
		contents.append("		return ret;\n");
		contents.append("	}\n");
		contents.append("	public static void main(String[] args){\n");
		contents.append("		DynamicCompilerChallenge cf = new DynamicCompilerChallenge();\n");
		contents.append("		try{\n");
		contents.append("			cf.execute();\n");
		contents.append("		}catch(Exception e){\n");
		contents.append("			e.printStackTrace();\n");
		contents.append("		}\n");
		contents.append("	}\n");
		contents.append("}\n");

		return new InMemoryJavaFileObject("challenge.DynamicCompilerChallenge", contents.toString());
	}

	/** 
	 * compile your files with the JavaCompiler 
	 * @throws CompilerException if the file cannot be compiled
	 */
	private void compile(Iterable<? extends JavaFileObject> files) throws CompilerException {
		//get system compiler:
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		// for compilation diagnostic message processing on compilation WARNING/ERROR
		MyDiagnosticListener compilerListener = new MyDiagnosticListener();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(compilerListener, Locale.ENGLISH, null);

		//specify classes output folder
		List<String> options = Arrays.asList("-d", this.classOutputFolder.getAbsolutePath());
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, compilerListener, options, null, files);

		if (!task.call() || compilerListener.hasReport()) {
			throw new CompilerException("Compilation did not succeed: " + compilerListener.getReport());
		}
	}

	/**
	 * executes the compiled class via reflection
	 * 
	 * @return Object containing the answer to the input expression
	 * @throws MultipleExceptions. So many things can go wrong...
	 */
	private Object runIt() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, IOException {
		// Create a File object on the root of the directory
		// containing the class file
		File file = this.classOutputFolder.getAbsoluteFile();

		Object o = null;
		URL url = file.toURI().toURL(); // file:/classes/demo
		URL[] urls = new URL[] {url};

		try (URLClassLoader loader = new URLClassLoader(urls)){

			// Load in the class; Class.childclass should be located in
			// the directory file:/class/demo/
			Class<?> thisClass = loader.loadClass("challenge.DynamicCompilerChallenge");

			Class<?> params[] = {};
			Object paramsObj[] = {};
			Object instance = thisClass.newInstance();
			Method thisMethod = thisClass.getDeclaredMethod("execute", params);

			// run the method on the instance:
			o = thisMethod.invoke(instance, paramsObj);
		}

		return o;
	}

	public String handleChallengeRequest(DIVAServletRequestWrapper req) {
		String result = "";
		String expression = req.getParameter(COMPILER_PARAM);
		if(expression == null){
			expression = "null";
		}
		String id = ((SessionStorage)req.getInformation(Dictionary.SESSION_STORE_OBJ)).getToken();
		setOutputLocation(id);

		String flag = (String)req.getInformation(Dictionary.FLAG_VALUE);		
		try{
			//1.Construct an in-memory java source file from your dynamic code
			JavaFileObject file = getJavaFileObject(expression, flag);
			Iterable<? extends JavaFileObject> files = Arrays.asList(file);

			//2.Compile your files by JavaCompiler
			compile(files);

			//3.Load your class by URLClassLoader, then instantiate the instance, and call method by reflection
			Object o = runIt();
			if(o != null){
				result = o.toString();
			}
		} catch(Throwable e) {
			result = e.getMessage();
			this.classOutputFolder.delete();
		}

		return result;
	}

}
