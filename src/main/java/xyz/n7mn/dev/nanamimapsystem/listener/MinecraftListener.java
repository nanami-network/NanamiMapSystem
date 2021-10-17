package xyz.n7mn.dev.nanamimapsystem.listener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.nanamimapsystem.util.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class MinecraftListener implements Listener {

    private final Plugin plugin;
    public MinecraftListener(Plugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerJoinEvent (PlayerJoinEvent e){

        boolean isFound = false;
        for (Plugin plugin1 : plugin.getServer().getPluginManager().getPlugins()){
            if (plugin1.getName().equals("Multiverse-Core")){
                isFound = true;
                break;
            }
        }
        if (isFound){
            return;
        }

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        if (manager.getProtocolVersion(e.getPlayer()) > 340){
            e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"ななみマップ鯖のシステムは1.12.2のため、1.12.2以降のブロックは使えません。(The Nanami map server system is 1.12.2, so blocks after 1.12.2 cannot be used.)");
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerQuitEvent (PlayerQuitEvent e){

        boolean isFound = false;
        for (Plugin plugin1 : plugin.getServer().getPluginManager().getPlugins()){
            if (plugin1.getName().equals("Multiverse-Core")){
                isFound = true;
                break;
            }
        }
        if (isFound){
            return;
        }

        World world = e.getPlayer().getWorld();
        new Thread(()->{
            try {
                sleep(10000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            Connection connect = MySQL.getConnect();
            if (connect == null){
                return;
            }

            HashMap<String, UUID> map = new HashMap<>();
            try {
                PreparedStatement statement = connect.prepareStatement("SELECT * FROM `MapList` WHERE `FolderName` = ?");
                statement.setString(1, world.getName());
                ResultSet set = statement.executeQuery();

                while (set.next()){
                    map.put(set.getString("FolderName"), UUID.fromString(set.getString("CreateUserUUID")));
                }
                set.close();
                statement.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }
            MySQL.disconnectConnect(connect);

            map.forEach(((s, uuid) -> {
                if (s.equals(world.getName())){

                    for (Player player : plugin.getServer().getOnlinePlayers()){
                        if (player.getLocation().getWorld().getName().equals(s)){
                            return;
                        }
                    }

                    Bukkit.getScheduler().runTask(plugin, ()->{
                        plugin.getServer().unloadWorld(world, true);
                    });
                    plugin.getLogger().info("誰もいない状態で10秒経過したため "+world.getName()+" を自動アンロードしました。");
                }
            }));

        }).start();

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerCommandPreprocessEvent (PlayerCommandPreprocessEvent e){
        String message = e.getMessage();


        boolean isFound = false;
        for (Plugin plugin1 : plugin.getServer().getPluginManager().getPlugins()){
            if (plugin1.getName().equals("Multiverse-Core")){
                isFound = true;
                break;
            }
        }
        if (!isFound){
            return;
        }

        if (message.startsWith("/mv create")){
            e.getPlayer().sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"システム変更のため 一時的に新規ワールド作成をできなくしております。");
            e.setCancelled(true);
        }

        if (message.startsWith("/create") || message.startsWith("/move") || message.startsWith("/remove") || message.startsWith("/load")){
            e.setCancelled(true);
        }
    }
}
