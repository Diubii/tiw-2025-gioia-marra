package it.polimi.tiw2025gioiamarra.items;


import java.util.Date;

public record Report(int id, Date reportDate, int courseID, Date examDate, int sessionID) {
}


