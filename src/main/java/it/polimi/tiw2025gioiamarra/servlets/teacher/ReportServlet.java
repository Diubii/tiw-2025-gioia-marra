package it.polimi.tiw2025gioiamarra.servlets.teacher;


import it.polimi.tiw2025gioiamarra.daos.interfaces.ReportsDAO;
import it.polimi.tiw2025gioiamarra.items.*;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name="ReportServlet", value="/report")
public class ReportServlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"session", "report"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        int reportID = Integer.parseInt(request.getParameter("report"));
        int sessionID = Integer.parseInt(request.getParameter("session"));
        int teacherID = (Integer) session.getAttribute("teacher");
        //int courseID;

        Report report;
        ArrayList<Enrollment> enrollments;

        try (DatabaseConnection con = new DatabaseConnection()) {
            ReportsDAO reportsDAO = con.getMySQLReportsDAO();
            report = reportsDAO.getReport(reportID, sessionID, teacherID);
            if(report == null) {
                response.sendRedirect("report-list");
                return;
            }

            enrollments = reportsDAO.getEnrollmentsInReport(reportID);
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        request.setAttribute("report", report);
        request.setAttribute("enrollments", enrollments);

        //CONFIGURAZIONE LAYOUT
        request.setAttribute("type", session.getAttribute("type"));
        request.setAttribute("pageTitle", "Verbale");
        request.setAttribute("fragname", "report");
        request.setAttribute("fragkind", "teacherFragments");
        request.setAttribute("backLink", "report-list");
        ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());
    }
}
