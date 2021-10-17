package xyz.n7mn.dev.nanamimapsystem.command;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
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
import java.util.UUID;

public class WorldLoad implements CommandExecutor {

    private final Plugin plugin;

    public WorldLoad(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.isOp() || !player.hasPermission("nanamimap.load")) {
                return true;
            }

            Connection connect = MySQL.getConnect();
            if (connect == null){
                return true;
            }

            if (args.length != 1){
                player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"/load <WorldName>");
                return true;
            }

            String name = null;
            String type = null;
            UUID user = null;
            try {
                PreparedStatement statement = connect.prepareStatement("SELECT * FROM `MapList` WHERE `WorldName` = ? AND Active = 1");
                statement.setString(1, args[0]);
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    name = set.getString("FolderName");
                    type = set.getString("WorldType");
                    user = UUID.fromString(set.getString("CreateUserUUID"));
                }
                set.close();
                statement.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }
            MySQL.disconnectConnect(connect);

            if (name == null){
                player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"ワールド名が存在しません。 (The world name does not exist.)");
                return true;
            }

            if (user != null && !player.getUniqueId().equals(user)){
                player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"そのワールドは別の人のワールドです。 (That world is another person's world.)");
                return true;
            }

            for (World world : plugin.getServer().getWorlds()){
                if (world.getName().equals(name)){
                    player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"そのワールドはロード済みです。「/move」を使用して移動してください。 (The world is already loaded. Please use \"/move\" to move it.)");
                    return true;
                }
            }

            WorldCreator creator = WorldCreator.name(name);
            switch (type) {
                case "normal":
                    creator.environment(World.Environment.NORMAL);
                    break;
                case "nether":
                    creator.environment(World.Environment.NETHER);
                    break;
                case "end":
                    creator.environment(World.Environment.THE_END);
                    break;
                case "flat":
                    creator.environment(World.Environment.NORMAL);
                    creator.type(WorldType.FLAT);
                    break;
                default:
                    creator.environment(World.Environment.NORMAL);
            }
            plugin.getServer().createWorld(creator);

            World world = plugin.getServer().getWorld(name);
            player.teleport(world.getSpawnLocation());
            player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+ChatColor.RESET+"ワールドをロードしてテレポートしました。 (The world name does not exist.)");
        }

        return true;
    }
}
