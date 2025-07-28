package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.Course;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import it.polimi.tiw2025gioiamarra.utils.Database;
import jakarta.servlet.ServletException;
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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
                        ps = con.prepareStatement("SELECT corso.idCorso,nome FROM iscrizioneCorso JOIN corso ON iscrizioneCorso.idCorso = corso.idCorso WHERE iscrizioneCorso.matricola = ? ORDER BY nome DESC");
                        ps.setInt(1, (Integer) session.getAttribute("student"));
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            int id = rs.getInt("idCorso");
                            String name = rs.getString("nome");
                            courses.add(new Course(id, name));
                        }
                        break;
                    case LoginType.Teacher:
                        ps = con.prepareStatement("SELECT * FROM corso WHERE codiceDocente = ? ORDER BY nome DESC");
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

            //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
            request.setAttribute("type", session.getAttribute("type"));
            request.setAttribute("courses", courses);

            //CONFIGURAZIONE LAYOUT
            String pageTitle;
            if(session.getAttribute("type") == LoginType.Student) {
                pageTitle = "Corsi studente";
            }
            else{
                pageTitle = "Corsi docente";
            }

            request.setAttribute("pageTitle", pageTitle);
            request.setAttribute("fragname", "courses");
            request.setAttribute("fragkind", "generalFragments");
            request.setAttribute("backLink", "#");
            ThymeleafUtils.process("generalLayout", request, response, getServletContext());

        }
    }
}