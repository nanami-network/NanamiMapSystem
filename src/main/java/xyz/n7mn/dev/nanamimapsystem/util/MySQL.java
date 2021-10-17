package xyz.n7mn.dev.nanamimapsystem.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.Enumeration;

public class MySQL {

    private final static Plugin plugin = Bukkit.getPluginManager().getPlugin("NanamiMapSystem");

    public static Connection getConnect(){

        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            return con;
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        return null;
    }


    public static void disconnectConnect(Connection con){

        try {
            if (con != null){
                con.close();
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }

    }

}
