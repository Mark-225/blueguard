package de.mark225.blueguard;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.mark225.blueguard.bluemap.BlueMapIntegration;
import de.mark225.blueguard.tasks.ResyncTask;
import de.mark225.blueguard.worldguard.WorldGuardIntegration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;

public class Blueguard extends JavaPlugin {
    public static Blueguard instance = null;
    private BlueMapIntegration bluemap  = null;
    private WorldGuardIntegration worldguard = null;
    private ResyncTask resyncTask = null;
    private boolean init = false;

    private boolean wgInit = false;

    @Override
    public void onLoad(){
        setupConfig();
        worldguard = new WorldGuardIntegration();
        wgInit = worldguard.init();
    }

    @Override
    public void onEnable(){
        instance = this;
        BlueMapAPI.registerListener(bluemap = new BlueMapIntegration());
        if(wgInit){
            init = true;
        }else{
            getLogger().severe("Worldguard integration couldn't initialise! Blueguard will not work!");
        }
    }

    public void stopTask(){
        if(resyncTask != null) {
            Bukkit.getScheduler().cancelTask(resyncTask.getTaskId());
            resyncTask = null;
        }
    }

    public void startTask(){
        if(resyncTask == null) {
            resyncTask = new ResyncTask();
            resyncTask.runTaskTimer(this, 0, BlueGuardConfig.syncInterval());
        }
    }

    public BlueMapIntegration getBluemapIntegration(){
        return bluemap;
    }

    public WorldGuardIntegration getWorldguardIntegration(){
        return worldguard;
    }

    private void setupConfig(){
        File f = new File(this.getDataFolder(), "config.yml");
        if(!f.exists()){
            this.saveDefaultConfig();
        }
        FileConfiguration config = this.getConfig();
        BlueGuardConfig.setConfig(config);
    }




}
