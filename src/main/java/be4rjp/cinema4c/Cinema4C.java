package be4rjp.cinema4c;

import be4rjp.cinema4c.command.c4cCommandExecutor;
import be4rjp.cinema4c.listener.EventListener;
import be4rjp.cinema4c.player.PlayManager;
import be4rjp.cinema4c.recorder.RecordManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Cinema4C extends JavaPlugin {
    
    private static Cinema4C plugin;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
    
        //Register event listeners
        getLogger().info("Registering event listeners...");
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(), this);
        
        loadData();
    
        //Register command executors
        getLogger().info("Registering command executors...");
        getCommand("c4c").setExecutor(new c4cCommandExecutor());
        getCommand("c4c").setTabCompleter(new c4cCommandExecutor());
        
        getLogger().info(ChatColor.GREEN + "Complete!");
    }
    
    public static void loadData(){
        RecordManager.loadAllRecordData();
        PlayManager.loadAllMovieData();
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    
    public static Cinema4C getPlugin(){return plugin;}
}
