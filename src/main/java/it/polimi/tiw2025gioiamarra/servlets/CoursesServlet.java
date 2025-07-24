package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.Course;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import it.polimi.tiw2025gioiamarra.utils.Database;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

@WebServlet(name = "coursesServlet", value = "/courses")
public class CoursesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("type") == null || (session.getAttribute("teacher") == null && session.getAttribute("student") == null)) {
            response.sendRedirect("student-login");
            return;
        }
        session.removeAttribute("course");

        Connection con = Database.getConnection();
        if (con != null) {
            ArrayList<Course> courses = new ArrayList<>();
            try {
                PreparedStatement ps;
                ResultSet rs;
                switch (session.getAttribute("type")) {
                    case LoginType.Student:
                        ps = con.prepareStatement("SELECT * FROM iscrizioneCorso WHERE matricola = ?");
                        ps.setInt(1, (Integer) session.getAttribute("studentNumber"));
                        rs = ps.executeQuery();
                        ArrayList<Integer> courseIDs = new ArrayList<>();
                        while (rs.next()) {
                            courseIDs.add(rs.getInt("idCorso"));
                        }
                        ps = con.prepareStatement("SELECT * FROM corso WHERE idCorso = ?");
                        for(Integer courseID : courseIDs) {
                            ps.setInt(1, courseID);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                String name = rs.getString("nome");
                                courses.add(new Course(courseID, name));
                            }
                        }
                        break;
                    case LoginType.Teacher:
                        ps = con.prepareStatement("SELECT * FROM corso WHERE codiceDocente = ?");
                        ps.setInt(1, (Integer) session.getAttribute("teacher"));
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            int courseID = rs.getInt("idCorso");
                            String name = rs.getString("nome");
                            courses.add(new Course(courseID, name));
                        }
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + session.getAttribute("type"));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                response.sendRedirect("student-login");
            }
            courses.sort(Comparator.comparing(Course::name).reversed()); //ORDER BY name DESC //TODO: Sortare in SQL
            request.setAttribute("type", session.getAttribute("type"));
            request.setAttribute("courses", courses);
            ThymeleafUtils.process("/WEB-INF/templates/pure/courses.html", request, response, getServletContext());
        }
    }
}