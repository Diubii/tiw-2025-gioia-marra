package it.polimi.tiw2025gioiamarra.daos.interfaces;

import it.polimi.tiw2025gioiamarra.items.Course;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CoursesDAO {
    ArrayList<Course> getStudentCourses(int studentID) throws SQLException;
    ArrayList<Course> getTeacherCourses(int teacherID) throws SQLException;
    Course getCourse(int courseID) throws SQLException;
    Integer getCourseIDFromExamSessionID(int examSessionID) throws SQLException;
    String getCourseNameFromCourseID(int courseID) throws SQLException;
}
