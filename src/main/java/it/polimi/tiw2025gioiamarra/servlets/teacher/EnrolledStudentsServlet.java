package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
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

@WebServlet(name="enrolledStudentsServlet", value="/enrolled-students")
public class EnrolledStudentsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!Utils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;
        ArrayList<Enrollment> enrollments;

        //Handling order by selected column
        String sort = request.getParameter("sort");
        sort = sort == null ? "matricola" : sort;

        String order = request.getParameter("order");
        order = order == null ? "asc" : order;

        Integer courseID = null;
        Integer sessionID = null;

        try{
            courseID = Integer.parseInt(request.getParameter("course"));
            sessionID = Integer.parseInt(request.getParameter("session"));
        }
        catch (NumberFormatException e){
            if(courseID != null) response.sendRedirect("exam-sessions?course=" + courseID);
            else response.sendRedirect("courses");
            return;
        }

        int teacherID = (Integer) session.getAttribute( "teacher");
        ArrayList<String> grades;

        try(DatabaseConnection con = new DatabaseConnection()) {
            enrollments = con.getMySQLExamSessionsDAO().getEnrolledStudents(courseID, sessionID, teacherID, sort, order);
            grades = con.getGrades();
        } catch (SQLException e) {
            Utils.redirectToDBErrorPage(response);
            return;
        }

        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        request.setAttribute("enrollments", enrollments);
        request.setAttribute("GradingStatus", GradingStatus.class);
        request.setAttribute("emptyReport", request.getParameter("emptyReport"));
        request.setAttribute("grades", grades);
        session.setAttribute("examSession", sessionID);


        //CONFIGURAZIONE LAYOUT
        request.setAttribute("type", session.getAttribute("type"));
        request.setAttribute("pageTitle", "Studenti iscritti");
        request.setAttribute("fragname", "enrolledstudents");
        request.setAttribute("fragkind", "teacherFragments");
        request.setAttribute("backLink", "exam-sessions?course=" + courseID);
        ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());
    }
}