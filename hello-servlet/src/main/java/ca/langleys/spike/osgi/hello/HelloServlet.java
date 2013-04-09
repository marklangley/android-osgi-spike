package ca.langleys.spike.osgi.hello;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple servlet to demonstrate that the app is live and listening.
 */
@SuppressWarnings("serial")
public class HelloServlet extends HttpServlet {
	
	private static final Logger log = LoggerFactory.getLogger(HelloServlet.class);
	
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
	
	private static final String PREFIX = "<html><head><title>Test Page</title></head><body>";
	private static final String SUFFIX = "</body></html>";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		log.debug("doGet() triggered");
		String content = "<p>Hello World @ " + DATE_FORMAT.format(new Date()) + "</p>";
		content += "<table>";
		content += makeRow("getAuthType()", req.getAuthType());
		content += makeRow("getCharacterEncoding()", req.getCharacterEncoding());
		content += makeRow("getContentLength()", req.getContentLength());
		content += makeRow("getContentType()", req.getContentType());
		content += makeRow("getContextPath()", req.getContextPath());
		content += makeRow("getLocalAddr()", req.getLocalAddr());
		content += makeRow("getLocale()", req.getLocale().toString());
		content += makeRow("getLocalName()", req.getLocalName());
		content += makeRow("getLocalPort()", req.getLocalPort());
		content += makeRow("getMethod()", req.getMethod());
		content += makeRow("getPathInfo()", req.getPathInfo());
		content += makeRow("getPathTranslated()", req.getPathTranslated());
		content += makeRow("getProtocol()", req.getProtocol());
		content += makeRow("getQueryString()", req.getQueryString());
		content += makeRow("getRemoteAddr()", req.getRemoteAddr());
		content += makeRow("getRemoteHost()", req.getRemoteHost());
		content += makeRow("getRemotePort()", req.getRemotePort());
		content += makeRow("getRemoteUser()", req.getRemoteUser());
		content += makeRow("getRequestedSessionId()", req.getRequestedSessionId());
		content += makeRow("getRequestURI()", req.getRequestURI());
		content += makeRow("getRequestURL()", req.getRequestURL().toString());
		content += makeRow("getScheme()", req.getScheme());
		content += makeRow("getServerName()", req.getServerName());
		content += makeRow("getServerPort()", req.getServerPort());
		content += makeRow("getServletPath()", req.getServletPath());
		content += "</table>";
		content += "<img src=\"/hello/assets/nicubunu_Game_baddie_Geek.png\">";
	    resp.getWriter().write(PREFIX + content + SUFFIX);      
	} 
	
	private String makeRow(String label, String value) {
		return String.format("<tr><td>%s</td><td>%s</td></tr>", label, value);
	}

	private String makeRow(String label, int value) {
		return String.format("<tr><td>%s</td><td>%d</td></tr>", label, value);
	}
}
