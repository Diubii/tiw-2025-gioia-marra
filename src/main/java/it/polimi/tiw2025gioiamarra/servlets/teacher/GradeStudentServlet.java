package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.daos.interfaces.ExamSessionsDAO;
import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.items.Student;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "gradeStudentServlet", value = "/grade-student")
public class GradeStudentServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session", "student"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        int studentNumber = Integer.parseInt(request.getParameter("student"));
        int courseID = Integer.parseInt(request.getParameter("course"));
        int examSessionID = Integer.parseInt(request.getParameter("session"));
        int teacherID = (Integer) session.getAttribute("teacher");

        Enrollment enrollment;
        ArrayList<String> grades;

        try (DatabaseConnection con = new DatabaseConnection()) {
            grades = con.getGrades();
            Student student = con.getMySQLStudentDAO().getStudentForGrading(studentNumber, courseID, examSessionID, teacherID);

            if(student != null){
                ExamSessionsDAO examSessionsDAO = con.getMySQLExamSessionsDAO();
                String grade = examSessionsDAO.getStudentGrade(studentNumber, examSessionID);
                GradingStatus gradingStatus = examSessionsDAO.getStudentGradingStatus(studentNumber, examSessionID);
                enrollment = new Enrollment(student, grade, gradingStatus, null, courseID, examSessionID);
            }
            else {
                response.sendRedirect("enrolled-students?course=" + courseID + "&session=" + examSessionID + "&sort=matricola&order=asc");
                return;
            }
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        request.setAttribute("enrollment", enrollment);
        session.setAttribute("examSession", examSessionID);
        request.setAttribute("grades", grades);

        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        session.setAttribute("student", studentNumber);

        //CONFIGURAZIONE LAYOUT
        request.setAttribute("type", session.getAttribute("type"));
        request.setAttribute("pageTitle", "Valuta studente");
        request.setAttribute("fragname", "gradestudent");
        request.setAttribute("fragkind", "teacherFragments");
        request.setAttribute("backLink", "enrolled-students?course=" + courseID + "&session=" + examSessionID + "&sort=matricola&order=asc");
        ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        if(session.getAttribute("examSession") == null){
            response.sendRedirect("courses");
            return;
        }

        String[] grades = request.getParameterValues("grades");
        String[] students = request.getParameterValues("studentNumbers");
        int examSession = (Integer) session.getAttribute("examSession");
        int teacher = (Integer) session.getAttribute("teacher");

        Integer courseID;

        try (DatabaseConnection con = new DatabaseConnection()){
            //Aggiorno il voto e lo stato
            con.getMySQLExamSessionsDAO().gradeStudents(grades, students, examSession, teacher);

            //Ottengo l'id corso per il redirect
            courseID = con.getMySQLCoursesDAO().getCourseIDFromExamSessionID(examSession);
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        if (courseID != null) {
            RequestDispatcher rd = request.getRequestDispatcher("enrolled-students?course=" + courseID + "&session=" + examSession + "&sort=matricola&order=asc");

            //Cambio da POST a GET mantenendo l'header X-Fragment-Request, bruttino but does the job
            //TODO: non urgente ma magari trovare un modo più carino
            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                @Override
                public String getMethod() {
                    return "GET";
                }
            };

            rd.forward(requestWrapper, response);
        } else {
            response.sendRedirect("courses");
        }
    }
}
