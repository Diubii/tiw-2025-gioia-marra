package it.polimi.tiw2025gioiamarra.daos.MySQLImplementations;

import it.polimi.tiw2025gioiamarra.daos.interfaces.TeacherDAO;
import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.utils.PasswordUtils;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLTeacherDAO implements TeacherDAO {
    private final Connection connection;

    public MySQLTeacherDAO(Connection connection) {
        this.connection = connection;
    }

//    @Override
//    public void updatePasswords(String newPassword) throws SQLException {
//        try(PreparedStatement ps = connection.prepareStatement("UPDATE docente SET pass = ?")) {
//            ps.setString(1, newPassword);
//            ps.executeUpdate();
//        }
//    }

    @Override
    public boolean login(int id, String password, HttpSession session) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM docente where codiceDocente=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if(PasswordUtils.matches(password, rs.getString("pass"))) {
                        session.setAttribute("type", LoginType.Teacher);
                        session.setAttribute("teacher", rs.getInt("codiceDocente"));
                        return true;
                    } else return false;
                } else return false;
            }
        }
    }
}
