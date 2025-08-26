package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.utils.Database;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.sql.*;

@WebServlet(name = "loginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection con = Database.getConnection();
        if (con == null) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            switch (request.getAttribute("type")) {
                case LoginType.Student:
                    ps = con.prepareStatement("SELECT * FROM studente where matricola=? and pass=?");
                    try{
                        ps.setInt(1, Integer.parseInt(request.getParameter("studentNumber")));
                    }catch (Exception e){
                        response.sendRedirect("student-login?error=1");
                    }
                    break;
                case LoginType.Teacher:
                    ps = con.prepareStatement("SELECT * FROM docente where codiceDocente=? and pass=?");
                    try{
                        ps.setInt(1, Integer.parseInt(request.getParameter("teacherID")));
                    } catch (Exception e) {
                        response.sendRedirect("teacher-login?error=1");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + request.getAttribute("type"));
            }

            ps.setString(2, request.getParameter("password"));

            rs = ps.executeQuery();
            if (rs.next()) {
                HttpSession session = request.getSession();
                switch (request.getAttribute("type")) {
                    case LoginType.Student:
                        session.setAttribute("type", LoginType.Student);
                        session.setAttribute("student", rs.getInt("matricola"));
                        break;
                    case LoginType.Teacher:
                        session.setAttribute("type", LoginType.Teacher);
                        session.setAttribute("teacher", rs.getInt("codiceDocente"));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + request.getAttribute("type"));
                }
                response.sendRedirect("courses");
            }
            else{

                switch (request.getAttribute("type")){
                    case LoginType.Student:
                        response.sendRedirect("student-login?error=1");
                        break;
                    case LoginType.Teacher:
                        response.sendRedirect("teacher-login?error=1");
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + request.getAttribute("type"));
                }

            }
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
        }
        finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(con);
        }
    }
}
