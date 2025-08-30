package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.daos.interfaces.CoursesDAO;
import it.polimi.tiw2025gioiamarra.items.Course;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import it.polimi.tiw2025gioiamarra.utils.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "coursesServlet", value = "/courses")
public class CoursesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {};
        if (!Utils.mandatoryParametersCheck(mandatoryParams, PageType.Common, session, request, response))
            return;

        session.removeAttribute("examSession");
        session.removeAttribute("course");

        ArrayList<Course> courses;

        try (DatabaseConnection db = new DatabaseConnection()) {
            CoursesDAO coursesDAO = db.getMySQLCoursesDAO();
            switch (session.getAttribute("type")) {
                case LoginType.Student -> courses = coursesDAO.getStudentCourses((Integer) session.getAttribute("student"));
                case LoginType.Teacher -> courses = coursesDAO.getTeacherCourses((Integer) session.getAttribute("teacher"));

                default -> throw new IllegalArgumentException("Unexpected value: " + session.getAttribute("type"));
            }
        } catch (SQLException | IllegalArgumentException e) {
            Utils.redirectToDBErrorPage(response);
            return;
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
    }
}