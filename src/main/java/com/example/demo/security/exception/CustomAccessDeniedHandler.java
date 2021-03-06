package com.example.demo.security.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private String errorPage;

    public CustomAccessDeniedHandler() {
    }

    public CustomAccessDeniedHandler(String errorPage) {
        this.errorPage = errorPage;
    }

    public String getErrorPage() {
        return errorPage;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        boolean isAjax = isAjaxRequest(request);
        if (!isAjax) {
            request.setAttribute("isAjaxRequest", isAjax);
            request.setAttribute("message", accessDeniedException.getMessage());
            request.getRequestDispatcher(errorPage).forward(request, response);
        } else {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
            response.getWriter().write("   <div class=\"row-fluid\">");
            response.getWriter().write("  <div class=\"span12\">");
            response.getWriter().write("     <div class=\"widget-box\">");
            response.getWriter().write("   <div class=\"error_ex\">");
            response.getWriter().write(" <h1>403</h1>");
            response.getWriter().write("<h3>没有权限</h3>");
            response.getWriter().write("<p>请与管理员联系!</p>");
            response.getWriter().write("<a class=\"btn btn-warning btn-big\"  href=\"\">Back to Home</a> </div>");
            response.getWriter().write("</div>");
            response.getWriter().write("</div>");
            response.getWriter().write("</div>");
            response.getWriter().write("</div>");
            response.getWriter().close();
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        if (header != null && "XMLHttpRequest".equals(header))
            return true;
        else
            return false;
    }

}
