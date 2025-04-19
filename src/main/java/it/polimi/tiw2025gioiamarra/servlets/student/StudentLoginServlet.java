package it.polimi.tiw2025gioiamarra.servlets.student;

import it.polimi.tiw2025gioiamarra.servlets.utils.LoginType;
import it.polimi.tiw2025gioiamarra.servlets.utils.ThymeleafUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name="studentLoginServlet", value="/student-login")
public class StudentLoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ThymeleafUtils.process("WEB-INF/templates/studentlogin.html", request, response, getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setAttribute("type", LoginType.Student);
        RequestDispatcher rd = request.getRequestDispatcher("/login");
        rd.forward(request, response);
    }
}
