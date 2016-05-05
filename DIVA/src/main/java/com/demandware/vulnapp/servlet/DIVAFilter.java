package com.demandware.vulnapp.servlet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.demandware.vulnapp.sessionmgmt.SessionManager;
import com.demandware.vulnapp.util.exception.IllegalSessionException;

/**
 * Filter is much easier to implement than Servlet system for these
 * Only allows requests to challenges/ dirs and wraps request
 * 
 * @author Chris Smith
 *
 */
public class DIVAFilter implements Filter {
	private final DivaApp app;
	private Path challPath = null;
	
	public DIVAFilter() {
		this.app = DivaApp.getInstance();
	}

	@Override
	public void destroy() {
		this.app.teardown();
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		this.app.setup(fConfig);
		this.challPath = Paths.get("/" + DivaApp.getInstance().getInformation(Dictionary.CHALLENGES_ROOT));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse resp = (HttpServletResponse) response;
		DIVAServletRequestWrapper req = new DIVAServletRequestWrapper((HttpServletRequest) request, resp);
		try{
			//first make sure user is accessing an allowed resource
			if(!isGoodPath(req)){
				throw new IllegalSessionException("Requested illegal path");
			}
			
			//setup challenge information
			req.setupJSPChallengeData();
			SessionManager.setCookieInformation(req);
			
			//allow request to continue
			chain.doFilter(req, response);
		
			//add last minute changes, if needed
			finalizeResponse(resp);
		} catch(IllegalSessionException e){
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}
	
	/**
	 * adds a few headers for security
	 */
	private void finalizeResponse(HttpServletResponse resp) {
		resp.setHeader("Cache-Control","no-store, no-cache, must-revalidate"); //HTTP 1.1 controls 
		resp.setHeader("Pragma","no-cache"); //HTTP 1.0 controls
		resp.setDateHeader ("Expires", 0); //Prevents caching on proxy servers
		resp.setHeader("X-XSS-Protection", "0"); //remove XSS auditor
	}

	
	private boolean isGoodPath(DIVAServletRequestWrapper req) {
		boolean pathsOK = false;
		Path reqPath = Paths.get(req.getServletPath());
		if(reqPath.getNameCount() > 1){
			pathsOK = matchesChallPath(reqPath) || matchesStaticPath(reqPath);
		} else if (reqPath.getNameCount() == 1){
			pathsOK = true;
		}
		
		return pathsOK;
	}

	private boolean matchesChallPath(Path reqPath){
		return reqPath.startsWith(this.challPath);
	}
	
	private boolean matchesStaticPath(Path reqPath){
		Path cssPath = Paths.get("/css");
		Path jsPath = Paths.get("/js");
		Path fontPath = Paths.get("/fonts");
		Path imgPath = Paths.get("/img");
		
		return reqPath.startsWith(cssPath) || reqPath.startsWith(jsPath) || reqPath.startsWith(fontPath) || reqPath.startsWith(imgPath);
	}
	
}
