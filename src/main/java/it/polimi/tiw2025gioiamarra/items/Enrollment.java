package it.polimi.tiw2025gioiamarra.items;

public class Enrollment {
    private final int studentNumber;
    private final String studentFullName;
    private final String studentEmail;
    private final String degreeCourse;
    private final String grade;
    private final String gradingStatus;
    private final String verbalID;
    private final int examSession;

    public Enrollment(int studentNumber, String studentFullName, String studentEmail, String degreeCourse, String grade, String gradingStatus, String verbalID, int examSession) {
        this.studentNumber = studentNumber;
        this.studentFullName = studentFullName;
        this.studentEmail = studentEmail;
        this.degreeCourse = degreeCourse;
        this.grade = grade;
        this.gradingStatus = gradingStatus;
        this.verbalID = verbalID;
        this.examSession = examSession;
    }

    public int getStudentNumber() {
        return studentNumber;
    }
    public String getStudentFullName() {
        return studentFullName;
    }
    public String getStudentEmail() {
        return studentEmail;
    }
    public String getDegreeCourse() {
        return degreeCourse;
    }
    public String getGrade() {
        return grade;
    }
    public String getGradingStatus() {
        return gradingStatus;
    }
    public String getVerbalID() {
        return verbalID;
    }
    public int getExamSession() {
        return examSession;
    }
}
