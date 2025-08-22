package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.utils.Database;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet(name="RejectResultServerlet", value="/RejectResult")
public class RejectResultServerlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(request.getParameter("course") == null || request.getParameter("sessionID") == null || session.getAttribute("student") == null) {
            response.sendRedirect("courses");
            return;
        }

        Connection con = Database.getConnection();
        if(con == null) { return; }

        int courseID = Integer.parseInt(request.getParameter("course"));
        int examSessionID = Integer.parseInt(request.getParameter("sessionID"));

        try {
            PreparedStatement ps = con.prepareStatement("UPDATE esame SET statoValutazione = 'Rejected' WHERE esame.matricola = ? AND esame.idAppello = "+examSessionID);
            ps.setInt(1, (Integer) session.getAttribute("student"));
            ps.execute();
            response.sendRedirect("ExamResult?course=" + courseID + "&session=" + examSessionID+"&sort=matricola&order=asc");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}