package it.polimi.tiw2025gioiamarra.servlets;

import it.polimi.tiw2025gioiamarra.items.Course;
import it.polimi.tiw2025gioiamarra.items.ExamSession;
import it.polimi.tiw2025gioiamarra.items.LoginType;
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
import java.util.ArrayList;

@WebServlet(name = "examSessionsServlet", value = "/exam-sessions")
public class ExamSessionsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String[] mandatoryParams = {"course"};
        if(!ThymeleafUtils.mandatoryParametersCheck(mandatoryParams, PageType.Common, session, request, response)) return;

        Connection con = Database.getConnection();

        if (con == null) {
            ThymeleafUtils.redirectToDBErrorPage(response);
            return;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ExamSession> examSessions = new ArrayList<>();
        ArrayList<Integer> appelliIscritto = new ArrayList<>();
        Course course;
        try { //TODO: Mettere in sicurezza
            ps = con.prepareStatement("SELECT * FROM corso WHERE idCorso = ?");
            ps.setInt(1, Integer.parseInt(request.getParameter("course")));
            rs = ps.executeQuery();
            if (rs.next()) {
                course = new Course(rs.getInt("idCorso"), rs.getString("nome"));
                request.setAttribute("course", course);
            }
            else{
                response.sendRedirect("courses");
                return;
            }

            //preparare tab exam di questo studente e metto enrolled solo se anche li
            if(session.getAttribute("type").equals(LoginType.Student)) {
                int matricola = (Integer)session.getAttribute("student");
                ps = con.prepareStatement("SELECT idAppello FROM esame WHERE matricola = ?");
                ps.setInt(1, matricola);
                rs = ps.executeQuery();
                while (rs.next()) {
                    appelliIscritto.add(rs.getInt("idAppello"));
                }
            }


            ps = con.prepareStatement("SELECT * FROM appello WHERE idCorso = ? ORDER BY dataOra DESC");
            ps.setInt(1, Integer.parseInt(request.getParameter("course")));
            rs = ps.executeQuery();
            Boolean enrolled = false;
            while(rs.next()){
                if(session.getAttribute("type").equals(LoginType.Student)){
                    enrolled = appelliIscritto.contains(rs.getInt("idAppello"));
                }
                else{
                    enrolled= true;
                }
                ExamSession examSession = new ExamSession(rs.getInt("idAppello"), course, rs.getDate("dataOra"), enrolled);
                examSessions.add(examSession);
            }

            //CONFIGURAZIONE PARAMETRI SPECIFICI PER LA PAGINA
            request.setAttribute("course", course);
            request.setAttribute("examSessions", examSessions);

            //CONFIGURAZIONE LAYOUT
            request.setAttribute("type", session.getAttribute("type"));
            request.setAttribute("pageTitle", "Date appelli d'esame");
            request.setAttribute("fragname", "examsessions");
            request.setAttribute("fragkind", "generalFragments");
            request.setAttribute("backLink", "/courses");

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
