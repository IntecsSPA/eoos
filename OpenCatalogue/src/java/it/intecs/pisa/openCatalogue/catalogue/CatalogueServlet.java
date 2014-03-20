/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.catalogue;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.metadata.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.openSearch.OpenSearchHandler;
import it.intecs.pisa.openCatalogue.prefs.Prefs;
import it.intecs.pisa.openCatalogue.solr.ingester.BaseIngester;
import it.intecs.pisa.openCatalogue.solr.ingester.IngesterFactory;
import it.intecs.pisa.util.DOMUtil;
import it.intecs.pisa.util.IOUtil;
import it.intecs.pisa.util.json.JsonUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;

/**
 *
 * @author simone
 */
@WebServlet(name = "CatalogueServlet", urlPatterns = {"/openCatalogue"})
public class CatalogueServlet extends HttpServlet {

    protected static final String METHOD_OPEN_SEARCH = "opensearch";
    protected static final String METHOD_HARVEST = "harvest";
    protected static final String METHOD_CSW = "csw";
    protected static final String METHOD_GUI = "gui";    
    protected static final String RESOURCE_OEM = "oem";
    protected static final String RESOURCE_CSV = "csv";
    protected static final String RESOURCE_XML = "xml";
    protected static final String OS_ATOM = "atom";
    protected static final String OS_WKT = "wkt";
    protected static final String OS_KML = "kml";
    protected static final String OS_CZML = "czml";
    protected static final String OS_JSON = "json";
    protected static final String OS_PRODUCT = "eoproduct";    
    protected String rootDirStr;
    protected File rootDir;
    protected File workspaceDir;
    protected OpenSearchHandler osh=null;

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /*
             * TODO output your page here. You may use following sample code.
             */
            out.println("<html>");
            out.println("<head>");
            out.println("<title>openCatalogue</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Open Catalogue Servlet at " + request.getContextPath() + "</h1>");
            out.println("<p/>");
            out.println("Atom search samples");
            out.println("<p/>");
            String url = request.getRequestURL()+"/opensearch/atom/?q=startIndex=1&amp;count=10&amp;bbox=[20,40 90,180]&amp";
            out.println("<a href=\""+url+"\">" + url +"</a><br/>");
            url = request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=0&amp;count=0";            
            out.println("<a href=\""+url+"\">" + url +"</a><br/>");
            url = request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10";            
            out.println("<a href=\""+url+"\">" + url +"</a><br/>");
            url = request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;st=RADAR&amp;startdate=2003-01-28T13:10:00.077Z&amp;stopdate=2003-01-28T13:40:00.077Z";            
            out.println("<a href=\""+url+"\">" + url +"</a><br/>");
            url = request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=0&amp;count=10&amp;g=LINESTRING(11.93%2039.88%2C%2010%2050%2C20%2025)&amp";
            out.println("<a href=\""+url+"\">" + url +"</a><br/>");
            url = request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;lat=54.75152&amp;lon=-25&amp;radius=1&amp";
            out.println("<a href=\""+url+"\">" + url +"</a><br/>");
            url = request.getRequestURL()+"/opensearch/atom/?q=*ASAR*&amp;startIndex=0&amp;count=10&amp;lat=54.75152&amp;lon=-25&amp;radius=1&amp";            
            out.println("<a href=\""+url+"\">" + url +"</a><br/>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI;

        requestURI = request.getRequestURI();
        if (requestURI.contains(METHOD_OPEN_SEARCH)) {
            try {
                handleOpenSearch(request, response);
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.error(ex.getMessage());
                response.sendError(400, ex.getMessage());
            }
        } else if (requestURI.contains(METHOD_HARVEST)) {
            handleHarvest(request, response);
        } else if (requestURI.contains(METHOD_GUI)) {
            handleGuiRequest(request, response);
        } else if (requestURI.contains(METHOD_CSW)) {
            handleCSW(request, response);
        } else {
            processRequest(request, response);
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI;

        requestURI = request.getRequestURI();
        if (requestURI.contains(RESOURCE_OEM)) {
            handleUpdate(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
    
    
    /**
     * Handles the HTTP
     * <code>PUT</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI;

        try
        {
            requestURI = request.getRequestURI();
            
            StringTokenizer tokenizer=new StringTokenizer(requestURI,"/");
            tokenizer.nextToken();
            tokenizer.nextToken();

            String format=tokenizer.nextToken();
            if(tokenizer.hasMoreTokens())
            {
                format+="/"+tokenizer.nextToken();
            }

            BaseIngester ingester=IngesterFactory.fromInputType(format);

            AbstractFilesystem workspace = new FileFilesystem(ServletVars.workspace);
            
            ingester.setConfiguration(workspace);
            ingester.setRunsOnTomcat();
            ingester.setSolrURL(Prefs.getSolrUrl());
            
            File temp=IOUtil.getTemporaryFile();
            OutputStream os=new FileOutputStream(temp);
            IOUtil.copy(request.getInputStream(), os);
            os.flush();
            os.close();
            
            String query=request.getQueryString();
            HashMap<String,String> queryHeaders=null;
            
            if(query!=null)
            {
                queryHeaders=new HashMap<String,String>();
                Iterable<String> split = Splitter.on("&").trimResults().omitEmptyStrings().split(query);
                
                Iterator<String> iter=split.iterator();
                while(iter.hasNext())
                {
                    String item=iter.next();
                    String[] values=item.split("=");
                    queryHeaders.put(URLDecoder.decode(values[0],"UTF-8"), URLDecoder.decode(values[1],"UTF-8"));
                }
            }
            JsonObject resp = ingester.ingestData(new FileFilesystem(temp),queryHeaders);
            JsonUtil.writeJsonToStream(resp, response.getOutputStream());
        }
        catch(Exception e)
        {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
    }

    /**
     * Handles the HTTP
     * <code>DELETE</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI;

        requestURI = request.getRequestURI();
        if (requestURI.contains(RESOURCE_OEM)) {
            handleDelete(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }
    
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

@Override
    public void init() throws ServletException {
        rootDirStr = getServletContext().getRealPath("/");
        rootDir = new File(rootDirStr);
        ServletVars.appFolder=rootDir;
        super.init();
        Prefs.install(); 
        
        try {
            IngesterFactory.init(new FileFilesystem(ServletVars.workspace));
        } catch (Exception ex) {
           throw new ServletException("Could not init IngesterFactory");
        }
        
        try
        {
            AbstractFilesystem repo = new FileFilesystem(Prefs.getMetadataFolder()); 
            AbstractFilesystem config = new FileFilesystem(rootDirStr+"/WEB-INF/openSearch/"); 
            String solrEndPoint = Prefs.getSolrUrl();
            osh = new OpenSearchHandler(config,repo,solrEndPoint);
        }
        catch(Exception e)
        {
            throw new ServletException("Cannot initialize OpenSearchHandler");
        }
        Log.info("EOpenCatalogue started");
}    
    
    private void handleOpenSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.contains(OS_ATOM)) {
            osh.processAtomRequest(request, response);
        } else if (requestURI.contains(OS_KML)) {
            osh.processKmlRequest(request, response);
        } else if (requestURI.contains(OS_WKT)) {
            osh.processWktRequest(request, response);
        } else if (requestURI.contains(OS_CZML)) {
            osh.processCZMLRequest(request, response);
        } else if (requestURI.contains(OS_PRODUCT)) {
            osh.processProductRequest(request, response);
        } else if (requestURI.contains(OS_JSON)) {
            osh.processJsonRequest(request, response);
        } else {
            Document description = osh.handleDescription(request.getRequestURL().toString());
            response.setContentType("application/xml");
            response.getWriter().print(DOMUtil.getDocumentAsString(description));
        }
    }

    private void handleHarvest(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleGuiRequest(HttpServletRequest request, HttpServletResponse response) throws IOException  {
        
        JsonObject outputJson = new JsonObject();
        JsonArray rows = new JsonArray();
        
        JsonObject tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search with no specific query parameters. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10");
        rows.add(tmpJson);

        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search on a geographical area identified vua a Bounding Box. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;bbox=60,40,90,90");
        rows.add(tmpJson);
        
        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "More complex search on RADAR products over a specific time window. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;sensorType=OPTICAL&amp;startdate=2009-04-06T00:00:00.077Z&amp;stopdate=2009-04-06T23:00:00.077Z");
        rows.add(tmpJson);        

        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search on a geographical area identified via a circle. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;lat=54.75152&amp;lon=-25&amp;radius=50");
        rows.add(tmpJson);
/*
        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search on a geographical area identified via a circle. The results are returned in JSON format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/json/?q=*ASAR*&amp;startIndex=0&amp;count=10&amp;lat=54.75152&amp;lon=-25&amp;radius=50");
        rows.add(tmpJson);
*/
       outputJson.add("rows", rows);
        
        sendJsonBackToClient(outputJson, response);
    }
    
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void handleCSW(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void sendJsonBackToClient(JsonObject outputJson, HttpServletResponse response) throws IOException {
        String jsonStr;
        response.setContentType("application/json");
        jsonStr = JsonUtil.getJsonAsString(outputJson);
        response.getWriter().print(jsonStr);
    }


}
