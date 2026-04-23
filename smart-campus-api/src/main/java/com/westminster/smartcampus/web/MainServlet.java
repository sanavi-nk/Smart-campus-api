package com.westminster.smartcampus.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MainServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<html><body>");
            out.println("<h1>Smart Campus Running</h1>");
            out.println("<p>Web application deployed successfully.</p>");
            out.println("<p>API base URL: <a href='" + request.getContextPath() + "/api/v1/'>" + request.getContextPath() + "/api/v1/</a></p>");
            out.println("</body></html>");
        }
    }
}
