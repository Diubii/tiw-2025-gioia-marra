package it.polimi.tiw2025gioiamarra.items;

import java.util.Date;

public record ExamSession (int id, int courseID, Date dateTime, Boolean enrolled) {}
