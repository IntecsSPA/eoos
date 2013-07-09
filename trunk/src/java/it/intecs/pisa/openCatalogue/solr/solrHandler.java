/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.solr;

import it.intecs.pisa.log.Log;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 *
 * @author simone
 */
public class solrHandler {

    String solrHost;

    public solrHandler(String solrEndPoint) {
        this.solrHost = solrEndPoint;
    }

    public SaxonDocument exchange(HttpServletRequest request) throws UnsupportedEncodingException, IOException, SaxonApiException, Exception {
        HttpClient client = new HttpClient();
        HttpMethod method;
        String urlStr = prepareUrl(request);
        Log.debug("The following search is goint to be executed:" + urlStr);
        // Create a method instance.
        method = new GetMethod(urlStr);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            Log.error("Method failed: " + method.getStatusLine());
        }
        
        SaxonDocument solrResponse = new SaxonDocument(method.getResponseBodyAsString());
        Log.debug(solrResponse.getXMLDocumentString());        
        return solrResponse;
    }

    /*
     * Esempio query data:
     *
     * 1) beginPosition:[2008-03-04T07:47:30.000Z TO 2009-09-04T07:47:30.000Z]
     * http://localhost/solr/collection1/select?q=*%3A*&fq=beginPosition%3A%5B2008-03-04T07%3A47%3A30.000Z+TO+2009-09-04T07%3A47%3A30.000Z%5D&wt=xml&indent=true
     * 2) beginPosition:[2007-04-04T07:47:30.000Z TO 2008-04-05T07:47:30.000Z]
     * http://localhost/solr/collection1/select?q=*%3A*&fq=beginPosition%3A%5B2007-04-04T07%3A47%3A30.000Z+TO+2008-04-05T07%3A47%3A30.000Z%5D&wt=xml&indent=true
     *
     * 3) endPosition:[2006-04-04T07:47:30.000Z TO 2008-04-05T07:47:30.000Z]
     * http://localhost/solr/collection1/select?q=*%3A*&fq=endPosition%3A%5B2006-04-04T07%3A47%3A30.000Z+TO+2008-04-05T07%3A47%3A30.000Z%5D&wt=xml&indent=true
     *
     * esempi query intersect
     *
     * 1) posList:"intersects(43 69 64 89)
     * http://localhost/solr/collection1/select?q=*%3A*&fq=posList%3A%22intersects(43+69+64+89)%22&wt=xml&indent=true
     *
     * 2) posList :[-90,-180 TO 15,45]
     * http://localhost/solr/collection1/select?q=*%3A*&fq=posList+%3A%5B-90%2C-180+TO+15%2C45%5D&wt=xml&indent=true
     *
     * 3) posList:"Intersects(POLYGON((78 7, 74 17, 52 98, 78 7)))"
     * http://localhost/solr/collection1/select?q=*%3A*&fq=posList%3A%22Intersects(POLYGON((78+7%2C+74+17%2C+52+98%2C+78+7)))%22&wt=xml&indent=true
     *
     *
     * opensearch
     *
     *
     * q={searchTerm} count={count} startIndex={startIndex?}
     * startPage={startPage?} language={language?}
     * inputEncoding={inputEncoding?} outputEncoding={outputEncoding?}
     * bbox={geo:box?} geom={geo:geometry?} id={geo:uid?} lat={geo:lat?}
     * lon={geo:lon?} radius={geo:radius?} rel={geo:relation?} loc={geo:name&}
     * startdate={time:start?} stopdate={time:end?} tp={cseop:timePosition?}
     * psn={cseop:platformShortName?} psi={cseop:platformSerialIdentifier?}
     * ot={cseop:orbitType?} isn={cseop:instrumentShortName?}
     * st={cseop:sensorType?} som={cseop:sensorOperationalMode?}
     * si={cseop:swathIdentifier?}
     */
    private String prepareUrl(HttpServletRequest request) throws UnsupportedEncodingException {
        Enumeration params = request.getParameterNames();
        String fq = "";
        String q = this.solrHost + "/select?q=*%3A*&wt=xml&indent=true";
        String name;
        String value;
        if (request.getParameter("q")!="*.*"){
            value = request.getParameter("q");
            q = this.solrHost + "/select?q=" + URLDecoder.decode(value, "ISO-8859-1") + "&wt=xml&indent=true";
        }
        String lat = null;
        String lon = null;
        String radius = null;

        while (params.hasMoreElements()) {
            name = (String) params.nextElement();
            value = request.getParameter(name);

            if (name.equals("count")) {
                q += "&rows=" + value;                
            } else if (name.equals("startPage")) {
            } else if (name.equals("startIndex")) {
                q += "&start=" + value;
            } else if (name.equals("uid")) {
            } else if (name.equals("bbox")) {
                Log.debug("BBOX "+value);
                fq += " AND posList:" + URLDecoder.decode(value, "ISO-8859-1");
            } else if (name.equals("g")) {
                fq += " AND posList :\"Intersects(" + (URLDecoder.decode(value, "ISO-8859-1")) + ")\"";
            } else if (name.equals("id")) {
                fq += " AND id:\"" + URLDecoder.decode(value, "ISO-8859-1")+ "\"";
            } else if (name.equals("lat")) {
                lat = URLDecoder.decode(value, "ISO-8859-1");
            } else if (name.equals("lon")) {
                lon = URLDecoder.decode(value, "ISO-8859-1");
            } else if (name.equals("radius")) {
                radius = URLDecoder.decode(value, "ISO-8859-1");
            } else if (name.equals("startdate")) {
                fq += " AND beginPosition:[" + URLDecoder.decode(value, "ISO-8859-1") + " TO *]";
            } else if (name.equals("stopdate")) {
                fq += " AND endPosition:[* TO " + URLDecoder.decode(value, "ISO-8859-1") + "]";
            } else if (name.equals("tp")) {
            } else if (name.equals("psn")) {
                fq += " AND platformShortName=" + value;
            } else if (name.equals("psi")) {
                fq += " AND platformSerialIdentifier=" + value;
            } else if (name.equals("ot")) {
                fq += " AND orbitType=" + value;
            } else if (name.equals("isn")) {
                fq += " AND instrumentShortName=" + value;
            } else if (name.equals("st")) {
                fq += " AND sensorType=" + value;
            } else if (name.equals("som")) {
                fq += " AND sensorOperationalMode=" + value;
            } else if (name.equals("si")) {
                fq += " AND swathIdentifier=" + value;
            } else {
            }
        }
              
        
        if ((lat!=null)&&(lon!=null)&&(radius!=null))
                fq += " AND posList :\"Intersects(Circle(" + lat + " "+ lon+ " "+ radius+"))\""; 
        
        String url = q;
        if (fq.length() > 1)
                url += "&fq="+URLEncoder.encode(fq.substring(5), "ISO-8859-1");        
        return url;
    }

    public int postDocument(String body) throws IOException, SaxonApiException, Exception {
        HttpClient client = new HttpClient();
        HttpMethod method;
        String urlStr = solrHost + "update?commit=true";
        Log.debug("Ingesting a new document to: " + urlStr);        
        Log.debug(body);        
        method = new PostMethod(urlStr);
        RequestEntity entity = new StringRequestEntity(body);
        ((PostMethod) method).setRequestEntity(entity);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setRequestHeader("Content-Type", "text/xml");
        method.setRequestHeader("charset", "utf-8");
        
        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            Log.error("Method failed: " + method.getStatusLine());
        }

        SaxonDocument solrResponse = new SaxonDocument(method.getResponseBodyAsStream());
        Log.debug(solrResponse.getXMLDocumentString());        
        return statusCode;
    }
}
