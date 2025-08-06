package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.Enrollment;
import it.polimi.tiw2025gioiamarra.items.GradingStatus;
import it.polimi.tiw2025gioiamarra.items.Report;
import it.polimi.tiw2025gioiamarra.items.Student;
import it.polimi.tiw2025gioiamarra.utils.Database;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name="ReportListServlet", value="/report-list")
public class ReportListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("type") == null || session.getAttribute("teacher") == null) {
            response.sendRedirect("teacher-login");
            return;
        }

        Connection con = Database.getConnection();
        ArrayList<Report> reportList = new ArrayList<>();
        try {

            PreparedStatement ps = con.prepareStatement("SELECT idVerbale,verbale.dataOra AS dataVerbale, appello.dataOra AS dataAppello,nome,appello.idAppello AS idAppello FROM corso JOIN appello ON corso.idCorso = appello.idCorso JOIN verbale ON verbale.idAppello = appello.idAppello WHERE corso.codiceDocente = ? ORDER BY corso.nome,appello.dataOra;");
            ps.setInt(1, (Integer) session.getAttribute("teacher"));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int reportId = Integer.parseInt(rs.getString("idVerbale"));
                String courseName =  rs.getString("nome");
                Report report = new Report(reportId, rs.getDate("dataVerbale"), courseName, rs.getDate("dataAppello"),Integer.parseInt(rs.getString("idAppello")));
                reportList.add(report);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
        request.setAttribute("reports", reportList);

        //CONFIGURAZIONE LAYOUT
        request.setAttribute("type", session.getAttribute("type"));
        request.setAttribute("pageTitle", "Verbali");
        request.setAttribute("fragname", "reportList");
        request.setAttribute("fragkind", "teacherFragments");
        request.setAttribute("backLink", "#");
        ThymeleafUtils.process(ThymeleafUtils.getRightPath(request), request, response, getServletContext());

    }

}
