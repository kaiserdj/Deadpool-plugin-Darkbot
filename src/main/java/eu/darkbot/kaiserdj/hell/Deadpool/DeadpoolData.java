package eu.darkbot.kaiserdj.hell.Deadpool;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DeadpoolData {
    private ArrayList<String[]> data;
    Connection connection = null;
    String db;

    public DeadpoolData() {
        this.data = null;

        File dbfile = new File(System.getProperty("user.dir")+File.separator+"hell"+File.separator+"Deadpool.db");

        this.db = "jdbc:sqlite:" + dbfile.getAbsolutePath();

        try{
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(this.db);

            if (this.connection != null) {
                DatabaseMetaData meta = this.connection.getMetaData();
                this.createDB();
            }
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }catch (ClassNotFoundException e) {
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
                " Time  TEXT)");
    }

    public void addData(String[] dead) throws SQLException {
        Statement stmt = this.connection.createStatement();
        PreparedStatement pstmt_Killer = this.connection.prepareStatement("INSERT INTO Killer(Name,Id) VALUES(?,?)");
        PreparedStatement pstmt_Killer_Update = this.connection.prepareStatement("UPDATE Killer SET Name = ? WHERE Id = ?");
        PreparedStatement pstmt_Details = this.connection.prepareStatement("INSERT INTO Details(Name,Id,Map,Location,Time) VALUES(?,?,?,?,?)");

        ResultSet rs;
        if (dead[1].equals("Npc") || dead[1].equals("Unknown") ) {
            rs = stmt.executeQuery("SELECT * FROM Killer WHERE Name='" + dead[0] + "' and Id='" + dead[1] + "'");
        }else{
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
        pstmt_Details.setString(5, dead[4]);
        pstmt_Details.executeUpdate();
    }

    public ArrayList<String[]> getGeneral() throws SQLException {
        ArrayList<String[]> resutl = new ArrayList<>();

        int pos = 1;

        Statement stmt = this.connection.createStatement();
        ResultSet rs = stmt.executeQuery("" +
                "SELECT v.Name, v.Id, count(va.Name) Deaths " +
                "FROM Killer v " +
                "INNER JOIN Details va ON va.Id = v.Id " +
                "GROUP BY v.Name, v.Id " +
                "order by Deaths desc;");

        while(rs.next()) {
            resutl.add(new String[] {String.valueOf(pos++), rs.getString("Name"), rs.getString("Id"), rs.getString("Deaths"), "+"});
        }

        return resutl;
    }
}
