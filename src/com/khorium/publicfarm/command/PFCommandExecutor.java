package com.khorium.publicfarm.command;

import com.khorium.publicfarm.Utils.LogUtil;
import com.khorium.publicfarm.Utils.MsgUtil;
import com.khorium.publicfarm.PublicFarm;
import com.khorium.publicfarm.database.DatabaseHelper;
import com.khorium.publicfarm.farm.Farm;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Created by Khorium on 2016/8/8.
 */
public class PFCommandExecutor implements CommandExecutor {

    private PublicFarm publicFarm;

    public PFCommandExecutor(PublicFarm publicFarm) {
        this.publicFarm = publicFarm;
    }

    public boolean onCommand(CommandSender commandSender, Command command,
                             String s, String[] strings) {

        if (strings.length <= 0) {
            sendHelpMessage(commandSender);
            return true;
        }


        PFCommand pfCommand = PFCommand.getCommand(strings[0]);

        switch (pfCommand) {
            case SET:
                set(commandSender, strings);
                return true;
            case LIST:
                list(commandSender, strings);
                return true;
            case DELETE:
                delete(commandSender, strings);
                return true;
            case RELOAD:
                reload(commandSender, strings);
            case NOVALUE:
                sendHelpMessage(commandSender);
                return true;
            default://impossible
                sendHelpMessage(commandSender);
                return true;
        }
    }
    private void set(CommandSender commandSender, String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(MsgUtil.getMessage("need-set-in-game"));
            return;
        }

        if (!commandSender.hasPermission("publicfarm.manage.set")) {
            commandSender.sendMessage(MsgUtil.getMessage
                    ("need-set-permission"));
            return;
        }

        if (strings.length != 3 || (!strings[2].equals("CARROT") &&
                !strings[2].equals("WHEAT") && !strings[2].equals
                ("NETHER_WARTS") && !strings[2].equals("POTATO"))) {
            commandSender.sendMessage(MsgUtil.getMessage("set-usage"));
            return;
        }

        for (Object o : DatabaseHelper.getAllFarms().entrySet()) {
            Farm farm = (Farm) ((Map.Entry) o).getValue();
            if (strings[1].equals(farm.getName())) {
                commandSender.sendMessage(MsgUtil.getMessage
                        ("duplicate-name"));
                return;
            }
        }


        LogUtil.logInfo("register Listener");
        LogUtil.logInfo(strings[1] + "," + strings[2]);
        publicFarm.startSingleListener(PFCommand.SET, strings[1], Material
                .getMaterial(strings[2]), ((Player) commandSender)
                .getUniqueId());
        commandSender.sendMessage(MsgUtil.getMessage("set-first-location"));
    }

    private void list(CommandSender commandSender, String[] strings) {

        if (!commandSender.hasPermission("publicfarm.manage.list")) {
            commandSender.sendMessage(MsgUtil.getMessage
                    ("need-list-permission"));
            return;
        }

        if (strings.length != 1) {
            commandSender.sendMessage(MsgUtil.getMessage("list-usage"));
            return;
        }


        publicFarm.getManager().listPublicFarm(commandSender);
    }

    private void delete(CommandSender commandSender, String[] strings) {

        if (!commandSender.hasPermission("publicfarm.manage.delete")) {
            commandSender.sendMessage(MsgUtil.getMessage
                    ("need-delete-permission"));
            return;
        }

        if (strings.length != 2) {
            commandSender.sendMessage(MsgUtil.getMessage("delete-usage"));
            return;
        }

        publicFarm.getManager().deletePublicFarm(commandSender, strings[1]);
    }

    private void sendHelpMessage(CommandSender commandSender) {

        if (commandSender.hasPermission("publicfarm.manage.set")) {
            commandSender.sendMessage(ChatColor.GREEN + "/pf set <农场名字> <作物类型> " +
                    ChatColor.YELLOW + " - " + MsgUtil.getMessage("command" +
                    ".description.set"));
        }
        if (commandSender.hasPermission("publicfarm.manage.list")) {
            commandSender.sendMessage(ChatColor.GREEN + "/pf list " +
                    ChatColor.YELLOW + " - " + MsgUtil.getMessage("command" +
                    ".description.list"));
        }
        if (commandSender.hasPermission("publicfarm.manage.delete")) {
            commandSender.sendMessage(ChatColor.GREEN + "/pf delete <农场名字> "
                    + ChatColor.YELLOW + " - " + MsgUtil.getMessage("command" +
                    ".description.delete"));
        }
    }

    private void reload(CommandSender commandSender, String[] strings) {

        if (!commandSender.hasPermission("publicfarm.reload")) {
            commandSender.sendMessage(MsgUtil.getMessage
                    ("need-reload-permission"));
            return;
        }

        if (strings.length != 1) {
            commandSender.sendMessage(MsgUtil.getMessage("reload-usage"));
            return;
        }

        publicFarm.reloadConfig();
    }

}
