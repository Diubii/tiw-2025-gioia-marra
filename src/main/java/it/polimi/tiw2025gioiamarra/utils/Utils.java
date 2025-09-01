package it.polimi.tiw2025gioiamarra.utils;

import it.polimi.tiw2025gioiamarra.items.PageType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class Utils {
    public static boolean mandatoryParametersCheck(String[] mandatoryParameters, PageType pageType, HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (pageType) {
            case Common -> {
                if ((session.getAttribute("teacher") == null && session.getAttribute("student") == null) || session.getAttribute("type") == null) {
                    response.sendRedirect("student-login");
                    return false;
                }
            }
            case Student -> {
                if (session.getAttribute("student") == null || session.getAttribute("type") == null) {
                    response.sendRedirect("student-login");
                    return false;
                }
            }
            case Teacher -> {
                if (session.getAttribute("teacher") == null || session.getAttribute("type") == null) {
                    response.sendRedirect("teacher-login");
                    return false;
                }
            }
        }

        for (String param : mandatoryParameters) {
            switch (param.toLowerCase()) {
                case "course" -> {
                    if (!coursesParameterCheck(request, response)) return false;
                }
                case "session" -> {
                    if (!sessionParameterCheck(request, response)) return false;
                }
                case "student" -> {
                    if (!studentParameterCheck(request, response)) return false;
                }
                case "report" -> {
                    if (!reportParameterCheck(request, response)) return false;
                }
                default -> {
                }
            }
        }

        return true;
    }

    private static boolean coursesParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String courseParam = request.getParameter("course");

        if (courseParam == null || courseParam.isEmpty()) {
            response.sendRedirect("courses");
            return false;
        }
        return true;
    }

    private static boolean sessionParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String courseParam = request.getParameter("course");
        String sessionParam = request.getParameter("session");

        if ((sessionParam == null || sessionParam.isEmpty()) && (courseParam != null && !courseParam.isEmpty())) {
            response.sendRedirect("exam-sessions?course=" + courseParam);
            return false;
        }
        return true;
    }

    private static boolean studentParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String courseParam = request.getParameter("course");
        String sessionParam = request.getParameter("session");
        String studentParam = request.getParameter("student");

        if ((studentParam == null || studentParam.isEmpty()) && (sessionParam != null && !sessionParam.isEmpty()) && (courseParam != null && !courseParam.isEmpty())) {
            response.sendRedirect("enrolled-students?course=" + courseParam + "&session=" + sessionParam);
            return false;
        }
        return true;
    }

    private static boolean reportParameterCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String reportParam = request.getParameter("report");
        String sessionParam = request.getParameter("session");

        if ((reportParam == null || reportParam.isEmpty()) || (sessionParam == null || sessionParam.isEmpty())) {
            response.sendRedirect("report-list");
            return false;
        }
        return true;
    }

    public static void redirectToDBErrorPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("db-error");
    }
}
