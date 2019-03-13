package com.github.dwesolowski.killfunding;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class PlayerKill implements Listener {

    private final KillFunding plugin;

    PlayerKill(final KillFunding pl) {
        this.plugin = pl;
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
        final String prefix = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("messages.prefix"));
        final Entity victim = e.getEntity();
        if (victim instanceof Player) {
            final Entity killer = e.getEntity().getKiller();
            if (killer != null) {
                double accountCredits = this.plugin.getConfig().getInt("options.default-amount");
                for (final String s : this.plugin.getConfig().getConfigurationSection("groups").getKeys(false)) {
                    if (!killer.isOp() && killer.hasPermission(this.plugin.getConfig(). getString("groups." + s + ".permission"))) {
                        accountCredits = this.plugin.getConfig().getInt("groups." + s + ".amount");
                        break;
                    }
                }
                double vBalance = KillFunding.economy.getBalance((OfflinePlayer) victim);
                if (this.plugin.getConfig().getString("options.enable-percentage").equals("true")) {
                    accountCredits = vBalance * accountCredits / 100;
                }
                if (vBalance > 0) {
                    boolean vHasBalance = true;
                    if (vBalance <= accountCredits) {
                        accountCredits = vBalance;
                        vHasBalance = false;
                    }
                    if (!victim.getName().equals(killer.getName())) {
                        final EconomyResponse takeMoney = KillFunding.economy.withdrawPlayer((OfflinePlayer) victim, accountCredits);
                        if (takeMoney.transactionSuccess() && accountCredits != 0) {
                            final String money = String.format("%,.2f", accountCredits);
                            if (this.plugin.getConfig().getString("options.enable-prefix").equals("true")) {
                                victim.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.plugin.getConfig().getString("messages.death-message").replace("{killer}", victim.getName()).replace("{killer}", killer.getName()).replace("{lost-money}", money)));
                            } else {
                                victim.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("messages.death-message").replace("{killer}", victim.getName()).replace("{killer}", killer.getName()).replace("{lost-money}", money)));
                            }
                        }
                    }
                    if (!killer.getName().equals(victim.getName())) {
                        final EconomyResponse giveMoney = KillFunding.economy.depositPlayer((OfflinePlayer) killer, accountCredits);
                        if (giveMoney.transactionSuccess()) {
                            final String money = String.format("%,.2f", accountCredits);
                            if (vHasBalance) {
                                if (this.plugin.getConfig().getString("options.enable-prefix").equals("true")) {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.plugin.getConfig().getString("messages.kill-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                } else {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("messages.kill-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                }
                            } else {
                                if (this.plugin.getConfig().getString("options.enable-prefix").equals("true")) {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.plugin.getConfig().getString("messages.kill-low-balance-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                } else {
                                    killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("kill-low-balance-message").replace("{victim}", victim.getName()).replace("{killer}", killer.getName()).replace("{reward-money}", money)));
                                }
                            }
                        }
                    }
                } else {
                    if (this.plugin.getConfig().getString("options.enable-prefix").equals("true")) {
                        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + this.plugin.getConfig().getString("messages.kill-no-balance-message").replace("{victim}", victim.getName())));
                    } else {
                        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("kill-no-balance-message").replace("{victim}", victim.getName())));
                    }
                }
            }
        }
    }
}