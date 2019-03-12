package com.github.dwesolowski.killfunding;

import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import org.bukkit.event.entity.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import net.milkbowl.vault.economy.*;
import org.bukkit.event.*;
import org.bukkit.command.*;

public class KillFunding extends JavaPlugin implements Listener {
    private static Economy economy;

    static {
        KillFunding.economy = null;
    }

    @Override
    public void onEnable() {
        registerConfig();
        registerEvents();
        registerEconomy();
        registerMetrics();
    }

    private void registerConfig() {
        saveDefaultConfig();
        saveConfig();
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void registerEconomy() {
        if (!this.setupEconomy()) {
            this.getLogger().severe("Plugin was disabled because an economy system was not found!");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerMetrics() {
        final MetricsLite metrics = new MetricsLite(this);
    }

    private boolean setupEconomy() {
        final RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            KillFunding.economy = economyProvider.getProvider();
        }
        return KillFunding.economy != null;
    }

    public static boolean isInt(final String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @EventHandler
    public void playerDeath(final EntityDeathEvent e) {
        final String prefix = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.prefix"));
        final Entity victim = e.getEntity();
        if (victim instanceof Player) {
            final Entity killer = e.getEntity().getKiller();
            if (killer != null) {
                boolean kHasPermission = false;
                boolean vHasPermission = false;
                double accountCredits = 0;
                for (final String s : this.getConfig().getConfigurationSection("groups").getKeys(false)) {
                    if (victim.hasPermission(this.getConfig().getString("groups." + s + ".permission"))) {
                        accountCredits = this.getConfig().getInt("groups." + s + ".amount");
                        vHasPermission = true;
                        break;
                    }
                }
                for (final String s : this.getConfig().getConfigurationSection("groups").getKeys(false)) {
                    if (killer.hasPermission(this.getConfig().getString("groups." + s + ".permission"))) {
                        accountCredits = this.getConfig().getInt("groups." + s + ".amount");
                        kHasPermission = true;
                        break;
                    }
                }
                double vBalance = KillFunding.economy.getBalance((OfflinePlayer) victim);
                if (vBalance > 0) {
                    boolean vHasBalance = true;
                    if (vBalance <= accountCredits) {
                        accountCredits = vBalance;
                        vHasBalance = false;
                    }
                    if (vHasPermission && !victim.getName().equals(killer.getName())) {
                        final EconomyResponse takeMoney = KillFunding.economy.withdrawPlayer((OfflinePlayer) victim, accountCredits);
                        if (takeMoney.transactionSuccess() && accountCredits != 0) {
                            final String money = String.format ("%,.2f", accountCredits);
                            if (this.getConfig().getString("options.enable-prefix").equals("true")) {
                                victim.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.getConfig().getString("messages.death-message").replace("{killer}", victim.getName()).replace("{killer}", killer.getName()).replace("{lost-money}", money)));
                            } else {
                                victim.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.death-message").replace("{killer}", victim.getName()).replace("{killer}", killer.getName()).replace("{lost-money}", money)));
                            }
                        }
                    }
                    if (kHasPermission && !killer.getName().equals(victim.getName())) {
                        final EconomyResponse giveMoney = KillFunding.economy.depositPlayer((OfflinePlayer) killer, accountCredits);
                        if (giveMoney.transactionSuccess()) {
                            final String money = String.format ("%,.2f", accountCredits);
                            if (vHasBalance) {
                                if (this.getConfig().getString("options.enable-prefix").equals("true")) {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.getConfig().getString("messages.kill-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                } else {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.kill-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                }
                            } else {
                                if (this.getConfig().getString("options.enable-prefix").equals("true")) {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.getConfig().getString("messages.kill-low-balance-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                } else {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("kill-low-balance-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                }
                            }
                        }
                    }
                } else {
                    if (this.getConfig().getString("options.enable-prefix").equals("true")) {
                        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.getConfig().getString("messages.kill-no-balance-message").replace("{victim}", victim.getName())));
                    } else {
                        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("kill-no-balance-message").replace("{victim}", victim.getName())));
                    }
                }
            }
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        final String noPermission = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.no-permission"));
        if (commandLabel.equalsIgnoreCase("kfreload")) {
            if (sender instanceof Player) {
                final Player p = (Player) sender;
                if (p.hasPermission("killfund.reload")) {
                    sender.sendMessage(ChatColor.GREEN + "The plugin has been reloaded!");
                    this.reloadConfig();
                } else if (!p.hasPermission("killfund.reload")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermission));
                }
            } else {
                sender.sendMessage(ChatColor.GREEN + "The plugin has been reloaded!");
                this.reloadConfig();
            }
        }
        return false;
    }
}