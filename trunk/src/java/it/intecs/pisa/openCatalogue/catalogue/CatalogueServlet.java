/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.catalogue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.openCatalogue.filesystem.AbstractFilesystem;
import it.intecs.pisa.openCatalogue.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.filesystem.StreamFileSystem;
import it.intecs.pisa.openCatalogue.ingest.convert.csv.CSVIngester;
import it.intecs.pisa.openCatalogue.ingest.Ingester;
import it.intecs.pisa.openCatalogue.openSearch.OpenSearchHandler;
import it.intecs.pisa.openCatalogue.prefs.Prefs;
import it.intecs.pisa.util.DOMUtil;
import it.intecs.pisa.util.json.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
    protected static final String RESOURCE_METADATA = "metadata";
    protected static final String RESOURCE_METADATAS = "metadatas";
    protected static final String RESOURCE_CSV = "csv";
    protected static final String OS_ATOM = "atom";
    protected static final String OS_WKT = "wkt";
    protected static final String OS_CZML = "czml";
    protected static final String OS_JSON = "json";
    protected static final String OS_PRODUCT = "eoproduct";    
    protected String rootDirStr;
    protected File rootDir;
    protected File workspaceDir;

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
        if (requestURI.contains(RESOURCE_METADATA)) {
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

        requestURI = request.getRequestURI();
        if (requestURI.contains(RESOURCE_METADATA)) {
            handleIngest(request, response);
        }else if (requestURI.contains(RESOURCE_CSV)) {
            handleCSV(request, response);
        }
        else
        {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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
        if (requestURI.contains(RESOURCE_METADATA)) {
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
        workspaceDir = ServletVars.workspace;
        ServletVars.appFolder=rootDir;
        super.init();
        Prefs.install();  
        Log.info("EOpenCatalogue started");
}    
    
    private void handleOpenSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        String requestURI = request.getRequestURI();
        AbstractFilesystem repo = new FileFilesystem(Prefs.getMetadataFolder()); 
        AbstractFilesystem config = new FileFilesystem(rootDirStr+"/WEB-INF/openSearch/"); 
        String solrEndPoint = Prefs.getSolrUrl();
        OpenSearchHandler osh = new OpenSearchHandler(config,repo,solrEndPoint);
        if (requestURI.contains(OS_ATOM)) {
            osh.processAtomRequest(request, response);
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

    private void handleIngest(HttpServletRequest request, HttpServletResponse response) throws IOException  {
        InputStream in;
        JsonObject inputJson = null;
        String itemId = null;
        String errorReason = null;
        boolean success = false;
        try {
            AbstractFilesystem repo = new FileFilesystem(Prefs.getMetadataFolder()); 
            AbstractFilesystem config = new FileFilesystem(rootDirStr+"/WEB-INF/openSearch/"); 
            String solrEndPoint = Prefs.getSolrUrl();            
            Ingester ing = new Ingester(repo,config,solrEndPoint);
            ing.ingestData(request,response);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JsonObject outputJson;

        outputJson = new JsonObject();
        outputJson.addProperty("success", success);

        if (itemId != null) {
            outputJson.addProperty("id", itemId);
        }

        if (errorReason != null) {
            outputJson.addProperty("errorReason", errorReason);
        }

        sendJsonBackToClient(outputJson, response);
    }

    
    private void handleGuiRequest(HttpServletRequest request, HttpServletResponse response) throws IOException  {
        String solrEndPoint = Prefs.getSolrUrl();            
        JsonObject outputJson = new JsonObject();
        JsonArray rows = new JsonArray();
        
        JsonObject tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search with no specific query parameters. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10");
        rows.add(tmpJson);

        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search on a geographical area identified vua a Bounding Box. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;bbox=[20,40 90,180]");
        rows.add(tmpJson);
        
        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "More complex search on RADAR products over a specific time window. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;st=RADAR&amp;startdate=2003-01-28T13:10:00.077Z&amp;stopdate=2003-01-28T13:40:00.077Z");
        rows.add(tmpJson);        

        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search on a geographical area identified via a circle. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/atom/?q=*.*&amp;startIndex=1&amp;count=10&amp;lat=54.75152&amp;lon=-25&amp;radius=1&amp");
        rows.add(tmpJson);

        tmpJson = new JsonObject();
        tmpJson.addProperty("title", "Simple search on a geographical area identified via a circle. The results are returned in ATOM format.");
        tmpJson.addProperty("value", request.getRequestURL()+"/opensearch/json/?q=*ASAR*&amp;startIndex=0&amp;count=10&amp;lat=54.75152&amp;lon=-25&amp;radius=1&amp");
        rows.add(tmpJson);

        outputJson.add("rows", rows);
        
        sendJsonBackToClient(outputJson, response);
    }
    
    
    private void handleCSV(HttpServletRequest request, HttpServletResponse response) throws IOException  {
        InputStream in;
        String errorReason = null;
        boolean success = false;
        boolean isTomcatCall= true;

        InputStream stream = request.getInputStream();
        String requestURI = request.getRequestURI();
        
        
        AbstractFilesystem configuration = new FileFilesystem(new File (new File (this.workspaceDir,"config"),requestURI.substring(requestURI.indexOf("csv")+4)));
        AbstractFilesystem toBeIngested = new StreamFileSystem(request.getInputStream());
        String url = Prefs.getSolrUrl();
        try{
        CSVIngester harv = new CSVIngester(configuration, configuration, url, isTomcatCall);
        harv.harvestDataFromStream(toBeIngested);
        
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JsonObject outputJson;

        outputJson = new JsonObject();
        outputJson.addProperty("success", success);

        if (errorReason != null) {
            outputJson.addProperty("errorReason", errorReason);
        }

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
