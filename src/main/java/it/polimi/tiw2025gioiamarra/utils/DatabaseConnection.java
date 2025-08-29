package it.polimi.tiw2025gioiamarra.utils;

import it.polimi.tiw2025gioiamarra.daos.MySQLImplementations.*;
import it.polimi.tiw2025gioiamarra.daos.interfaces.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseConnection implements AutoCloseable {

    final static String url = "database.oci.diubi.dev";
    final static String port = "3306";
    final static String db = "tiw";
    final static String user = "tiw";
    final static String psw = "tiw-2025-gioia-marra";

    private final Connection connection;

    public DatabaseConnection() throws SQLException {
        try{
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e.getMessage());
        }

        this.connection = DriverManager.getConnection("jdbc:mariadb://" + url + ":" + port + "/" + db, user, psw);
    }

    public StudentDAO getMySQLStudentDAO() {
        return new MySQLStudentDAO(connection);
    }

    public TeacherDAO getMySQLTeacherDAO() {
        return new MySQLTeacherDAO(connection);
    }

    public CoursesDAO getMySQLCoursesDAO() {
        return new MySQLCoursesDAO(connection);
    }

    public ExamSessionsDAO getMySQLExamSessionsDAO() {
        return new MySQLExamSessionsDAO(connection);
    }

    public ReportsDAO getMySQLReportsDAO() {
        return new MySQLReportsDAO(connection);
    }

    public ArrayList<String> getGrades() {
        //Get enum values to show in the select tag
        try (PreparedStatement ps = connection.prepareStatement("SELECT SUBSTRING(COLUMN_TYPE,5) " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA='tiw'" +
                "  AND TABLE_NAME='esame'" +
                "  AND COLUMN_NAME='voto'")) {
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String values = rs.getString("SUBSTRING(COLUMN_TYPE,5)");
                    values = values.replace("(", "");
                    values = values.replace(")", "");
                    values = values.replace("'", "");
                    return new ArrayList<>(Arrays.asList(values.split(",")));
                } else {
                    return new ArrayList<>();
                }
            }
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
