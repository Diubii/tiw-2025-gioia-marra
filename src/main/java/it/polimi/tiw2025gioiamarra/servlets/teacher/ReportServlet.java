package it.polimi.tiw2025gioiamarra.servlets.teacher;


import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.Report;
import it.polimi.tiw2025gioiamarra.items.Student;
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

@WebServlet(name="ReportServlet", value="/report")
public class ReportServlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("type") == null || session.getAttribute("teacher") == null) {
            response.sendRedirect("teacher-login");
            return;
        }

        Connection con = Database.getConnection();
        int reportId = Integer.parseInt(request.getParameter("reportId"));
        int sessionID = Integer.parseInt(request.getParameter("sessionId"));
        int courseID;
        ArrayList<Enrollment> enrollments = new ArrayList<>();

        Report report=null;
        try {

            PreparedStatement ps = con.prepareStatement("SELECT idVerbale,verbale.dataOra AS dataVerbale, appello.dataOra AS dataAppello,nome,appello.idAppello AS idAppello ,corso.idCorso AS idCorso FROM corso JOIN appello ON corso.idCorso = appello.idCorso JOIN verbale ON verbale.idAppello = appello.idAppello WHERE idVerbale=? AND appello.idAppello=?");
            ps.setInt(1, reportId);
            ps.setInt(2, sessionID);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String courseName =  rs.getString("nome");
                courseID = rs.getInt("idCorso");
                report = new Report(reportId, rs.getDate("dataVerbale"), courseName, rs.getDate("dataAppello"),Integer.parseInt(rs.getString("idAppello")));

                ps = con.prepareStatement("SELECT matricola, cognome, nome, email, corsoLaurea, voto, statoValutazione, idVerbale, idAppello,idCorso FROM esame NATURAL JOIN iscrizioneCorso NATURAL JOIN studente WHERE idVerbale=? AND idCorso=? ORDER BY matricola");
                ps.setInt(1, reportId);
                ps.setInt(2, courseID);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int studentNumber = Integer.parseInt(rs.getString("matricola"));
                    String studentName = rs.getString("nome");
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
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        request.setAttribute("report", report);
        request.setAttribute("enrollments", enrollments);

        //CONFIGURAZIONE LAYOUT
        request.setAttribute("type", session.getAttribute("type"));
        request.setAttribute("pageTitle", "Verbale");
        request.setAttribute("fragname", "report");
        request.setAttribute("fragkind", "teacherFragments");
        request.setAttribute("backLink", "/report-list");
        ThymeleafUtils.process("generalLayout", request, response, getServletContext());


    }
}
