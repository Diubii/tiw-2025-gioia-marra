package it.polimi.tiw2025gioiamarra.utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            } //TODO: handle else
        } catch (SQLException e) {
            throw new RuntimeException(e); //TODO: Handle exception
        }

        return new ArrayList<>();
    }
}
