package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.Course;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import it.polimi.tiw2025gioiamarra.utils.Database;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

@WebServlet(name = "coursesServlet", value = "/courses")
public class CoursesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {};
        if (!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Common, session, request, response))
            return;

        session.removeAttribute("examSession");
        session.removeAttribute("course");

        Connection con = Database.getConnection();
        if (con == null) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        ArrayList<Course> courses = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
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
                    throw new IllegalArgumentException("Unexpected value: " + session.getAttribute("type"));
            }

            //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA

            request.setAttribute("courses", courses);

            //CONFIGURAZIONE LAYOUT
            String pageTitle;
            if (session.getAttribute("type") == LoginType.Student) {
                pageTitle = "Corsi studente";
            } else {
                pageTitle = "Corsi docente";
            }

            request.setAttribute("type", session.getAttribute("type"));
            request.setAttribute("pageTitle", pageTitle);
            request.setAttribute("fragname", "courses");
            request.setAttribute("fragkind", "generalFragments");
            request.setAttribute("backLink", "#");

            ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());
        } catch (SQLException | IllegalArgumentException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
        }
        finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(con);
        }
    }
}