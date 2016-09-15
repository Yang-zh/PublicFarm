package com.khorium.publicfarm;

import com.khorium.publicfarm.Utils.LogUtil;
import com.khorium.publicfarm.command.PFCommand;
import com.khorium.publicfarm.command.PFCommandExecutor;
import com.khorium.publicfarm.database.*;
import com.khorium.publicfarm.farm.PFManager;
import com.khorium.publicfarm.listener.FarmProtectListener;
import com.khorium.publicfarm.Utils.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Khorium on 2016/8/8.
 */
public class PublicFarm extends JavaPlugin {

    public static PublicFarm instance;

    private PFManager manager;
    private PFCommandExecutor executor;
    private FarmProtectListener farmProtectListener;

    public int harvestLimitPerDay;
    public int today;
    public boolean protectFarm;
    private Database database;

    @Override
    public void onEnable() {
        getLogger().info("publicfarm initializing");
        if (!initPublicFarm()) {
            return;
        }
        registerListener();
    }

    private boolean initPublicFarm() {

        instance = this;

        //create/save config and load config
        saveDefaultConfig();
        reloadConfig();

        //set CommandExecutor
        getCommand("pf").setExecutor(new PFCommandExecutor(this));

        //create farmManager
        manager = new PFManager(this);

        //init database
        if (!initDataBase()) {
            return false;
        }
        initLimitWatcher();

        LogUtil.logInfo("preparing message");
        //load Messages
        MsgUtil.loadCfgMessages(instance);
        return true;
    }

    private void initLimitWatcher() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TimerTask() {
            @Override
            public void run() {
                int newDay = new Date(System.currentTimeMillis()).getDay();
                if (newDay != today) {
                    today = newDay;
                    farmProtectListener.clearLimits();
                }
            }
        },0,1000);
    }

    private boolean initDataBase() {
        try {
            ConfigurationSection dbCfg = getConfig().getConfigurationSection
                    ("database");
            DatabaseCore dbCore;
            if (dbCfg.getBoolean("mysql")) {
                // MySQL database - Required database be created first.
                String user = dbCfg.getString("user");
                String pass = dbCfg.getString("password");
                String host = dbCfg.getString("host");
                String port = dbCfg.getString("port");
                String database = dbCfg.getString("database");
                dbCore = new MySQLCore(host, user, pass, database, port);
            } else {
                // SQLite database - Doing this handles file creation
                dbCore = new SQLiteCore(new File(this.getDataFolder(), "farms" +
                        ".db"));
            }
            this.database = new Database(dbCore);
            // Make the database up to date
            DatabaseHelper.setup(getDB());
        } catch (Database.ConnectionException e) {
            e.printStackTrace();
            getLogger().severe("Error connecting to database. Aborting plugin" +
                    " load.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        } catch (SQLException e) {
//            e.printStackTrace();
            getLogger().severe(ChatColor.RED+"Error setting up database. Aborting plugin " +
                    "load.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.harvestLimitPerDay = getConfig().getInt("harvest-limit-per-day");
        this.protectFarm = getConfig().getBoolean("protect-farm");
    }

    private void registerListener() {
        farmProtectListener = new FarmProtectListener(this);
        getServer().getPluginManager().registerEvents(farmProtectListener,
                this);
    }


    public void startSingleListener(PFCommand command, String name, Material
            farmType, UUID uuid) {
        switch (command) {
            case SET:
                farmProtectListener.startSetListener(name, uuid, farmType);
        }
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
        this.reloadConfig();
    }

    public PFManager getManager() {
        return manager;
    }

    public PFCommandExecutor getExecutor() {
        return executor;
    }

    public Database getDB() {
        return this.database;
    }
}
