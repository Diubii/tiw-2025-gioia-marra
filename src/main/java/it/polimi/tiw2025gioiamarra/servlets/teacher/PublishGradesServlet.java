package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.GradingStatus;
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
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name="publishGradesServlet", value="/publish-grades")
public class PublishGradesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if(request.getParameter("course") == null || request.getParameter("session") == null || session.getAttribute("teacher") == null) {
            response.sendRedirect("courses");
            return;
        }

        Connection con = Database.getConnection();
        if(con == null) { return; }

        int courseID = Integer.parseInt(request.getParameter("course"));
        int examSessionID = Integer.parseInt(request.getParameter("session"));
        String sort = request.getParameter("sort");
        String order = request.getParameter("order");


        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("UPDATE esame SET statoValutazione='Published' WHERE statoValutazione='Inserted' AND idAppello="+examSessionID);
            ps.execute();
            response.sendRedirect("enrolled-students?course=" + courseID + "&session=" + examSessionID+"&sort="+sort+"&order="+order);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
