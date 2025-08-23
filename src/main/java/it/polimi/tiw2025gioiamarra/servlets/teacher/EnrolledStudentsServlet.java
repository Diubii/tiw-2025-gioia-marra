package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.*;
import it.polimi.tiw2025gioiamarra.utils.Database;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.context.Context;

@WebServlet(name="enrolledStudentsServlet", value="/enrolled-students")
public class EnrolledStudentsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        Connection con = Database.getConnection();
        if (con == null) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        ArrayList<Enrollment> enrollments = new ArrayList<>();

        //Handling order by selected column
        String sort = request.getParameter("sort");
        sort = sort == null ? "matricola" : sort;
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("matricola", "matricola");
        validSortFields.put("cognome", "cognome");
        validSortFields.put("email", "email");
        validSortFields.put("corsoLaurea", "corsoLaurea");
        validSortFields.put("voto", "voto");
        validSortFields.put("statoValutazione", "statoValutazione");
        validSortFields.put("idVerbale", "idVerbale");
        String orderBy = validSortFields.getOrDefault(sort, "matricola"); // default

        String order = request.getParameter("order");
        String sortDirection = "ASC";
        if ("desc".equalsIgnoreCase(order)) {
            sortDirection = "DESC";
        }

        int courseID = Integer.parseInt(request.getParameter("course"));
        int sessionID = Integer.parseInt(request.getParameter("session"));

        try {

            PreparedStatement ps = con.prepareStatement("SELECT matricola, cognome, nome, email, corsoLaurea, voto, statoValutazione, idVerbale, idAppello FROM esame NATURAL JOIN iscrizioneCorso NATURAL JOIN studente WHERE idCorso = ? && idAppello = ? ORDER BY ? ?");
            ps.setInt(1, courseID);
            ps.setInt(2, sessionID);
            ps.setString(3, sort);
            ps.setString(4, sortDirection);
            ResultSet rs = ps.executeQuery();

            if(!rs.next()) {
                response.sendRedirect("exam-sessions?course=" + courseID);
                return;
            }

            while (rs.next()) {
                int studentNumber = Integer.parseInt(rs.getString("matricola"));
                String studentName =  rs.getString("nome");
                String studentSurname = rs.getString("cognome");
                String studentEmail = rs.getString("email");
                String degreeCourse = rs.getString("corsoLaurea");
                String grade = rs.getString("voto");
                GradingStatus gradingStatus = GradingStatus.valueOf(rs.getString("statoValutazione"));
                String verbalID = rs.getString("idVerbale");
                Student student = new Student(studentNumber, studentName, studentSurname, studentEmail, degreeCourse);
                Enrollment enrollment = new Enrollment(student, grade, gradingStatus, verbalID, courseID, sessionID);
                //System.out.println(enrollment);
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
        }

        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        request.setAttribute("enrollments", enrollments);
        request.setAttribute("GradingStatus", GradingStatus.class);
        request.setAttribute("emptyReport", request.getParameter("emptyReport"));
        request.setAttribute("grades", ThymeleafUtils.getGrades(con));
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