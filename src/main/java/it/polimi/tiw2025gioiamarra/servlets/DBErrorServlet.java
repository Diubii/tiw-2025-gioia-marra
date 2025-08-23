package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "DBErrorServlet", value = "/db-error")
public class DBErrorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().invalidate();
        ThymeleafUtils.process("db-error.html", req, resp, getServletContext());
    }
}
