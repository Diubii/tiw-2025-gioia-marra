package it.polimi.tiw2025gioiamarra.daos.interfaces;

import it.polimi.tiw2025gioiamarra.items.Student;
import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;
import java.util.ArrayList;

public interface StudentDAO {
    //void updatePasswords(String newPassword) throws SQLException;
    Student getStudentFromID(int id) throws SQLException;
    Student getStudentForGrading(int id, int courseID, int examSessionID, int teacherID) throws SQLException;
    boolean login(int id, String password, HttpSession session) throws SQLException;
}