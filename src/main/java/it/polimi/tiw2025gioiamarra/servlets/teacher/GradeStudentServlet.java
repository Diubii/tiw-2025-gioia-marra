package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.items.Student;
import it.polimi.tiw2025gioiamarra.utils.Database;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

@WebServlet(name = "gradeStudentServlet", value = "/grade-student")
public class GradeStudentServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session", "student"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        Connection con = Database.getConnection();
        if (con == null) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

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
            if (rs.next()) {
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

                request.setAttribute("grades", ThymeleafUtils.getGrades(con));

                //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
                session.setAttribute("student", studentNumber);

                //CONFIGURAZIONE LAYOUT
                request.setAttribute("type", session.getAttribute("type"));
                request.setAttribute("pageTitle", "Valuta studente");
                request.setAttribute("fragname", "gradestudent");
                request.setAttribute("fragkind", "teacherFragments");
                request.setAttribute("backLink", "enrolled-students?course=" + courseID + "&session=" + examSessionID + "&sort=matricola&order=asc");
                ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());

            } else {
                response.sendRedirect("courses");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        if (session.getAttribute("type") == null || session.getAttribute("teacher") == null) {
            response.sendRedirect("teacher-login");
            return;
        }

        if(session.getAttribute("examSession") == null){
            response.sendRedirect("courses");
            return;
        }

        Connection con = Database.getConnection();

        if (con == null) {
            response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
            return;
        }

        try {
            //Aggiorno il voto e lo stato
            PreparedStatement ps = con.prepareStatement("UPDATE esame NATURAL JOIN appello NATURAL JOIN corso SET voto=?, statoValutazione=? WHERE matricola=? AND idAppello=? AND codiceDocente=?");
            String[] grades = request.getParameterValues("grades");
            String[] students = request.getParameterValues("studentNumbers");
            int examSession = (Integer) session.getAttribute("examSession");
            int teacher = (Integer) session.getAttribute("teacher");

            for (int i = 0; i < grades.length; i++) {
                if (grades[i] == null || grades[i].isBlank() || students[i] == null || students[i].isBlank()) continue;
                String grade = grades[i];
                int student = Integer.parseInt(students[i]);
                ps.setString(1, grade);

                //Se il voto è "Vuoto" lo stato diventa "Non inserito", altrimenti "Inserito"
                if (!grade.equals("Vuoto")) ps.setString(2, GradingStatus.Inserted.name());
                else ps.setString(2, GradingStatus.NotInserted.name());
                ps.setInt(3, student);
                ps.setInt(4, examSession);
                ps.setInt(5, teacher);
                ps.addBatch();
            }
            ps.executeBatch();

            //Ottengo l'id corso per il redirect
            ps = con.prepareStatement("SELECT idCorso FROM appello WHERE idAppello=?");
            ps.setInt(1, examSession);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                RequestDispatcher rd = request.getRequestDispatcher("enrolled-students?course=" + rs.getString("idCorso") + "&session=" + examSession + "&sort=matricola&order=asc");

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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
