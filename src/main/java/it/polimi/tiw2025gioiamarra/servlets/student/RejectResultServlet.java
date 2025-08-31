package it.polimi.tiw2025gioiamarra.servlets.student;

import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.Utils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="RejectResultServlet", value="/RejectResult")
public class RejectResultServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!Utils.mandatoryParametersCheck(mandatoryParams, PageType.Student, session, request, response)) return;

        int studentID = (int) session.getAttribute("student");

        Integer courseID = null;
        Integer examSessionID = null;

        try {
            courseID = Integer.parseInt(request.getParameter("course"));
            examSessionID = Integer.parseInt(request.getParameter("session"));
        }
        catch (NumberFormatException e) {
            if(courseID == null) response.sendRedirect("courses");
            else response.sendRedirect("exam-sessions?course=" + courseID);
            return;
        }


        try (DatabaseConnection con = new DatabaseConnection()) {
            con.getMySQLExamSessionsDAO().rejectGrade(studentID, examSessionID);
        } catch (SQLException e) {
            Utils.redirectToDBErrorPage(response);
            return;
        }

        response.sendRedirect("ExamResult?course=" + courseID + "&session=" + examSessionID + "&sort=matricola&order=asc");
    }
}