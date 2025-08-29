package it.polimi.tiw2025gioiamarra.daos.interfaces;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.ExamSession;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ExamSessionsDAO {
    ArrayList<Enrollment> getEnrolledStudents(int courseID, int examSessionID, int teacherID, String sort, String sortDirection) throws SQLException;
    ArrayList<Integer> getStudentEnrollmentsIDs(int studentID) throws SQLException;
    ArrayList<ExamSession> getTeacherExamSessions(int courseID) throws SQLException;
    ArrayList<ExamSession> getStudentExamSessions(int courseID, int studentID) throws SQLException;
    Enrollment getEnrollment(int studentID, int examSessionID) throws SQLException;
    ExamSession getExamSession(int examSessionID) throws SQLException;
    String getStudentGrade(int studentID, int examSessionID) throws SQLException;
    GradingStatus getStudentGradingStatus(int studentID, int examSessionID) throws SQLException;
    void gradeStudents(String[] grades, String[] studentIDs, int examSessionID, int teacherID) throws SQLException;
    void publishGrades(int examSessionID) throws SQLException;
    void rejectGrade(int studentID, int examSessionID) throws SQLException;
}
