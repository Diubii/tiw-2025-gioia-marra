package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.LoginType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Boolean student=false;
        if(request.getSession().getAttribute("type").equals(LoginType.Student)) {
            student = true;
        }

        request.getSession().invalidate();

        if(student) {
            response.sendRedirect(request.getContextPath() + "/student-login");
        }
        else{
            response.sendRedirect(request.getContextPath() + "/teacher-login");
        }

    }
}

