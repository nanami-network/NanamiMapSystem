package xyz.n7mn.dev.nanamimapsystem.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.nanamimapsystem.util.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WorldDelete implements CommandExecutor {

    private final Plugin plugin;
    public WorldDelete(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (player.isOp() || player.hasPermission("nanamimap.delete")){

                if (args.length != 1){
                    player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"/delete <world name>");
                    return true;
                }

                String folderName = null;
                Connection con = MySQL.getConnect();
                if (con != null){
                    try {
                        PreparedStatement statement = con.prepareStatement("SELECT * FROM `MapList` WHERE WorldName = ? AND Active = 1");
                        statement.setString(1, args[0]);
                        ResultSet set = statement.executeQuery();
                        if (set.next()){
                            folderName = set.getString("FolderName");
                        }
                        set.close();
                        statement.close();

                        if (folderName != null){
                            PreparedStatement statement1 = con.prepareStatement("UPDATE `MapList` SET `Active` = 0 WHERE WorldName = ?");
                            statement1.setString(1, args[0]);
                            statement1.execute();
                            statement1.close();
                        }
                    } catch (SQLException ex){
                        ex.printStackTrace();
                    }
                    MySQL.disconnectConnect(con);
                }

                if (folderName == null){
                    player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"ワールドが存在しません。 (The world does not exist.)");
                    return true;
                }

                if (player.getLocation().getWorld().getName().equals(folderName)){
                    plugin.getLogger().info("プレーヤーを移動");
                    player.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
                }

                for (World world : plugin.getServer().getWorlds()){
                    if (world.getName().equals(folderName)){
                        for (Player player1 : plugin.getServer().getOnlinePlayers()){
                            if (player1.getLocation().getWorld().getName().equals(world.getName())){
                                player1.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
                                player1.sendMessage("今いるワールドは削除されるため初期ワールドへ転送されました。 (The world you are in is being deleted and you have been transferred to the initial world.)");
                                plugin.getLogger().info("ワールド移動 : "+player1.getName());
                            }
                        }

                        plugin.getLogger().info("ワールドアンロード");
                        plugin.getServer().unloadWorld(world, true);
                        break;
                    }
                }


                player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"ワールドを削除しました。 (Deleted the world.)");
            }
        }
        return true;
    }
}
