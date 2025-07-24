package it.polimi.tiw2025gioiamarra.items;

public record Enrollment(Student student, String grade, GradingStatus gradingStatus, String verbalID, int courseID, int examSessionID) { }
