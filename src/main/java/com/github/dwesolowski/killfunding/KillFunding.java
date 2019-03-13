package com.github.dwesolowski.killfunding;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class KillFunding extends JavaPlugin {
    static Economy economy;

    static {
        KillFunding.economy = null;
    }

    @Override
    public void onEnable() {
        registerConfig();
        registerEvents();
        registerEconomy();
        registerCommands();
        registerMetrics();
    }

    private void registerConfig() {
        saveDefaultConfig();
        saveConfig();
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerKill(this), this);
    }

    private void registerEconomy() {
        if (!this.setupEconomy()) {
            this.getLogger().severe("Plugin was disabled because an economy system was not found!");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    private boolean setupEconomy() {
        final RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            KillFunding.economy = economyProvider.getProvider();
        }
        return KillFunding.economy != null;
    }

    private void registerCommands() {
        this.getCommand("killfund").setExecutor(new CommandReload(this));
    }

    private void registerMetrics() {
        final MetricsLite metrics = new MetricsLite(this);
    }
}