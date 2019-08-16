import java.io.File

def mkdirs(path: List[String]) = // return true if path was created
    path.tail.foldLeft(new File(path.head)){(a,b) => a.mkdir; new File(a,b)}.mkdir

def deleteRecursively(file: File): Unit = {
  if (file.isDirectory) {
    file.listFiles.foreach(deleteRecursively)
  }
  if (file.exists && !file.delete) {
    throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }
}

def currentMethodName() : String = Thread.currentThread.getStackTrace()(2).getMethodName
lazy val source = cpg.method.name("_jspService").parameter 

def checkCommandInjectionChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // Sink is the exeucte methods 
  val sink = cpg.method.fullName("org.apache.commons.exec.DefaultExecutor.execute.*").parameter
  println(sink.reachableBy(source).flows.p) 
} 

def checkCookieChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // Sinks are comparison methods, the data flow is "broken" (discontinued)
  // from there on
  //
  // Sinks are very general but they have to pass methods 
  // with `Cookie` inside their name.
  //
  // Results are flow to the three comparison methods in isTruthy
  var sink = cpg.method.name("equalsIgnoreCase").methodReturn
  println(sink.reachableBy(source).flows.passes(".*Cookie.*").p)
  
  sink = cpg.method.name("valueOf").methodReturn
  println(sink.reachableBy(source).flows.passes(".*Cookie.*").p)
  
  sink = cpg.method.name("parseInt").methodReturn
  println(sink.reachableBy(source).flows.passes(".*Cookie.*").p)
}

def checkDebugChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // The comment of the challenge class is saying:
  // "No-op. Flag is in text of HTML"
  // 
  // This is the closest we can get to a flag to it,
  // if it is not in there we can't see it at the moment.
  // 
  // However, we potentially add html files to the graph.
  println(cpg.literal.code(".*(?i)FLAG.*").code.l.sorted.distinct)
}

def checkDynamicCompilerChallenge() = {
  // this challenge is solved with two queries 
  // 1. from _jspService to `call` 
  // (call: // https://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.CompilationTask.html#call())
  // 2. from `runIt` to `invoke`
 
  // The reason is that there is no direct data flow 
  // between these functions

  println(s"""=== running ${currentMethodName()} ===""")

  // 1. 
  var source = cpg.method.name("_jspService").parameter 
  var sink = cpg.method.name("call").parameter
  println(sink.reachableBy(source).flows.p)

  // 2.
  source = cpg.method.name("runIt").parameter
  sink = cpg.method.name("invoke").parameter
  println(sink.reachableBy(source).flows.p)


  // A more dynamic approach could be:
  // - find caller of `invoke`: cpg.method.name("invoke").caller.fullName.p
  // - caller of the caller:  cpg.method.name("invoke").caller.caller.fullName.p
  // - find caller of `call`: cpg.method.name("call").caller.fullName.p
  // - check if they are called by the same method
  // - compare the line numbers or query the AST (not handled here)
  
  /*
    ocular> cpg.method.name("call").caller.caller.fullName.p
    res69: List[String] = List("com.demandware.vulnapp.challenge.impl.DynamicCompilerChallenge.handleChallengeRequest:java.lang.String(com.demandware.vulnapp.servlet.DIVAServletRequestWrapper)")

    ocular> cpg.method.name("invoke").caller.caller.fullName.p
    res70: List[String] = List("com.demandware.vulnapp.challenge.impl.DynamicCompilerChallenge.handleChallengeRequest:java.lang.String(com.demandware.vulnapp.servlet.DIVAServletRequestWrapper)")
  */

  // traversing the callers can be done in some loop ("repeat().until()")
}

def checkECBOracleChallenge() = {  
  println(s"""=== running ${currentMethodName()} ===""")
  // We check:
  // 1. is AES/ECB/NoPadding is used in doFinal
  // 2. is user input used in doFinal

  // sink for both
  val sink = cpg.method.name("doFinal").parameter
  // 1. 
  var source = cpg.method
                  .name("<operator>.assignment")              // start from assignments and filtering method arguments
                  .filter(_.parameter                         
                           .argument
                           .code("\"AES/ECB/NoPadding\""))    // for AES/ECB/NoPadding
                  .parameter
 
  println(sink.reachableBy(source).flows.passes("getInstance").passes(".*encrypt.*").p)

  // 2.
  source = cpg.method.name("_jspService").parameter
  println(sink.reachableBy(source).flows.passes("getParameter").passes(".*encrypt.*").p)
}


def checkEntropyChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // The sink should match the request handler:
  // XXX.handleChall<engeRequest:java.lang.String(com.demandware.vulnapp.servlet.DIVAServletRequestWrapper)
  val sink = cpg.method.fullName(".*handleChallengeRequest.*DIVAServletRequestWrapper.*").methodReturn
  // We are finding a flow which is passing `generateToken`
  // also `getParameter`
  println(sink.reachableBy(source)
              .flows
              .passes(_.isCall.name("generateToken"))
              .passes(_.isCall.name("getParameter"))
              .p
         )
}

 
def checkHardCodePasswordChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  cpg.call
     .name("<operator>.assignment")                             // we look for assignments, like `String password = "mypassword"`
     .filter(_.argument.order(1).code(".*password"))            // filtering the left side for "password"
     .filter(_.argument.order(2).codeNot("\\$.*"))              // removing java internals ($r[1-9]) 
     .code                                                      // code value
     .l                                                         // to list
     .foreach(println(_))                                       // printing

  // result should be
  // com.demandware.vulnapp.challenge.impl.HardCodePasswordChallenge.password = "TheresNoWayAnyoneWillGuessThisPasswordEverLookIts66CharactersLong"
}

def checkHiddenChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  cpg.typeDecl
     .filter(_.baseTypeDecl.name("AbstractChallenge"))        // Interested in classes that extends AbstractChallenge
     .method        
     .name("handleChallengeRequest")                          // Filtering for the "handleChallengeRequest" method
     .l
     .foreach{x=>
        if(x.start.callOut.l.size < 1) {                      // Check if we have less then 1 callOut
         println(s""" found 0 call outs in the method "handleChallengeRequest" in the class ${x.start.definingTypeDecl.name.l.headOption.getOrElse("")}""")
       }
     }
}

def checkLogInjectionChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // sink is contains("ACCESS GRANTED")
  val sink = cpg.method
                .name("contains")
                .filter(_.parameter
                         .argument
                         .code("\"ACCESS GRANTED\"")
                       )
                .parameter
  // the flow should pass 
  // method that have "Log" in their name
  println(sink.reachableBy(source).flows.passes(".*Log.*").p)
}

def checkMD5Challenge() = {}
def checkNullStringChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  val sink = cpg.method.name("readFromFile").parameter
  println(sink.reachableBy(source).flows.passes("getParameter").p)
}

def checkRFIChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // sink is readFromFile 
  val sink = cpg.method.name("readFromFile").parameter
  println(sink.reachableBy(source).flows.p)

}

def checkRNGChallenge() = {}

def checkSQLIChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  val sink = cpg.method.fullName(".*sql.*execute.*").parameter
  println(sink.reachableBy(source).flows.p) 
}

def checkTimingChallenge() = {
  // Timing is difficult to detect in a static way
  // so it would make sense to come up with some kind of metric
  // for subcalls
  println(s"""=== running ${currentMethodName()} ===""")
  // prints subcalls sorted by linenumber
  // branches are not handled (todo)
  println(cpg.method.name("stringCompare")
             .map{m=> m.fullName +                                              // method name full signature
              "{\n" +                                                           // opening bracket
              m.start
               .callOut                                                         // all calls
               .l                                                               // to list             
               .groupBy(_.lineNumber.get)                                       // group by line number
               .map(c=> (c._1,c._2.maxBy(_.code.length).code))                  // get the call with the longest code (internals) 
               .toList                                                          // to list
               .sortBy(_._1)                                                    // sort by line number
               .map{ case(linenumber, code) => s"""\t${linenumber} ${code}"""}  // format
               .mkString("\n")+"\n}"                                            // list to string, every entry is \n separated and adding closing braket 
         } .p)
}

def checkUnfinishedChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // sink is the verification of the password 
  val sink = cpg.method.name("isDebugUser").parameter
  println(sink.reachableBy(source).flows.p)
}


def checkUserAgentChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // requires https://github.com/ShiftLeftSecurity/policy/pull/252

  // sink is comparison of strings 
  //  => userAgent.getBrowser().getBrowserType().equals(BrowserType.MOBILE_BROWSER);
  val sink = cpg.method.name("equals").parameter
  
  // flow should pass parseUserAgentString
  println(sink.reachableBy(source).flows.passes("parseUserAgentString").p)
}

def checkValidationChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // not possible at the moment
}
def checkXSSChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  val sink = cpg.method.name("submitCachedCallable").parameter
  println(sink.reachableBy(source).flows.p)
}

def checkXXEChallenge() = {
  println(s"""=== running ${currentMethodName()} ===""")
  // there are several `parse` methods so it makes sense to filter 
  // for fullNameExact here. A regex like "javax.xml.parsers.DocumentBuilder.parse.*"
  // is also possible
  val sink = cpg.method.fullNameExact("javax.xml.parsers.DocumentBuilder.parse:org.w3c.dom.Document(org.xml.sax.InputSource)").parameter
  println(sink.reachableBy(source).flows.p)
}

@main def exec(jarFile: String) : Boolean = {
  
  println("[+] Reset workspace ")
  workspace.reset

  println("[+] Creating CPG and SP for " + jarFile) 
  createCpgAndSp(jarFile)

  println("[+] Verify if CPG was created successfully") 
  if(!workspace.baseCpgExists(jarFile)) {
       println("Failed to create CPG for " + jarFile)
       return false
  }

  println("[+] Check if CPG is loaded")
  if(workspace.loadedCpgs.toList.size == 0) {
       println("Failed to load CPG for " + jarFile)
       return false
  } 

  println("[+] Check CommandInjectionChallenge")
  checkCommandInjectionChallenge
  println("[+] -------------------------------")

  println
  println

  println("[+] Check CookieChallenge")
  checkCookieChallenge
  println("[+] -------------------------------")
  
  println
  println
  
  println("[+] Check DebugChallenge")
  checkDebugChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check DynamicCompilerChallenge")
  checkDynamicCompilerChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check ECBOracleChallenge")
  checkECBOracleChallenge
  println("[+] -------------------------------")
  
  println
  println
  
  println("[+] Check EntropyChallenge")
  checkEntropyChallenge
  println("[+] -------------------------------")

  println
  println

  println("[+] Check HardCodePasswordChallenge")
  checkHardCodePasswordChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check HiddenChallenge")
  checkHiddenChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check LogInjectionChallenge")
  checkLogInjectionChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check MD5Challenge [TBD]")
  checkMD5Challenge // todo
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check NullStringChallenge")
  checkNullStringChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check RFIChallenge")
  checkRFIChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check RNGChallenge [TBD]")
  checkRNGChallenge // todo
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check SQLIChallenge")
  checkSQLIChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check TimingChallenge")
  checkTimingChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check UnfinishedChallenge")
  checkUnfinishedChallenge
  println("[+] -------------------------------")
  
  println
  println
  
  println("[+] Check UserAgentChallenge")
  checkUserAgentChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check ValidationChallenge [TBD]")
  checkValidationChallenge // todo
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check XSSChallenge")
  checkXSSChallenge
  println("[+] -------------------------------")

  println
  println
  
  println("[+] Check XXEChallenge")
  checkXXEChallenge
  println("[+] -------------------------------")

  println
  println
  
  return true
}
