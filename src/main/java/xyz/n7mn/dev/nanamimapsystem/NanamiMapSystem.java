package xyz.n7mn.dev.nanamimapsystem;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.nanamimapsystem.command.WorldCreate;
import xyz.n7mn.dev.nanamimapsystem.command.WorldLoad;
import xyz.n7mn.dev.nanamimapsystem.util.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class NanamiMapSystem extends JavaPlugin {

    private Map<String,String> map = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        Connection con = MySQL.getConnect();

        if (con != null){
            try {
                PreparedStatement statement = con.prepareStatement("SELECT * FROM MapList WHERE Active = 1");
                ResultSet set = statement.executeQuery();
                while (set.next()){
                    map.put(set.getString("WorldName"),set.getString("FolderName"));
                }
                set.close();
                statement.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }
        MySQL.disconnectConnect(con);
        getLogger().info("有効マップ数 : "+map.size() + " マップ");

        getCommand("create").setExecutor(new WorldCreate(this));
        getCommand("load").setExecutor(new WorldLoad(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        map.forEach((worldName, folderName)->{
            for (World world : getServer().getWorlds()){
                if (world.getName().equals(folderName)){
                    getServer().unloadWorld(world, true);
                }
            }
        });
    }
}
