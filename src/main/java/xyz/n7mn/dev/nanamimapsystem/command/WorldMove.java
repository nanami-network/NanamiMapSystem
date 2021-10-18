package xyz.n7mn.dev.nanamimapsystem.command;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.nanamimapsystem.util.MySQL;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WorldMove implements CommandExecutor {

    private final Plugin plugin;
    public WorldMove(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.isOp() || sender.hasPermission("nanamimap.move")){

            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length != 1){
                    player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"/move <WorldName>");
                    return true;
                }

                String folderName = null;
                Connection con = MySQL.getConnect();
                try {
                    PreparedStatement statement = con.prepareStatement("SELECT * FROM `MapList` WHERE `WorldName` = ? AND Active = 1");
                    statement.setString(1, args[0]);

                    ResultSet set = statement.executeQuery();
                    if (set.next()){
                        folderName = set.getString("FolderName");
                    }
                    set.close();
                    statement.close();
                    con.close();
                } catch (SQLException ex){
                    ex.printStackTrace();
                }


                for (World world : plugin.getServer().getWorlds()){
                    if (world.getName().equals(folderName)){
                        player.teleport(world.getSpawnLocation());
                        player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"ワールド「"+args[0]+"」へテレポートしました。 (Teleported to world \""+args[0]+"\".)");
                        return true;
                    }
                }

                player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"このワールドは現在ロードされていません。「/load "+args[0]+"」を使用してロードしてください。 (This world is currently not loaded. Please use \"/load "+args[0]+"\" to load it.)");
            }

        }

        return true;
    }
}
