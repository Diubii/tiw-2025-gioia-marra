package it.polimi.tiw2025gioiamarra.servlets.student;

import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name="ExamResultServerlet", value = "/ExamResult")
public class ExamResultServerlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        //Prefisso /WEB-INF/templates/ e suffisso .html
        ThymeleafUtils.process("pure/student/examresult", request, response, getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

    }

}
