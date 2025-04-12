package it.polimi.tiw2025gioiamarra;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

@WebServlet(name = "enrollmentsServlet", value = "/enrollments-servlet")
public class EnrollmentsServlet extends HttpServlet {

    Connection connection;

    public void init() {
        connection = Database.getConnection();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(connection != null){
        }
        else{
            connection = Database.getConnection();
        }
    }

    public void destroy() {
    }
}
