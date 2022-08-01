package eu.darkbot.kaiserdj.hell.Deadpool;

import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class DeadpoolData {
    private ArrayList<String[]> data;
    Connection connection;
    String db;

    Long sessionTime;

    public DeadpoolData() {
        this.data = null;

        File dbfile = new File(System.getProperty("user.dir") + File.separator + "hell" + File.separator + "Deadpool.db");

        this.db = "jdbc:sqlite:" + dbfile.getAbsolutePath();
        this.sessionTime = Instant.now().toEpochMilli();

        boolean existsdb = dbfile.exists();

        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(this.db);
            if (!existsdb) {
                this.createDB();
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createDB() throws SQLException {
        Statement stmt = this.connection.createStatement();
        stmt.execute("CREATE TABLE Killer " +
                "(Name  Text," +
                " Id    Text)");
        stmt.execute("CREATE TABLE Details " +
                "(Name  TEXT, " +
                " Id  Text, " +
                " Map  TEXT, " +
                " Location  TEXT, " +
                " Time  INTEGER)");
    }

    public void addData(String[] dead) throws SQLException {
        Statement stmt = this.connection.createStatement();
        PreparedStatement pstmt_Killer = this.connection.prepareStatement("INSERT INTO Killer(Name,Id) VALUES(?,?)");
        PreparedStatement pstmt_Killer_Update = this.connection.prepareStatement("UPDATE Killer SET Name = ? WHERE Id = ?");
        PreparedStatement pstmt_Details = this.connection.prepareStatement("INSERT INTO Details(Name,Id,Map,Location,Time) VALUES(?,?,?,?,?)");

        ResultSet rs;
        if (dead[1].equals("Npc") || dead[1].equals("Unknown")) {
            rs = stmt.executeQuery("SELECT * FROM Killer WHERE Name='" + dead[0] + "' and Id='" + dead[1] + "'");
        } else {
            rs = stmt.executeQuery("SELECT * FROM Killer WHERE Id='" + dead[1] + "'");
        }

        if (!rs.next()) {
            pstmt_Killer.setString(1, dead[0]);
            pstmt_Killer.setString(2, dead[1]);
            pstmt_Killer.executeUpdate();
        } else {
            pstmt_Killer_Update.setString(1, dead[0]);
            pstmt_Killer_Update.setString(2, dead[1]);
            pstmt_Killer_Update.executeUpdate();
        }

        pstmt_Details.setString(1, dead[0]);
        pstmt_Details.setString(2, dead[1]);
        pstmt_Details.setString(3, dead[2]);
        pstmt_Details.setString(4, dead[3]);
        pstmt_Details.setLong(5, Long.parseLong(dead[4]));
        pstmt_Details.executeUpdate();
    }

    public ArrayList<String[]> getGeneral(String type) throws SQLException {
        ArrayList<String[]> resutl = new ArrayList<>();

        int pos = 1;

        String sessionSQL = "";
        if(type.equals("session")){
            sessionSQL = "WHERE va.Time > " + this.sessionTime + " ";
        }

        Statement stmt = this.connection.createStatement();
        ResultSet rs = stmt.executeQuery("" +
                "SELECT v.Name, v.Id, count(va.Name) Deaths " +
                "FROM Killer v " +
                "INNER JOIN Details va ON va.Id = v.Id " +
                sessionSQL +
                "GROUP BY v.Name, v.Id " +
                "order by Deaths desc;");

        while (rs.next()) {
            resutl.add(new String[]{String.valueOf(pos++), rs.getString("Name"), rs.getString("Id"), rs.getString("Deaths")});
        }

        return resutl;
    }

    public ArrayList<String[]> getDetails(String type, String id) throws SQLException {
        ArrayList<String[]> resutl = new ArrayList<>();

        String sessionSQL = "";
        if(type.equals("session")){
            sessionSQL = " and Time > " + this.sessionTime + " ";
        }

        Statement stmt = this.connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM Details WHERE Id='" + id + "'" + sessionSQL);

        while (rs.next()) {
            DateFormat df = new SimpleDateFormat("HH:mm:ss  dd-MM-yyyy");

            resutl.add(new String[]{
                    rs.getString("Id"),
                    rs.getString("Name"),
                    rs.getString("Map"),
                    rs.getString("Location"),
                    df.format(rs.getLong("Time"))
            });
        }

        return resutl;
    }

    public void resetData() throws SQLException {
        PreparedStatement pstmt = this.connection.prepareStatement("DELETE FROM Details");
        pstmt.executeUpdate();
    }

    public void resetData(String id) throws SQLException {
        PreparedStatement pstmt = this.connection.prepareStatement("DELETE FROM Details WHERE Id = ?");
        pstmt.setString(1, id);
        pstmt.executeUpdate();
    }
}
