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
import java.util.ArrayList;

public class ThymeleafUtils {
    public static TemplateEngine getTemplateEngine(WebApplicationTemplateResolver templateResolver) {
        templateResolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        return templateEngine;
    }

    public static JakartaServletWebApplication getWebApplication(ServletContext servletContext) {
        return JakartaServletWebApplication.buildApplication(servletContext);
    }

    public static WebApplicationTemplateResolver getTemplateResolver(JakartaServletWebApplication webApplication) {
        return new WebApplicationTemplateResolver(webApplication);
    }

    public static void process(String path, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws IOException {
        JakartaServletWebApplication webapp = getWebApplication(servletContext);
        WebApplicationTemplateResolver resolver = getTemplateResolver(webapp);
        TemplateEngine templateEngine = ThymeleafUtils.getTemplateEngine(resolver);
        WebContext ctx = new WebContext(webapp.buildExchange(request, response));
        templateEngine.process(path, ctx, response.getWriter());
    }
}
