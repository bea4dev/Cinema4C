package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.bridge.Cinema4CBridge;
import be4rjp.cinema4c.bridge.PluginBridge;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionsData implements TrackData{
    
    private final Map<Integer, String[]> commandMap = new HashMap<>();
    
    private int endTick = 0;
    
    @Override
    public void record(SceneRecorder sceneRecorder, int tick) {
        //None
    }
    
    @Override
    public void recordEnd() {
        //None
    }
    
    @Override
    public void play(ScenePlayer scenePlayer, int tick) {
        if(commandMap.keySet().contains(tick)){
            String[] args = commandMap.get(tick);
            String pluginName = args[0];
            String command = args[1];
    
            PluginBridge pluginBridge = Cinema4CBridge.getPluginBridge(pluginName);
            
            if(pluginBridge == null){
                Cinema4C.getPlugin().getLogger().warning("Extension plugin '" + pluginName + "' was not found.");
                return;
            }
            pluginBridge.executeCommand(scenePlayer, command);
        }
    }
    
    @Override
    public void playInitialize(ScenePlayer scenePlayer) {
        //None
    }
    
    @Override
    public int getEndTick() {
        return endTick;
    }
    
    @Override
    public void setEndTick(int endTick) {
        this.endTick = endTick;
    }
    
    @Override
    public void playEnd(ScenePlayer scenePlayer) {
        //None
    }
    
    @Override
    public DataType getDataType() {
        return DataType.EXTENSIONS;
    }
    
    @Override
    public void write(FileConfiguration yml, String root) {
        if(yml.contains(root)){
            yml.set(root, null);
        }
    
        yml.set(root + ".type", this.getDataType().toString());
    
        //commands  ([index] <plugin-name>command)
        List<String> commands = new ArrayList<>();
        for(Map.Entry<Integer, String[]> entry : this.commandMap.entrySet()){
            int index = entry.getKey();
            String[] args = entry.getValue();
        
            String line = "[" + index + "] <" + args[0] + ">" + args[1];
            commands.add(line);
        }
        yml.set(root + ".command", commands);
    }
    
    @Override
    public void load(FileConfiguration yml, String root) {
        //commands  ([index] <plugin-name>command)
        List<String> commands = yml.getStringList(root + ".command");
        for(String line : commands) {
            line = line.replace("[", "");
            line = line.replace("<", "");
            line = line.replaceFirst(" ", "");
        
            String[] indexLine = line.split("]");
            int index = Integer.parseInt(indexLine[0]);
            String[] args = indexLine[1].split(">");
            if(args.length < 2){
                Cinema4C.getPlugin().getLogger().warning("Syntax error. -> " + indexLine[1]);
                continue;
            }
            this.commandMap.put(index, args);
        
            if(index > endTick){
                endTick = index;
            }
        }
    }
}
