package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.LoginType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.Utils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.h2.security.SHA256;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.SQLException;

@WebServlet(name = "loginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getAttribute("type") == null){
            response.sendRedirect("/student-login");
            return;
        }

        String studentNumber = request.getParameter("studentNumber");
        String teacherID = request.getParameter("teacherID");

        Integer id = null;
        try {
            if (studentNumber != null) id = Integer.parseInt(studentNumber);
            else if (teacherID != null) id = Integer.parseInt(teacherID);
        } catch (NumberFormatException e) {
            if(studentNumber != null) response.sendRedirect("student-login?error=1");
            else response.sendRedirect("teacher-login?error=1");
            return;
        }

        if(id == null){
            response.sendRedirect("student-login?error=1");
            return;
        }

        try(DatabaseConnection con = new DatabaseConnection()){
            String password = request.getParameter("password");

            switch (request.getAttribute("type")) {
                case LoginType.Student -> {
                    if (con.getMySQLStudentDAO().login(id, password, request.getSession()))
                        response.sendRedirect("courses");
                    else
                        response.sendRedirect("student-login?error=1");
                }

                case LoginType.Teacher -> {
                    if (con.getMySQLTeacherDAO().login(id, password, request.getSession()))
                        response.sendRedirect("courses");
                    else
                        response.sendRedirect("teacher-login?error=1");
                }

                default -> throw new IllegalStateException("Unexpected value: " + request.getAttribute("type"));
            }
        } catch(SQLException | IllegalStateException e){
            Utils.redirectToDBErrorPage(response);
        }
        catch (NumberFormatException e){
            if(studentNumber != null) response.sendRedirect("student-login?error=1");
            if(teacherID != null) response.sendRedirect("teacher-login?error=1");
        }
    }
}
