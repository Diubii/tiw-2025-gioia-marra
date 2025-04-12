package it.polimi.tiw2025gioiamarra;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    final static String url = "database.oci.diubi.dev";
    final static String port = "3306";
    final static String db = "tiw";
    final static String user = "tiw";
    final static String psw = "tiw-2025-gioia-marra";

    public static Connection getConnection() {
        Connection connection = null;
        try{
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }

        try{
            connection = DriverManager.getConnection("jdbc:mariadb://" + url + ":" + port + "/" + db, user, psw);
            System.out.println("Connection established!");
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return connection;
    }
}
