package it.polimi.tiw2025gioiamarra.daos.MySQLImplementations;

import it.polimi.tiw2025gioiamarra.daos.interfaces.ExamSessionsDAO;
import it.polimi.tiw2025gioiamarra.daos.interfaces.ReportsDAO;
import it.polimi.tiw2025gioiamarra.daos.interfaces.StudentDAO;
import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.Report;
import it.polimi.tiw2025gioiamarra.items.Student;

import java.sql.*;
import java.util.ArrayList;

public class MySQLReportsDAO implements ReportsDAO {
    private final Connection connection;

    public MySQLReportsDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public ArrayList<Report> getTeacherReports(int teacherID) throws SQLException {
        ArrayList<Report> reports = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT idVerbale,verbale.dataOra AS dataVerbale, appello.dataOra AS dataAppello,nome,appello.idAppello AS idAppello, corso.idCorso FROM corso JOIN appello ON corso.idCorso = appello.idCorso JOIN verbale ON verbale.idAppello = appello.idAppello WHERE corso.codiceDocente = ? ORDER BY corso.nome,appello.dataOra;")) {
            ps.setInt(1, teacherID);

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int reportId = Integer.parseInt(rs.getString("idVerbale"));
                    int courseID = rs.getInt("idCorso");
                    Report report = new Report(reportId, rs.getDate("dataVerbale"), courseID, rs.getDate("dataAppello"), Integer.parseInt(rs.getString("idAppello")));
                    reports.add(report);
                }
            }
        }
        return reports;
    }

    @Override
    public Report getReport(int reportID, int examSessionID, int teacherID) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement("SELECT idVerbale,verbale.dataOra AS dataVerbale, appello.dataOra AS dataAppello,nome,appello.idAppello AS idAppello ,corso.idCorso AS idCorso FROM corso JOIN appello ON corso.idCorso = appello.idCorso JOIN verbale ON verbale.idAppello = appello.idAppello WHERE idVerbale=? AND appello.idAppello=? AND corso.codiceDocente=?")) {
            ps.setInt(1, reportID);
            ps.setInt(2, examSessionID);
            ps.setInt(3, teacherID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int courseID = rs.getInt("idCorso");
                    return new Report(reportID, rs.getDate("dataVerbale"), courseID, rs.getDate("dataAppello"), Integer.parseInt(rs.getString("idAppello")));
                }
                else return null;
            }
        }
    }

    @Override
    public ArrayList<Enrollment> getEnrollmentsInReport(int reportID) throws SQLException {
        ArrayList<Enrollment> enrollments = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM esame NATURAL JOIN studente WHERE idVerbale = ?")){
            ps.setInt(1, reportID);
            try(ResultSet rs = ps.executeQuery()) {
                int studentID;
                int examSessionID;
                Enrollment enrollment;
                ExamSessionsDAO examSessionsDAO = new MySQLExamSessionsDAO(connection);
                while (rs.next()) {
                    studentID = rs.getInt("matricola");
                    examSessionID = rs.getInt("idAppello");
                    enrollment = examSessionsDAO.getEnrollment(studentID, examSessionID);
                    enrollments.add(enrollment);
                }
            }
        }
        return enrollments;
    }

    @Override
    public int generateReport(int sessionID) throws SQLException {
        connection.setAutoCommit(false);

        int idVerbale = -1;
        //Inserisco Verbale
        try(PreparedStatement psInsert = connection.prepareStatement("INSERT INTO `verbale` (`idVerbale`, `dataOra`, `idAppello`) VALUES (NULL, NOW(), ?);", Statement.RETURN_GENERATED_KEYS)){
            psInsert.setInt(1, sessionID);
            psInsert.executeUpdate();

            try (ResultSet rs = psInsert.getGeneratedKeys()) {
                if (rs.next()) {
                    idVerbale = rs.getInt(1);
                }
            }
        }
        catch (SQLException e) {
            connection.rollback();
            throw new SQLException(e);
        }

        int updated;
        try(PreparedStatement psUpdate = connection.prepareStatement("UPDATE esame " +
                "  SET  voto = CASE " +
                "             WHEN statoValutazione = 'Rejected' THEN 'Rimandato' " +
                "             ELSE voto " +
                "            END, " +
                "   statoValutazione = 'Verbalized', " +
                "    idVerbale = ? " +
                "WHERE statoValutazione = 'Published' OR statoValutazione = 'Rejected'")){
            psUpdate.setInt(1, idVerbale);
            updated = psUpdate.executeUpdate();
        }
        catch (SQLException e) {
            connection.rollback();
            throw new SQLException(e);
        }

        //Se ho associato dei voti al verbale committo, altrimenti rollback
        if (updated > 0) {
            connection.commit();
        } else {
            connection.rollback();
        }

        connection.setAutoCommit(true);
        return idVerbale;
    }
}
