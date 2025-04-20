package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.utils.Database;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

@WebServlet(name="gradeStudentServlet", value = "/grade-student")
public class GradeStudentServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //TODO: handle missing session
        if(request.getParameter("student") == null || request.getParameter("exam") == null) {
            response.sendRedirect("enrolled-students");
            return;
        }

        Connection con = Database.getConnection();
        Enrollment enrollment;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT matricola, cognome, nome, email, corsoLaurea, voto, statoValutazione, idAppello FROM esame NATURAL JOIN iscrizioneCorso NATURAL JOIN studente WHERE matricola = ? AND idAppello = ?");

            int studentNumber = Integer.parseInt(request.getParameter("student"));
            int examSession = Integer.parseInt(request.getParameter("exam"));
            ps.setInt(1, studentNumber);
            ps.setInt(2, examSession);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                String studentFullName = rs.getString("nome") + " " + rs.getString("cognome");
                String studentEmail = rs.getString("email");
                String degreeCourse = rs.getString("corsoLaurea");
                String grade = rs.getString("voto");
                String gradingStatus = rs.getString("statoValutazione");

                enrollment = new Enrollment(studentNumber, studentFullName, studentEmail, degreeCourse, grade, gradingStatus, null, examSession);
                request.setAttribute("enrollment", enrollment);

                //Get enum values to show in the select tag
                ps = con.prepareStatement("SELECT SUBSTRING(COLUMN_TYPE,5)\n" +
                        "FROM information_schema.COLUMNS\n" +
                        "WHERE TABLE_SCHEMA='tiw'" +
                        "  AND TABLE_NAME='esame'" +
                        "  AND COLUMN_NAME='voto'");
                rs = ps.executeQuery();
                if(rs.next()) {
                    String values = rs.getString("SUBSTRING(COLUMN_TYPE,5)");
                    values = values.replace("(", "");
                    values = values.replace(")", "");
                    values = values.replace("'", "");
                    System.out.println(Arrays.toString(values.split(",")));
                    ArrayList<String> grades = new ArrayList<>(Arrays.asList(values.split(",")));
                    request.setAttribute("grades", grades);
                } //TODO: handle else

                ThymeleafUtils.process("WEB-INF/templates/pure/teacher/gradestudent.html", request, response, getServletContext());
            }
            else{
                response.sendRedirect("enrolled-students");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
