package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.ExamSession;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import it.polimi.tiw2025gioiamarra.utils.Utils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jboss.weld.exceptions.NullInstanceException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "examSessionsServlet", value = "/exam-sessions")
public class ExamSessionsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course"};
        if(!Utils.mandatoryParametersCheck(mandatoryParams, PageType.Common, session, request, response)) return;

        ArrayList<ExamSession> examSessions;
        String courseName;

        Integer courseID = null;
        try {
            courseID = Integer.parseInt(request.getParameter("course"));
        }
        catch (NumberFormatException e) {
            response.sendRedirect("courses");
            return;
        }


        try(DatabaseConnection con = new DatabaseConnection()) {

            courseName = con.getMySQLCoursesDAO().getCourseNameFromCourseID(courseID);
            if(courseName == null){
                throw new NullInstanceException("Course not found.");
            }

            switch ((LoginType) session.getAttribute("type")){
                case LoginType.Student -> {
                    int studentID = (int) session.getAttribute("student");

                    if(!con.getMySQLCoursesDAO().getStudentCoursesIDs(studentID).contains(courseID)){
                        response.sendRedirect("courses");
                        return;
                    }

                    examSessions = con.getMySQLExamSessionsDAO().getStudentExamSessions(courseID, studentID);
                }
                case LoginType.Teacher -> {
                    int teacherID = (int) session.getAttribute("teacher");

                    if(!con.getMySQLCoursesDAO().getTeacherCoursesIDs(teacherID).contains(courseID)){
                        response.sendRedirect("courses");
                        return;
                    }

                    examSessions = con.getMySQLExamSessionsDAO().getTeacherExamSessions(courseID, teacherID);
                }
                default -> examSessions = new ArrayList<>();
            }

            if(examSessions == null) {
                throw new NullInstanceException("Exam sessions not found.");
            }

            courseName = con.getMySQLCoursesDAO().getCourseNameFromCourseID(courseID);
        } catch (SQLException e) {
            Utils.redirectToDBErrorPage(response);
            return;
        }
        catch (NullInstanceException e) {
            response.sendRedirect("courses");
            return;
        }

        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        request.setAttribute("examSessions", examSessions);
        request.setAttribute("courseName", courseName);

        //CONFIGURAZIONE LAYOUT
        request.setAttribute("type", session.getAttribute("type"));
        request.setAttribute("pageTitle", "Date appelli d'esame");
        request.setAttribute("fragname", "examsessions");
        request.setAttribute("fragkind", "generalFragments");
        request.setAttribute("backLink", "/courses");

        ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());
    }
}
