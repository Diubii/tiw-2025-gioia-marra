package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.items.Student;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name="enrolledStudentsServlet", value="/enrolled-students")
public class EnrolledStudentsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;
        ArrayList<Enrollment> enrollments;

        //Handling order by selected column
        String sort = request.getParameter("sort");
        sort = sort == null ? "matricola" : sort;
//        Map<String, String> validSortFields = new HashMap<>();
//        validSortFields.put("matricola", "matricola");
//        validSortFields.put("cognome", "cognome");
//        validSortFields.put("email", "email");
//        validSortFields.put("corsoLaurea", "corsoLaurea");
//        validSortFields.put("voto", "voto");
//        validSortFields.put("statoValutazione", "statoValutazione");
//        validSortFields.put("idVerbale", "idVerbale");
//        String orderBy = validSortFields.getOrDefault(sort, "matricola"); // default

        String order = request.getParameter("order");
        String sortDirection = "ASC";
        if ("desc".equalsIgnoreCase(order)) {
            sortDirection = "DESC";
        }

        int courseID = Integer.parseInt(request.getParameter("course"));
        int sessionID = Integer.parseInt(request.getParameter("session"));
        int teacherID = (Integer) session.getAttribute( "teacher");

        ArrayList<String> grades;

        try(DatabaseConnection con = new DatabaseConnection()) {
            enrollments = con.getMySQLExamSessionsDAO().getEnrolledStudents(courseID, sessionID, teacherID, sortDirection, sort);
            grades = con.getGrades();
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
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