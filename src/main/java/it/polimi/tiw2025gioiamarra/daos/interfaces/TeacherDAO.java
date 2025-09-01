package it.polimi.tiw2025gioiamarra.daos.interfaces;

import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;

public interface TeacherDAO {
    //void updatePasswords(String newPassword) throws SQLException;
    boolean login(int id, String password, HttpSession session) throws SQLException;
}
