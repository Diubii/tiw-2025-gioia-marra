package it.polimi.tiw2025gioiamarra.servlets.teacher;

import it.polimi.tiw2025gioiamarra.items.PageType;
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
import java.sql.Statement;

@WebServlet(name="GenerateReportServlet", value="/generateReport")
public class GenerateReportServlet extends HttpServlet{
    //Crea un nuovo report relativo a appello specificato con data odierna
    //Guarda tutte le righe esame con rifiutato o pubblicato e le mette verbalizzato e vi associa l'idReport generato

    //se ci sono voti per i quali è possibile fare un report lo fa e lo reindirizza a visualizzazione report con l'id di quel report
    //altrimenti ritorna a li con error 1 magari
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course", "session"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Teacher, session, request, response)) return;

        Connection con = Database.getConnection();
        if (con == null) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;
        ResultSet rs = null;

        int courseID = Integer.parseInt(request.getParameter("course"));
        int sessionID = Integer.parseInt(request.getParameter("session"));

        //Faccio transazione, annullo se non ho associato nessun voto
        try {
            con.setAutoCommit(false);

            //Inserisco Verbale
            psInsert = con.prepareStatement("INSERT INTO `verbale` (`idVerbale`, `dataOra`, `idAppello`) VALUES (NULL, NOW(), ?);", Statement.RETURN_GENERATED_KEYS);
            psInsert.setInt(1, sessionID);
            psInsert.executeUpdate();

            rs = psInsert.getGeneratedKeys();
            int idVerbale = -1;
            if (rs.next()) {
                idVerbale = rs.getInt(1);
            }

            //Aggiorno tabella esami andando a verbalizzare i voti
            psUpdate = con.prepareStatement(
                        "UPDATE esame " +
                            "  SET  voto = CASE " +
                            "             WHEN statoValutazione = 'Rejected' THEN 'Rimandato' " +
                            "             ELSE voto " +
                            "            END, " +
                            "   statoValutazione = 'Verbalized', " +
                            "    idVerbale = ? " +
                            "WHERE statoValutazione = 'Published' OR statoValutazione = 'Rejected'"
            );
            psUpdate.setInt(1, idVerbale);
            int updated = psUpdate.executeUpdate();

            //Se ho associato dei voti al verbale committo, altrimenti rollback
            if (updated > 0) {
                con.commit();
                //redirigo
                response.sendRedirect("report?reportId=" + idVerbale + "&sessionId=" + sessionID);
            } else {
                con.rollback();
                //redirigo con avviso
                response.sendRedirect("enrolled-students?course=" + courseID + "&session=" + sessionID +"&sort=matricola&order=asc&emptyReport=1");
            }

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException _) {}
            ThymeleafUtils.redirectToDBErrorPage(response);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(psInsert);
            DbUtils.closeQuietly(psUpdate);
            DbUtils.closeQuietly(con);
        }
    }
}
