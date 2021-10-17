package xyz.n7mn.dev.nanamimapsystem.command;

import org.apache.commons.codec.digest.DigestUtils;
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

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class WorldCreate implements CommandExecutor {

    private final Plugin plugin;
    public WorldCreate(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){
            Player player = (Player) sender;

            if (!player.isOp() || !player.hasPermission("nanamimap.create")){
                return true;
            }

            if (args.length < 1 || args.length > 3){
                player.sendMessage("" +
                        "「/create <名前> <ワールドタイプ> <seed>」と入力してください。\n" +
                        "Enter \"/create <name> <world type> <seed>\".\n" +
                        "\n" +
                        "例 (Example) :\n" +
                        "/create testWorld\n" +
                        "/create testworld nether\n" +
                        "/create testWorld"
                );
                return true;
            }


            String worldName = args[0];
            String folderName = "";
            String worldType = "normal";

            player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"現在ワールド生成中... (World generation in progress.)");
            if (worldName.getBytes(StandardCharsets.UTF_8).length == worldName.length()){
                if (worldName.length() <= 32){
                    folderName = worldName;
                } else {
                    folderName = worldName.substring(0, 32);
                }
            } else {
                String s = DigestUtils.sha512Hex(String.valueOf(new SecureRandom().nextInt()));
                if (s.length() < 32){
                    folderName = s;
                } else {
                    folderName = s.substring(0, 32);
                }
            }
            WorldCreator worldCreator = WorldCreator.name(folderName);

            if (folderName.length() == 0){
                player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"なにか不具合が発生したようです。 (A glitch has occurred.)");
                return true;
            }

            if (args.length == 2 || args.length == 3){
                switch (args[1]) {
                    case "normal":
                        worldCreator.environment(World.Environment.NORMAL);
                        break;
                    case "nether":
                        worldCreator.environment(World.Environment.NETHER);
                        break;
                    case "end":
                        worldCreator.environment(World.Environment.THE_END);
                        break;
                    case "flat":
                        worldCreator.environment(World.Environment.NORMAL);
                        worldCreator.type(WorldType.FLAT);
                        break;
                    default:
                        player.sendMessage(ChatColor.YELLOW + "" +
                                "[ななみ鯖] " + ChatColor.RESET + "ワールドタイプは以下のタイプのみ指定できます。(Only the following types of world types can be specified.)\n" +
                                "normal -- 通常ワールド (Normal World)\n" +
                                "nether -- ネザーワールド (Nether World)\n" +
                                "end    -- ジ・エンドワールド (The End World)\n" +
                                "flat   -- フラットワールド (Flat World)"
                        );
                        return true;
                }
            }

            try {
                if (args.length == 3){
                    worldCreator.seed(Long.parseLong(args[2]));
                }
            } catch (Exception e){
                // e.printStackTrace();
            }


            try {
                folderName = Pattern.compile("[(\\|/|:|\\*|?|\"|<|>|\\\\|)]").matcher(folderName).replaceAll("");
            } catch (Exception ex){
                ex.printStackTrace();
                player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"ワールド名がおかしいです。");
                return true;
            }
            plugin.getServer().createWorld(worldCreator);
            World world = plugin.getServer().getWorld(folderName);

            player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"ワールド生成完了！ (World generation complete!)");
            plugin.getLogger().info("ワールド名 : "+worldName+" / フォルダ名 : "+folderName);

            player.teleport(world.getSpawnLocation());
            player.sendMessage(ChatColor.YELLOW + "" +
                    "[ななみ鯖] "+ChatColor.RESET+"テレポートしました。\n" +
                    "Teleported to the world.\n" +
                    "削除するには「/delete "+worldName+"」と入力してください\n" +
                    "To delete, type \"/delete "+worldName+"\"."
            );

            String finalWorldName = worldName;
            String finalWorldType = worldType;
            String finalFolderName = folderName;

            new Thread(()->{
                try {
                    Connection con = MySQL.getConnect();
                    if (con != null){
                        PreparedStatement statement = con.prepareStatement("INSERT INTO `MapList`(`WorldUUID`, `WorldName`, `WorldType`, `FolderName`, `CreateUserUUID`, `CreateDate`, `LastLoadDate`, `IsComplete`, `Active`) VALUES (?,?,?,?,?,?,?,?,?)");
                        statement.setString(1, UUID.randomUUID().toString());
                        statement.setString(2, finalWorldName);
                        statement.setString(3, finalWorldType);
                        statement.setString(4, finalFolderName);
                        statement.setString(5, player.getUniqueId().toString());

                        Timestamp nowTime = new Timestamp(new Date().getTime());
                        statement.setTimestamp(6, nowTime);
                        statement.setTimestamp(7, nowTime);
                        statement.setBoolean(8, true);
                        statement.setBoolean(9, true);

                        statement.execute();
                        statement.close();
                    }

                    MySQL.disconnectConnect(con);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }).start();
        }


        return true;
    }
}
