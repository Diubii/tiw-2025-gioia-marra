package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.Utils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "loginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getAttribute("type") == null){
            response.sendRedirect("/student-login");
            return;
        }

        try(DatabaseConnection con = new DatabaseConnection()){
            String password = request.getParameter("password");
            switch (request.getAttribute("type")) {
                case LoginType.Student -> {
                    int id = Integer.parseInt(request.getParameter("studentNumber"));
                    if (con.getMySQLStudentDAO().login(id, password, request.getSession()))
                        response.sendRedirect("courses");
                    else
                        response.sendRedirect("student-login?error=1");
                }

                case LoginType.Teacher -> {
                    int id = Integer.parseInt(request.getParameter("teacherID"));
                    if (con.getMySQLTeacherDAO().login(id, password, request.getSession()))
                        response.sendRedirect("courses");
                    else
                        response.sendRedirect("teacher-login?error=1");
                }

                default -> throw new IllegalArgumentException("Unexpected value: " + request.getAttribute("type"));
            }
        } catch(SQLException | IllegalArgumentException e){
            Utils.redirectToDBErrorPage(response);
        }
    }
}
