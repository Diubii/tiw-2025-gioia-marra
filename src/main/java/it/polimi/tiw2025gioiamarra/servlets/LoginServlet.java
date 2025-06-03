package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.utils.Database;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

@WebServlet(name = "loginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection con = Database.getConnection();
        if (con == null) {
            System.err.println("Couldn't establish a connection to the database.");
            return;
        }

        try {
            PreparedStatement ps;
            switch (request.getAttribute("type")) {
                case LoginType.Student:
                    ps = con.prepareStatement("SELECT * FROM studente where matricola=? and pass=?");
                    ps.setInt(1, Integer.parseInt(request.getParameter("studentNumber")));
                    break;
                case LoginType.Teacher:
                    ps = con.prepareStatement("SELECT * FROM docente where codiceDocente=? and pass=?");
                    ps.setInt(1, Integer.parseInt(request.getParameter("teacherID")));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + request.getAttribute("type"));
            }

            ps.setString(2, request.getParameter("password"));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HttpSession session = request.getSession();
                switch (request.getAttribute("type")) {
                    case LoginType.Student:
                        session.setAttribute("type", LoginType.Student);
                        session.setAttribute("studentNumber", rs.getInt("matricola"));
                        break;
                    case LoginType.Teacher:
                        session.setAttribute("type", LoginType.Teacher);
                        session.setAttribute("teacher", rs.getInt("codiceDocente"));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + request.getAttribute("type"));
                }
                response.sendRedirect("courses");
            }
            //TODO: Gestire caso login errato
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
