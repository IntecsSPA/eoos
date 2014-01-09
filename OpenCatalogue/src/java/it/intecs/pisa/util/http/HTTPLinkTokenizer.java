/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util.http;

/**
 *
 * @author massi
 */
public class HTTPLinkTokenizer {
    protected String port;
    protected String username = null;
    protected String password = null;
    protected String host = null;
    protected String schemaLessHost = null;
    protected String authLessHost = null;
    protected String path = null;

    public HTTPLinkTokenizer(String url) {
        schemaLessHost = url.substring(7);
        if (schemaLessHost.indexOf("@") == -1) {
            authLessHost = schemaLessHost;
        } else {
            authLessHost = schemaLessHost.substring(schemaLessHost.indexOf("@") + 1);
            username = schemaLessHost.substring(0, schemaLessHost.indexOf(":"));
            password = schemaLessHost.substring(schemaLessHost.indexOf(":") + 1, schemaLessHost.indexOf("@"));
        }

        if (authLessHost.indexOf(":") == -1) {
            path = authLessHost.substring(authLessHost.indexOf("/"));
            host = authLessHost.substring(0, authLessHost.indexOf("/"));
            port = "80";
        } else {
            path = authLessHost.substring(authLessHost.indexOf("/"));
            host = authLessHost.substring(0, authLessHost.indexOf(":"));
            port = authLessHost.substring(authLessHost.indexOf(":")+1, authLessHost.indexOf("/"));
        }
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getPath() {
        return path;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }
}
