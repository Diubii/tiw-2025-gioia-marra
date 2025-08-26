package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.*;
import it.polimi.tiw2025gioiamarra.utils.Database;
import it.polimi.tiw2025gioiamarra.utils.ThymeleafUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.dbutils.DbUtils;

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

        String[] mandatoryParams = {};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        Connection con = Database.getConnection();
        if (con == null) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        ArrayList<Report> reportList = new ArrayList<>();
        boolean error = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT idVerbale,verbale.dataOra AS dataVerbale, appello.dataOra AS dataAppello,nome,appello.idAppello AS idAppello FROM corso JOIN appello ON corso.idCorso = appello.idCorso JOIN verbale ON verbale.idAppello = appello.idAppello WHERE corso.codiceDocente = ? ORDER BY corso.nome,appello.dataOra;");
            ps.setInt(1, (Integer) session.getAttribute("teacher"));

            rs = ps.executeQuery();
            while (rs.next()) {
                int reportId = Integer.parseInt(rs.getString("idVerbale"));
                String courseName =  rs.getString("nome");
                Report report = new Report(reportId, rs.getDate("dataVerbale"), courseName, rs.getDate("dataAppello"),Integer.parseInt(rs.getString("idAppello")));
                reportList.add(report);
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
