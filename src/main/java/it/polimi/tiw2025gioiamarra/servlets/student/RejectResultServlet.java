package it.polimi.tiw2025gioiamarra.servlets.student;

import it.polimi.tiw2025gioiamarra.items.PageType;
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
import java.sql.SQLException;

@WebServlet(name="RejectResultServlet", value="/RejectResult")
public class RejectResultServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Student, session, request, response)) return;

        int studentID = (int) session.getAttribute("student");
        int courseID = Integer.parseInt(request.getParameter("course"));
        int examSessionID = Integer.parseInt(request.getParameter("session"));

        try (DatabaseConnection con = new DatabaseConnection()) {
            con.getMySQLExamSessionsDAO().rejectGrade(studentID, examSessionID);
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        response.sendRedirect("ExamResult?course=" + courseID + "&session=" + examSessionID + "&sort=matricola&order=asc");
    }
}