package it.polimi.tiw2025gioiamarra.utils;

import it.polimi.tiw2025gioiamarra.items.PageType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThymeleafUtils {
    public static TemplateEngine getTemplateEngine(WebApplicationTemplateResolver templateResolver) {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    public static JakartaServletWebApplication getWebApplication(ServletContext servletContext) {
        return JakartaServletWebApplication.buildApplication(servletContext);
    }

    public static WebApplicationTemplateResolver getTemplateResolver(JakartaServletWebApplication webApplication) {
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    public static void process(String path, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws IOException {
        JakartaServletWebApplication webapp = getWebApplication(servletContext);
        WebApplicationTemplateResolver resolver = getTemplateResolver(webapp);
        TemplateEngine templateEngine = ThymeleafUtils.getTemplateEngine(resolver);
        WebContext ctx = new WebContext(webapp.buildExchange(request, response));
        templateEngine.process(path, ctx, response.getWriter());
    }

    public static String getRightPath(HttpServletRequest request) {
        String async = request.getHeader("X-Fragment-Request");
        return async != null && async.equals("true") ? "fragmentWrapper" : "generalLayout";
    }

    public static ArrayList<String> getGrades(Connection con) {
        PreparedStatement ps;
        ResultSet rs;

        try {
            //Get enum values to show in the select tag
            ps = con.prepareStatement("SELECT SUBSTRING(COLUMN_TYPE,5) " +
                    "FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA='tiw'" +
                    "  AND TABLE_NAME='esame'" +
                    "  AND COLUMN_NAME='voto'");
            rs = ps.executeQuery();
            if (rs.next()) {
                String values = rs.getString("SUBSTRING(COLUMN_TYPE,5)");
                values = values.replace("(", "");
                values = values.replace(")", "");
                values = values.replace("'", "");
                return new ArrayList<>(Arrays.asList(values.split(",")));
            }
            else{
                return new ArrayList<>();
            }
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public static boolean mandatoryParametersCheck(String[] mandatoryParameters, PageType pageType, HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (pageType) {
            case Common -> {
                if((session.getAttribute("teacher") == null && session.getAttribute("student") == null) || session.getAttribute("type") == null) {
                    response.sendRedirect("student-login");
                    return false;
                }
            }
            case Student -> {
                if(session.getAttribute("student") == null || session.getAttribute("type") == null) {
                    response.sendRedirect("student-login");
                    return false;
                }
            }
            case Teacher -> {
                if(session.getAttribute("teacher") == null || session.getAttribute("type") == null) {
                    response.sendRedirect("teacher-login");
                    return false;
                }
            }
        }

        for(String param : mandatoryParameters) {
            switch (param.toLowerCase()){
                case "course" -> {
                    if(!coursesParameterCheck(request, response)) return false;
                }
                case "session" -> {
                    if(!sessionParameterCheck(request, response)) return false;
                }
                case "student" -> {
                    if(!studentParameterCheck(request, response)) return false;
                }
                case "report" -> {
                    if(!reportParameterCheck(request, response)) return false;
                }
                default -> {}
            }
        }

        return true;
    }

    private static boolean coursesParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String courseParam = request.getParameter("course");

        if(courseParam == null || courseParam.isEmpty()) {
            response.sendRedirect("courses");
            return false;
        }
        return true;
    }

    private static boolean sessionParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String courseParam = request.getParameter("course");
        String sessionParam = request.getParameter("session");

        if((sessionParam == null || sessionParam.isEmpty()) && (courseParam != null && !courseParam.isEmpty())) {
            response.sendRedirect("exam-sessions?course=" + courseParam);
            return false;
        }
        return true;
    }

    private static boolean studentParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String courseParam = request.getParameter("course");
        String sessionParam = request.getParameter("session");
        String studentParam = request.getParameter("student");

        if((studentParam == null || studentParam.isEmpty()) && (sessionParam != null && !sessionParam.isEmpty()) && (courseParam != null && !courseParam.isEmpty())) {
            response.sendRedirect("enrolled-students?course=" + courseParam + "&session=" + sessionParam);
            return false;
        }
        return true;
    }

    private static boolean reportParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String reportParam = request.getParameter("report");
        String sessionParam = request.getParameter("session");

        if((reportParam == null || reportParam.isEmpty()) || (sessionParam == null || sessionParam.isEmpty())) {
            response.sendRedirect("report-list");
            return false;
        }
        return true;
    }

    public static void redirectToDBErrorPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("db-error");
    }
}
