package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet(name="publishGradesServlet", value="/publish-grades")
public class PublishGradesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        int courseID = Integer.parseInt(request.getParameter("course"));
        int examSessionID = Integer.parseInt(request.getParameter("session"));

        String sort = request.getParameter("sort");
        if(sort == null || sort.isEmpty()) sort = "matricola";
        String order = request.getParameter("order");
        if(order == null || order.isEmpty()) order = "asc";

        try (DatabaseConnection con = new DatabaseConnection()) {
            con.getMySQLExamSessionsDAO().publishGrades(examSessionID);
        } catch (SQLException e) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        response.sendRedirect("enrolled-students?course=" + courseID + "&session=" + examSessionID + "&sort=" + sort + "&order=" + order);
    }
}
