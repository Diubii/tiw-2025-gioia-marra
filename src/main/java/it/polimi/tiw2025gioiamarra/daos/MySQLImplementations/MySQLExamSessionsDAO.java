package it.polimi.tiw2025gioiamarra.daos.MySQLImplementations;

import it.polimi.tiw2025gioiamarra.daos.interfaces.ExamSessionsDAO;
import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.ExamSession;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class MySQLExamSessionsDAO implements ExamSessionsDAO {
    private final Connection connection;

    public MySQLExamSessionsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public ArrayList<Enrollment> getEnrolledStudents(int courseID, int examSessionID, int teacherID, String sort, String sortDirection) throws SQLException {
        ArrayList<Enrollment> enrollments = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("SELECT matricola, cognome, studente.nome, email, corsoLaurea, voto, statoValutazione, idVerbale, idAppello FROM esame NATURAL JOIN iscrizioneCorso NATURAL JOIN studente JOIN corso ON iscrizioneCorso.idCorso = corso.idCorso WHERE corso.idCorso = ? AND idAppello = ? AND codiceDocente = ? ORDER BY ? ?")) {
            ps.setInt(1, courseID);
            ps.setInt(2, examSessionID);
            ps.setInt(3, teacherID);
            ps.setString(4, sort);
            ps.setString(5, sortDirection);

            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    int studentNumber = Integer.parseInt(rs.getString("matricola"));
                    String studentName = rs.getString("nome");
                    String studentSurname = rs.getString("cognome");
                    String studentEmail = rs.getString("email");
                    String degreeCourse = rs.getString("corsoLaurea");
                    String grade = rs.getString("voto");
                    GradingStatus gradingStatus = GradingStatus.valueOf(rs.getString("statoValutazione"));
                    String verbalID = rs.getString("idVerbale");
                    Student student = new Student(studentNumber, studentName, studentSurname, studentEmail, degreeCourse);
                    Enrollment enrollment = new Enrollment(student, grade, gradingStatus, verbalID, courseID, examSessionID);
                    //System.out.println(enrollment);
                    enrollments.add(enrollment);
                }
            }
        }
        return enrollments;
    }

    @Override
    public ArrayList<Integer> getStudentEnrollmentsIDs(int studentID) throws SQLException {
        ArrayList<Integer> enrollments = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("SELECT idAppello FROM esame WHERE matricola = ?")) {
            ps.setInt(1, studentID);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(rs.getInt("idAppello"));
                }
            }
        }
        return enrollments;
    }

    @Override
    public ArrayList<ExamSession> getTeacherExamSessions(int courseID) throws SQLException {
        ArrayList<ExamSession> examSessions = new ArrayList<>();

        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM appello WHERE idCorso = ? ORDER BY dataOra DESC")){
            ps.setInt(1, courseID);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamSession examSession = new ExamSession(rs.getInt("idAppello"), courseID, rs.getDate("dataOra"), true);
                    examSessions.add(examSession);
                }
            }
        }
        return examSessions;
    }

    @Override
    public ArrayList<ExamSession> getStudentExamSessions(int courseID, int studentID) throws SQLException {
        ArrayList<ExamSession> examSessions = new ArrayList<>();

        ArrayList<Integer> enrollmentsIDs = getStudentEnrollmentsIDs(studentID);

        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM appello WHERE idCorso = ? ORDER BY dataOra DESC")){
            ps.setInt(1, courseID);
            try(ResultSet rs = ps.executeQuery()) {
                boolean enrolled;
                while (rs.next()) {
                    enrolled = enrollmentsIDs.contains(rs.getInt("idAppello"));
                    ExamSession examSession = new ExamSession(rs.getInt("idAppello"), courseID, rs.getDate("dataOra"), enrolled);
                    examSessions.add(examSession);
                }
            }
        }
        return examSessions;
    }

    @Override
    public Enrollment getEnrollment(int studentID, int examSessionID) throws SQLException {
        Enrollment enrollment = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT matricola, cognome, studente.nome, email, corsoLaurea, voto, statoValutazione, idVerbale, idAppello, idCorso FROM esame NATURAL JOIN appello NATURAL JOIN studente WHERE matricola = ? AND idAppello = ?")) {
            ps.setInt(1, studentID);
            ps.setInt(2, examSessionID);

            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    int studentNumber = Integer.parseInt(rs.getString("matricola"));
                    int courseID = Integer.parseInt(rs.getString("idCorso"));
                    String studentName = rs.getString("nome");
                    String studentSurname = rs.getString("cognome");
                    String studentEmail = rs.getString("email");
                    String degreeCourse = rs.getString("corsoLaurea");
                    String grade = rs.getString("voto");
                    GradingStatus gradingStatus = GradingStatus.valueOf(rs.getString("statoValutazione"));
                    String verbalID = rs.getString("idVerbale");
                    Student student = new Student(studentNumber, studentName, studentSurname, studentEmail, degreeCourse);
                    enrollment = new Enrollment(student, grade, gradingStatus, verbalID, courseID, examSessionID);
                }
            }
        }
        return enrollment;
    }

    @Override
    public ExamSession getExamSession(int examSessionID) throws SQLException {
        ExamSession examSession = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM appello WHERE idAppello = ?")){
            ps.setInt(1, examSessionID);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    int courseID = rs.getInt("idCorso");
                    Date dateTime = rs.getDate("dataOra");
                    examSession = new ExamSession(examSessionID, courseID, dateTime, true);
                }
            }
        }
        return examSession;
    }

    @Override
    public String getStudentGrade(int studentID, int examSessionID) throws SQLException {
        String grade = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT voto FROM esame WHERE matricola = ? AND idAppello = ?")) {
            ps.setInt(1, studentID);
            ps.setInt(2, examSessionID);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) grade = rs.getString("voto");
            }
        }
        return grade;
    }

    @Override
    public GradingStatus getStudentGradingStatus(int studentID, int examSessionID) throws SQLException {
        GradingStatus gradingStatus = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT statoValutazione FROM esame WHERE matricola = ? AND idAppello = ?")) {
            ps.setInt(1, studentID);
            ps.setInt(2, examSessionID);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) gradingStatus = GradingStatus.valueOf(rs.getString("statoValutazione"));
            }
        }
        return gradingStatus;
    }

    @Override
    public void gradeStudents(String[] grades, String[] studentIDs, int examSessionID, int teacherID) throws SQLException {
        //Aggiorno il voto e lo stato
        try(PreparedStatement ps = connection.prepareStatement("UPDATE esame NATURAL JOIN appello NATURAL JOIN corso SET voto=?, statoValutazione=? WHERE matricola=? AND idAppello=? AND codiceDocente=? AND (statoValutazione=? OR statoValutazione=?)")){
            for (int i = 0; i < grades.length; i++) {
                if (grades[i] == null || grades[i].isBlank() || studentIDs[i] == null || studentIDs[i].isBlank()) continue;
                String grade = grades[i];
                int student = Integer.parseInt(studentIDs[i]);
                ps.setString(1, grade);

                //Se il voto è "Vuoto" lo stato diventa "Non inserito", altrimenti "Inserito"
                if (!grade.equals("Vuoto")) ps.setString(2, "Inserted");
                else ps.setString(2, "NotInserted");
                ps.setInt(3, student);
                ps.setInt(4, examSessionID);
                ps.setInt(5, teacherID);
                ps.setString(6, "NotInserted");
                ps.setString(7, "Inserted");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }


    @Override
    public void publishGrades(int examSessionID) throws SQLException { //TODO: Mettere in sicurezza questa query (il prof che sta pubblicando è responsabile dell'appello?)
        try(PreparedStatement ps = connection.prepareStatement("UPDATE esame SET statoValutazione='Published' WHERE statoValutazione='Inserted' AND idAppello=?")) {
            ps.setInt(1, examSessionID);
            ps.execute();
        }
    }

    @Override
    public void rejectGrade(int studentID, int examSessionID) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement("UPDATE esame SET statoValutazione = 'Rejected' WHERE esame.matricola = ? AND esame.idAppello = ?")){
            ps.setInt(1, studentID);
            ps.setInt(2, examSessionID);
            ps.execute();
        }
    }
}
