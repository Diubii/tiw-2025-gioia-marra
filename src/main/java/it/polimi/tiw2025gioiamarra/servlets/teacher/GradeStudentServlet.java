package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.items.Student;
import it.polimi.tiw2025gioiamarra.utils.Database;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@WebServlet(name="gradeStudentServlet", value = "/grade-student")
public class GradeStudentServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(request.getParameter("student") == null || request.getParameter("course") == null || request.getParameter("session") == null || session.getAttribute("teacher") == null) {
            response.sendRedirect("courses");
            return;
        }

        Connection con = Database.getConnection();
        if(con == null) { return; }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT matricola, cognome, studente.nome, email, corsoLaurea, voto, statoValutazione, idAppello FROM esame NATURAL JOIN iscrizioneCorso NATURAL JOIN studente JOIN corso on iscrizioneCorso.idCorso = corso.idCorso WHERE studente.matricola = ? AND corso.idCorso = ? AND idAppello = ? AND codiceDocente = ?");
            int studentNumber = Integer.parseInt(request.getParameter("student"));
            int courseID = Integer.parseInt(request.getParameter("course"));
            int examSessionID = Integer.parseInt(request.getParameter("session"));
            int teacherID = (Integer) session.getAttribute("teacher");
            ps.setInt(1, studentNumber);
            ps.setInt(2, courseID);
            ps.setInt(3, examSessionID);
            ps.setInt(4, teacherID);
            ResultSet rs = ps.executeQuery();
            Enrollment enrollment;
            if(rs.next()) {
                String studentName = rs.getString("nome");
                String studentSurname = rs.getString("cognome");
                String studentEmail = rs.getString("email");
                String degreeCourse = rs.getString("corsoLaurea");
                String grade = rs.getString("voto");
                GradingStatus gradingStatus = GradingStatus.valueOf(rs.getString("statoValutazione"));
                Student student = new Student(studentNumber, studentName, studentSurname, studentEmail, degreeCourse);
                enrollment = new Enrollment(student, grade, gradingStatus, null, courseID, examSessionID);
                request.setAttribute("enrollment", enrollment);

                session.setAttribute("examSession", examSessionID);

                //Get enum values to show in the select tag
                ps = con.prepareStatement("SELECT SUBSTRING(COLUMN_TYPE,5) " +
                        "FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA='tiw'" +
                        "  AND TABLE_NAME='esame'" +
                        "  AND COLUMN_NAME='voto'");
                rs = ps.executeQuery();
                if(rs.next()) {
                    String values = rs.getString("SUBSTRING(COLUMN_TYPE,5)");
                    values = values.replace("(", "");
                    values = values.replace(")", "");
                    values = values.replace("'", "");
                    ArrayList<String> grades = new ArrayList<>(Arrays.asList(values.split(",")));
                    request.setAttribute("grades", grades);
                } //TODO: handle else

                //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
                session.setAttribute("student", studentNumber);

                //CONFIGURAZIONE LAYOUT
                request.setAttribute("type", session.getAttribute("type"));
                request.setAttribute("pageTitle", "Valuta studente");
                request.setAttribute("fragname", "gradestudent");
                request.setAttribute("fragkind", "teacherFragments");
                request.setAttribute("backLink", "enrolled-students?course=" + courseID + "&session=" + examSessionID+"&sort=matricola&order=asc");
                ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());

            }
            else{
                response.sendRedirect("courses");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("type") == null ||session.getAttribute("teacher") == null) {
            response.sendRedirect("teacher-login");
            return;
        }

        Connection con = Database.getConnection();
        if(con == null) { return; }
        try {
            //Aggiorno il voto e lo stato
            PreparedStatement ps = con.prepareStatement("UPDATE esame SET voto=?, statoValutazione=? WHERE matricola=? AND idAppello=?"); //TODO: Rendere più sicura questa query, lo studente ha come docente lo stesso che vuole modificare il suo voto?
            String grade = request.getParameter("grade");
            int examSession = (Integer) session.getAttribute("examSession");
            ps.setString(1, grade);

            //Se il voto è "Vuoto" lo stato diventa "Non inserito", altrimenti "Inserito"
            if(!grade.equals("Vuoto")) ps.setString(2, GradingStatus.Inserted.name());
            else ps.setString(2, GradingStatus.NotInserted.name());

            ps.setInt(3, (Integer) session.getAttribute("student"));
            ps.setInt(4, examSession);
            ps.executeUpdate();

            //Ottengo l'id corso per il redirect
            ps = con.prepareStatement("SELECT idCorso FROM appello WHERE idAppello=?");
            ps.setInt(1, examSession);
            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                response.sendRedirect("enrolled-students?course=" + rs.getString("idCorso") + "&session=" + examSession+"&sort=matricola&order=asc");
            }
            else{
                response.sendRedirect("courses");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
