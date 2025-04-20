package it.polimi.tiw2025gioiamarra.items;

public class Course {
    private final int id;
    private final String name;
    //private final int teacherId;

    public Course(int id, String name/*, int teacherId*/) {
        this.id = id;
        this.name = name;
        //this.teacherId = teacherId;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }
}
