package it.polimi.tiw2025gioiamarra.daos.MySQLImplementations;

import it.polimi.tiw2025gioiamarra.daos.interfaces.StudentDAO;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.items.Student;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLStudentDAO implements StudentDAO {
    private final Connection connection;

    public MySQLStudentDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Student getStudentFromID(int id) throws SQLException {
        Student student = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM studente WHERE matricola = ?")) {
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("nome");
                    String surname = rs.getString("cognome");
                    String email = rs.getString("email");
                    String degreeCourse = rs.getString("corsoLaurea");
                    student = new Student(id, name, surname, email, degreeCourse);
                }
            }
        }
        return student;
    }

    @Override
    public Student getStudentForGrading(int id, int courseID, int examSessionID, int teacherID) throws SQLException {
        Student student = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT matricola, studente.cognome, studente.nome, email, corsoLaurea FROM esame NATURAL JOIN appello NATURAL JOIN studente JOIN corso on appello.idCorso = corso.idCorso WHERE studente.matricola = ? AND appello.idCorso = ? AND idAppello = ? AND corso.codiceDocente = ? AND (statoValutazione = ? OR statoValutazione = ?)")) {
            ps.setInt(1, id);
            ps.setInt(2, courseID);
            ps.setInt(3, examSessionID);
            ps.setInt(4, teacherID);
            ps.setString(5, "Inserted");
            ps.setString(6, "NotInserted");

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()) {
                    String name = rs.getString("nome");
                    String surname = rs.getString("cognome");
                    String email = rs.getString("email");
                    String degreeCourse = rs.getString("corsoLaurea");
                    student = new Student(id, name, surname, email, degreeCourse);
                }
            }
        }

        return student;
    }

    @Override
    public boolean login(int id, String password, HttpSession session) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM studente where matricola=? and pass=?")) {
            ps.setInt(1, id);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    session.setAttribute("type", LoginType.Student);
                    session.setAttribute("student", rs.getInt("matricola"));
                    return true;
                } else return false;
            }
        }
    }
}
