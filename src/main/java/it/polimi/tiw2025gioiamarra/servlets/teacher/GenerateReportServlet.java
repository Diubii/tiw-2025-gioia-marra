package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.PageType;
import it.polimi.tiw2025gioiamarra.utils.DatabaseConnection;
import it.polimi.tiw2025gioiamarra.utils.Utils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="GenerateReportServlet", value="/generateReport")
public class GenerateReportServlet extends HttpServlet{
    //Crea un nuovo report relativo a appello specificato con data odierna
    //Guarda tutte le righe esame con rifiutato o pubblicato e le mette verbalizzato e vi associa l'idReport generato

    //se ci sono voti per i quali è possibile fare un report lo fa e lo reindirizza a visualizzazione report con l'id di quel report
    //altrimenti ritorna a li con error 1 magari
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!Utils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        Integer courseID = null;
        Integer sessionID = null;

        try {
            courseID = Integer.parseInt(request.getParameter("course"));
            sessionID = Integer.parseInt(request.getParameter("session"));
        }
        catch (NumberFormatException e) {
            if(courseID != null) response.sendRedirect("exam-sessions?course=" + courseID);
            else response.sendRedirect("courses");
            return;
        }

        int reportID;

        //Faccio transazione, annullo se non ho associato nessun voto
        try (DatabaseConnection con = new DatabaseConnection()) {
            reportID = con.getMySQLReportsDAO().generateReport(sessionID);
        } catch (SQLException e) {
            Utils.redirectToDBErrorPage(response);
            return;
        }

        if(reportID >= 0) response.sendRedirect("report?report=" + reportID + "&session=" + sessionID);
        else response.sendRedirect("enrolled-students?course=" + courseID + "&session=" + sessionID +"&sort=matricola&order=asc&emptyReport=1");
    }
}
