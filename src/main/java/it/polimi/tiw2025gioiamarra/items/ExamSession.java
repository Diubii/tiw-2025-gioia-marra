package it.polimi.tiw2025gioiamarra.items;

import java.util.Date;

public record ExamSession (int id, Course course, Date dateTime, Boolean enrolled) {}
