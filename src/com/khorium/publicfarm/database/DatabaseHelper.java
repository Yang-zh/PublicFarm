package com.khorium.publicfarm.database;

import com.khorium.publicfarm.Utils.LogUtil;
import com.khorium.publicfarm.farm.Farm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.jline.internal.Log;

import java.sql.*;
import java.util.HashMap;

public class DatabaseHelper {

    private static HashMap<String, Farm> farms = new HashMap<String, Farm>();
    private static Database db;

    public static void setup(Database db) throws SQLException {
        if (!db.hasTable("farms")) {
            createFarmsTable(db);
        }
        DatabaseHelper.db = db;
        refreshFarms();
    }

    /**
     * Creates the database table 'farms'.
     *
     * @throws SQLException If the connection is invalid.
     */
    public static void createFarmsTable(Database db) throws SQLException {
        Statement st = db.getConnection().createStatement();
        String createTable = "CREATE TABLE farms (" + "id VARCHAR(32) PRIMARY" +
                " KEY NOT NULL, " + "type  TEXT(32) NOT NULL, " + "name  TEXT" +
                "(32) NOT NULL, " + "x1  " + "INTEGER(32) NOT NULL, " + "y1  " +
                "INTEGER(32) NOT NULL, " + "z1" + "  INTEGER(32) NOT NULL, "
                + "x2  INTEGER(32) NOT NULL, " + "y2  INTEGER(32) NOT NULL, "
                + "z2  INTEGER(32) NOT NULL, " + "world VARCHAR(32) NOT NULL"
                + ");";
        st.execute(createTable);
    }

    public static HashMap<String, Farm> getAllFarms() {

        if (farms == null) {
            refreshFarms();
        }

        return farms;
    }

    private static void refreshFarms() {
        try {
            Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " +
                    "farms");
            ResultSet rs = ps.executeQuery();
            int x1, y1, z1, x2, y2, z2 = 0;
            World world = null;
            String farmName = null;
            String farmType = null;
            String uuid = null;
            farms.clear();
            while (rs.next()) {
                x1 = rs.getInt("x1");
                y1 = rs.getInt("y1");
                z1 = rs.getInt("z1");
                x2 = rs.getInt("x2");
                y2 = rs.getInt("y2");
                z2 = rs.getInt("z2");
                world = Bukkit.getWorld(rs.getString("world"));
                farmName = rs.getString("name");
                farmType = rs.getString("type");
                uuid = rs.getString("id");

                farms.put(uuid, new Farm(farmName, farmType, uuid, new
                        Location(world, x1, y1, z1), new Location(world,
                        x2, y2, z2)));
            }
            LogUtil.logInfo("farms count:" + farms.size());
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.logInfo("refresh error");
        }
    }

    public static boolean deleteFarm(String name) {
        try {
            Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM farms " +
                    "Where name = \'" + name + "\'");
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        refreshFarms();
        return true;
    }

    //INSERT INTO farms VALUES ('', '', '', '', '', '', '', '', '', '')

    public static boolean addFarm(Farm farm) {
        try {
            Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO farms "
                    + "VALUES (\'" + farm.getId() + "\', \'" + farm.getType()
                    + "\', \'" + farm.getName() + "\', \'" + farm.getX1() +
                    "\'," + " \'" + farm.getY1() + "\', \'" + farm.getZ1() +
                    "\', \'" + farm.getX2() + "\', \'" + farm.getY2() + "\',"
                    + " \'" + farm.getZ2() + "\', \'" + farm.getWorld() +
                    "\')");
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        refreshFarms();
        return true;
    }
}