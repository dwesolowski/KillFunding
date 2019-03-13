package com.github.dwesolowski.killfunding;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandReload implements CommandExecutor {

    private final KillFunding plugin;

    CommandReload(final KillFunding plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        final String noPermission = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("messages.no-permission"));
        if (commandLabel.equalsIgnoreCase("killfund")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    final Player player = (Player) sender;
                    if (player.hasPermission("killfund.admin")) {
                        sender.sendMessage(ChatColor.YELLOW + "Basic Commands:");
                        sender.sendMessage(ChatColor.GREEN + "/killfund reload");
                    } else if (!player.hasPermission("killfund.admin")) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermission));
                    }
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Basic Commands:");
                    sender.sendMessage(ChatColor.GREEN + "/killfund reload");
                }
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        final Player player = (Player) sender;
                        if (player.hasPermission("killfund.reload")) {
                            sender.sendMessage(ChatColor.GREEN + "The plugin has been reloaded!");
                            this.plugin.reloadConfig();
                        } else if (!player.hasPermission("killfund.reload")) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermission));
                        }
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "The plugin has been reloaded!");
                        this.plugin.reloadConfig();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid arguments.");
                }
            } else if (args.length > 1) {
                sender.sendMessage(ChatColor.RED + "Invalid arguments.");
            }
        }
        return false;
    }
}
