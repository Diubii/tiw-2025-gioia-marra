package it.polimi.tiw2025gioiamarra.servlets.student;

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

@WebServlet(name="ExamResultServerlet", value = "/ExamResult")
public class ExamResultServerlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        Connection con = Database.getConnection();
        ArrayList<Integer> appelliIscritto = new ArrayList<Integer>();

        try {
            PreparedStatement ps;
            ResultSet rs;

            //preparare tab exam di questo studente e metto enrolled solo se anche li
            int matricola = (Integer)session.getAttribute("student");
            ps = con.prepareStatement("SELECT idAppello FROM esame WHERE matricola = ?");
            ps.setInt(1, matricola);
            rs = ps.executeQuery();
            while (rs.next()) {
                appelliIscritto.add(rs.getInt("idAppello"));
            }


            ps = con.prepareStatement("SELECT matricola, cognome, studente.nome, email, corsoLaurea, voto, statoValutazione, esame.idAppello , corso.idCorso, corso.nome AS nomeCorso , dataOra  FROM esame NATURAL JOIN iscrizioneCorso NATURAL JOIN studente JOIN corso on iscrizioneCorso.idCorso = corso.idCorso JOIN appello ON appello.idAppello = esame.idAppello WHERE studente.matricola = ? AND corso.idCorso = ? AND esame.idAppello = ? ");
            int studentNumber = (int) session.getAttribute("student");
            int courseID = Integer.parseInt(request.getParameter("course"));
            int examSessionID = Integer.parseInt(request.getParameter("session"));
            ps.setInt(1, studentNumber);
            ps.setInt(2, courseID);
            ps.setInt(3, examSessionID);
            rs = ps.executeQuery();
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
                //Course
                Course course;
                course = new Course(rs.getInt("idCorso"), rs.getString("nomeCorso"));
                request.setAttribute("course", course);
                //ExamSession
                ExamSession examSession;
                examSession = new ExamSession(rs.getInt("idAppello"), course, rs.getDate("dataOra"),appelliIscritto.contains(rs.getInt("idAppello")) );
                request.setAttribute("examsession", examSession);



                //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
                session.setAttribute("examSession", examSessionID);
                session.setAttribute("student", studentNumber);


                //CONFIGURAZIONE LAYOUT
                request.setAttribute("type", session.getAttribute("type"));
                request.setAttribute("pageTitle", "Risultato esame");
                request.setAttribute("fragname", "examresult");
                request.setAttribute("fragkind", "studentFragments");
                request.setAttribute("backLink", "exam-sessions?course=" + courseID);
                ThymeleafUtils.process("generalLayout", request, response, getServletContext());

            }
            else{
                //caso non enrolled sarebbe questo
                response.sendRedirect("exam-sessions?course=" + courseID);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

    }

}
