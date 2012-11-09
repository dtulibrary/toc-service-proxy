package dk.dtu.dtic.mokuji;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.util.UriUtils;


/**
 * Servlet implementation class MokujiProxyServlet
 */
public class MokujiProxyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private static String mokujiRootUrl;
	private static String mokujiApiKey;
	private static String mokujiProxyUrl;
	private static HttpClient httpClient;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MokujiProxyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
    	super.init();
    	try {
    		Context ctx = (Context) new InitialContext().lookup("java:comp/env");
    		mokujiRootUrl = (String) ctx.lookup("dadscis.toc.serviceUrl");
    		mokujiApiKey    = (String) ctx.lookup("dadscis.toc.apiKey");
    		mokujiProxyUrl = (String) ctx.lookup("dadscis.toc.proxyUrl");
    		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
    		cm.setMaxTotal(100);
    		httpClient = new DefaultHttpClient(cm);//[configure HttpClient with PoolingClientConnectionManager]
    	} catch (NamingException e) {
			e.printStackTrace();
		}
    }

    public void destroy() {
        //[shutdown PoolingClientConnectionManager]
    	httpClient.getConnectionManager().shutdown();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
	    //String servletPath = request.getServletPath();   // /servlet/MyServlet
	    String pathInfo = request.getPathInfo();         // /a/b;c=123
	    String query = "";
	    String mokujiUrl = "";
	    try {
	    	query = "?proxy_url="+mokujiProxyUrl;
			System.out.println("QUERY: "+query);
		    mokujiUrl = mokujiRootUrl+"/"+mokujiApiKey+pathInfo+query;
		    mokujiUrl = UriUtils.encodeHttpUrl(mokujiUrl,"UTF-8");
		    System.out.println("Constructed url: "+mokujiUrl);
		} catch (UnsupportedEncodingException e1) {
			// Catch and do nothing.
		}
	    
      	HttpResponse mokujiResponse;
		try {
			mokujiResponse = httpClient.execute(new HttpGet(mokujiUrl));
	      	response.setContentLength((int)mokujiResponse.getEntity().getContentLength());
	      	response.setContentType(mokujiResponse.getEntity().getContentType().getValue());
	      	response.setStatus(mokujiResponse.getStatusLine().getStatusCode());
	      	IOUtils.copy(mokujiResponse.getEntity().getContent(), response.getOutputStream());
		} catch (ClientProtocolException e) {
			// Catch and do nothing.
		} catch (IOException e) {
			// Catch and do nothing.
		}
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	*/
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	} 
	
}
