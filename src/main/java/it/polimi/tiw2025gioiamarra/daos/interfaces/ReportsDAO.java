package it.polimi.tiw2025gioiamarra.daos.interfaces;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.Report;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ReportsDAO {
    ArrayList<Report> getTeacherReports(int teacherID) throws SQLException;
    Report getReport(int reportID, int examSessionID, int teacherID) throws SQLException;
    ArrayList<Enrollment> getEnrollmentsInReport(int reportID) throws SQLException;
    int generateReport(int sessionID, int teacherID) throws SQLException;
}
