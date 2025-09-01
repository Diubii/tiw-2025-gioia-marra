package it.polimi.tiw2025gioiamarra.servlets.student;

import it.polimi.tiw2025gioiamarra.daos.interfaces.CoursesDAO;
import it.polimi.tiw2025gioiamarra.daos.interfaces.ExamSessionsDAO;
import it.polimi.tiw2025gioiamarra.items.Course;
import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.ExamSession;
import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import it.polimi.tiw2025gioiamarra.utils.Utils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "ExamResultServlet", value = "/ExamResult")
public class ExamResultServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if (!Utils.mandatoryParametersCheck(mandatoryParams, PageType.Student, session, request, response))
            return;

        Integer courseID = null;
        Integer examSessionID = null;

        try {
            courseID = Integer.parseInt(request.getParameter("course"));
            examSessionID = Integer.parseInt(request.getParameter("session"));
        } catch (NumberFormatException e) {
            if(courseID == null) response.sendRedirect("courses");
            else response.sendRedirect("exam-sessions?course=" + courseID);
            return;
        }

        ArrayList<Integer> appelliIscritto;

        int studentID = (Integer) session.getAttribute("student");
        Enrollment enrollment;
        Course course;
        Date dateTime;

        try (DatabaseConnection con = new DatabaseConnection()) { //TODO: Redirigere se parametri errati o studente non iscritto
            ExamSessionsDAO examSessionsDAO = con.getMySQLExamSessionsDAO();

            CoursesDAO coursesDAO = con.getMySQLCoursesDAO();
            course = con.getMySQLCoursesDAO().getCourse(courseID);
            if(course == null) {
                response.sendRedirect("courses");
                return;
            }

            ExamSession examSession = examSessionsDAO.getExamSession(examSessionID);
            enrollment = examSessionsDAO.getEnrollment(studentID, examSessionID);
            if (examSession == null || enrollment == null || !courseID.equals(coursesDAO.getCourseIDFromExamSessionID(examSessionID))) {
                response.sendRedirect("exam-sessions?course=" + courseID);
                return;
            }

            dateTime = examSessionsDAO.getExamSession(examSessionID).dateTime();
        } catch (SQLException e) {
            Utils.redirectToDBErrorPage(response);
            return;
        }

        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        request.setAttribute("enrollment", enrollment);
        request.setAttribute("course", course);
        request.setAttribute("dateTime", dateTime);
        session.setAttribute("examSession", examSessionID);
        //session.setAttribute("student", studentNumber);


        //CONFIGURAZIONE LAYOUT
        request.setAttribute("type", session.getAttribute("type"));
        request.setAttribute("pageTitle", "Risultato esame");
        request.setAttribute("fragname", "examresult");
        request.setAttribute("fragkind", "studentFragments");
        request.setAttribute("backLink", "exam-sessions?course=" + courseID);
        ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());
    }
}
