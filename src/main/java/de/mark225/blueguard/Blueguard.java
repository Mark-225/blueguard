package de.mark225.blueguard;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.mark225.blueguard.bluemap.BlueMapIntegration;
import de.mark225.blueguard.tasks.ResyncTask;
import de.mark225.blueguard.worldguard.WorldGuardIntegration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Blueguard extends JavaPlugin {
    public static Blueguard instance = null;
    private BlueMapIntegration bluemap  = null;
    private WorldGuardIntegration worldguard = null;
    private ResyncTask resyncTask = null;

    private boolean wgInit = false;

    @Override
    public void onLoad(){
        worldguard = new WorldGuardIntegration();
        wgInit = worldguard.init();
    }

    @Override
    public void onEnable(){
        instance = this;
        setupConfig();
        BlueMapAPI.registerListener(bluemap = new BlueMapIntegration());
        if(wgInit){
            resyncTask = new ResyncTask();
        }else{
            getLogger().severe("Worldguard integration couldn't initialise! Blueguard will not work!");
        }
    }

    public void startTask(){
        if(resyncTask != null)
            resyncTask.runTaskTimer(this, 0, BlueGuardConfig.syncInterval());
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
