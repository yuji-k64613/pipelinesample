package com.sample;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.sql.DriverManager;
import java.net.URI;
import java.sql.SQLException;
import java.net.URISyntaxException;
import java.sql.Connection;

//
// http://localhost:8080/hello
//
public class Main {
    public static void main(String[] args) {
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.addServlet(new ServletHolder(new ExampleServlet()), "/hello");
        // setup Server and Handler List
        HandlerList handlerList = new HandlerList();
        //handlerList.addHandler(resourceHandler);
        handlerList.addHandler(servletHandler);

        final Server jettyServer = new Server();
        jettyServer.setHandler(handlerList);

        //final int PORT = 8080;
        String portStr = System.getenv("PORT");
        int PORT = (portStr != null)? Integer.parseInt(portStr) : 8080;
        final HttpConfiguration httpConfig = new HttpConfiguration();

		// hide server info on header
		httpConfig.setSendServerVersion(false);
		final HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpConfig);
		final ServerConnector httpConnector = new ServerConnector(jettyServer, httpConnFactory);
		httpConnector.setPort(PORT);
		jettyServer.setConnectors(new Connector[] { httpConnector });

		try {
			jettyServer.start();
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static class ExampleServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final String CONTENT_TYPE = "text/plane; charset=UTF-8";
            resp.setContentType(CONTENT_TYPE);
            final PrintWriter out = resp.getWriter();
            out.println("hello, world!");
            out.println("DEVEL v1.0");

            try {
                Connection con = getConnection();
                out.println(con.toString());
            }
            catch (Exception e){
                out.println(e.toString());
            }
            out.close();
        }
    }

    private static Connection getConnection()
        throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" 
            + dbUri.getHost() 
            + ':' + dbUri.getPort() 
            + dbUri.getPath() + "?sslmode=require";

        return DriverManager.getConnection(dbUrl, username, password);
}
}

