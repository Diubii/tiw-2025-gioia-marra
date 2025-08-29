package it.polimi.tiw2025gioiamarra.daos.MySQLImplementations;

import it.polimi.tiw2025gioiamarra.daos.interfaces.CoursesDAO;
import it.polimi.tiw2025gioiamarra.items.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MySQLCoursesDAO implements CoursesDAO {
    private final Connection connection;

    public MySQLCoursesDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public ArrayList<Course> getStudentCourses(int studentID) throws SQLException {
        ArrayList<Course> courses = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("SELECT corso.idCorso,nome FROM iscrizioneCorso JOIN corso ON iscrizioneCorso.idCorso = corso.idCorso WHERE iscrizioneCorso.matricola = ? ORDER BY nome DESC")) {
            getCourses(studentID, courses, ps);
        }
        return courses;
    }

    @Override
    public ArrayList<Course> getTeacherCourses(int teacherID) throws SQLException {
        ArrayList<Course> courses = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM corso WHERE codiceDocente = ? ORDER BY nome DESC")) {
            getCourses(teacherID, courses, ps);
        }
        return courses;
    }

    @Override
    public Course getCourse(int courseID) throws SQLException { //TODO: Mettere in sicurezza
        Course course = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM corso WHERE idCorso = ?")){
            ps.setInt(1, courseID);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    course = new Course(rs.getInt("idCorso"), rs.getString("nome"));
                }
            }
        }
        return course;
    }

    @Override
    public Integer getCourseIDFromExamSessionID(int examSessionID) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement("SELECT idCorso FROM appello WHERE idAppello=?")) {
            ps.setInt(1, examSessionID);
            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()) return rs.getInt("idCorso");
                else return null;
            }
        }
    }

    @Override
    public String getCourseNameFromCourseID(int courseID) throws SQLException {
        String courseName = null;
        try(PreparedStatement ps = connection.prepareStatement("SELECT nome FROM corso WHERE idCorso = ?")){
            ps.setInt(1, courseID);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) courseName = rs.getString("nome");
            }
        }
        return courseName;
    }

    private void getCourses(int teacherID, ArrayList<Course> courses, PreparedStatement ps) throws SQLException {
        ps.setInt(1, teacherID);
        try(ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int courseID = rs.getInt("idCorso");
                String name = rs.getString("nome");
                courses.add(new Course(courseID, name));
            }
        }
    }
}
