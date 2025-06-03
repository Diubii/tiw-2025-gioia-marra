package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
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

@WebServlet(name="enrolledStudentsServlet", value="/enrolled-students")
public class EnrolledStudentsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("type") == null) {
            response.sendRedirect("teacher-login");
            return;
        }

        Connection con = Database.getConnection();
        ArrayList<Enrollment> enrollments = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT matricola, cognome, nome, email, corsoLaurea, voto, statoValutazione, idVerbale, idAppello FROM esame NATURAL JOIN iscrizioneCorso NATURAL JOIN studente WHERE idCorso = ?");
            String courseID = request.getParameter("course");
            ps.setInt(1, Integer.parseInt(courseID));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int studentNumber = Integer.parseInt(rs.getString("matricola"));
                String studentFullName = rs.getString("cognome") + " " + rs.getString("nome");
                String studentEmail = rs.getString("email");
                String degreeCourse = rs.getString("corsoLaurea");
                String grade = rs.getString("voto");
                String gradingStatus = rs.getString("statoValutazione");
                String verbalID = rs.getString("idVerbale");
                int examSession = rs.getInt("idAppello");
                Enrollment enrollment = new Enrollment(studentNumber, studentFullName, studentEmail, degreeCourse, grade, gradingStatus, verbalID, examSession);
                enrollments.add(enrollment);
            }
        } catch (SQLException e) { //TODO: gestire eccezione
            throw new RuntimeException(e);
        }
        request.setAttribute("enrollments", enrollments);
        ThymeleafUtils.process("WEB-INF/templates/pure/teacher/enrolledstudents.html", request, response, getServletContext());
    }
}