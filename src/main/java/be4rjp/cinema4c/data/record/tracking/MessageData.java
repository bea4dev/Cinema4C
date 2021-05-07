package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import be4rjp.cinema4c.util.Title;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * タイトルとチャットメッセージの録画データ
 */
public class MessageData implements TrackData{
    
    private final Map<Integer, String> chatMap = new HashMap<>();
    private final Map<Integer, Title> titleMap = new HashMap<>();
    
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
        if(chatMap.keySet().contains(tick)){
            String message = chatMap.get(tick);
            for(Player audience : scenePlayer.getAudiences()){
                audience.sendMessage(message);
            }
        }
        
        if(titleMap.keySet().contains(tick)){
            Title title = titleMap.get(tick);
            for(Player audience : scenePlayer.getAudiences()){
                title.sendTitle(audience);
            }
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
        return DataType.MESSAGE;
    }
    
    @Override
    public void write(FileConfiguration yml, String root) {
        if(yml.contains(root)){
            yml.set(root, null);
        }
    
        yml.set(root + ".type", this.getDataType().toString());
    
        //messages  ([index] message)
        List<String> messages = new ArrayList<>();
        for(Map.Entry<Integer, String> entry : this.chatMap.entrySet()){
            int index = entry.getKey();
            String message = entry.getValue();
            
            String line = "[" + index + "] " + message;
            messages.add(line);
        }
        yml.set(root + ".message", messages);
    
        //titles ([index] title/subTitle/fadeIn/stay/fadeOut)
        List<String> titles = new ArrayList<>();
        for(Map.Entry<Integer, Title> entry : this.titleMap.entrySet()){
            int index = entry.getKey();
            Title title = entry.getValue();
            
            String line = "[" + index + "] " + title.getTitle() + "/" + title.getSubTitle() + "/" + title.getFadeIn() + "/" + title.getStay() + "/" + title.getFadeOut();
            titles.add(line);
        }
        yml.set(root + ".title", titles);
    }
    
    @Override
    public void load(FileConfiguration yml, String root) {
        //messages  ([index] message)
        List<String> messages = yml.getStringList(root + ".message");
        for(String line : messages) {
            line = line.replace("[", "");
            line = line.replaceFirst(" ", "");
    
            String[] indexMessage = line.split("]");
            int index = Integer.parseInt(indexMessage[0]);
            String message = ChatColor.translateAlternateColorCodes('&', indexMessage[1]);
            this.chatMap.put(index, message);
            
            if(index > endTick){
                endTick = index;
            }
        }
        
        //titles ([index] title/subTitle/fadeIn/stay/fadeOut)
        List<String> titles = yml.getStringList(root + ".title");
        for(String line : titles){
            line = line.replace("[", "");
            line = line.replaceFirst(" ", "");
            
            String[] indexTitle = line.split("]");
            int index = Integer.parseInt(indexTitle[0]);
            
            String[] titleArgs = indexTitle[1].split("/");
            
            String title = ChatColor.translateAlternateColorCodes('&', titleArgs[0]);
            String subTitle = ChatColor.translateAlternateColorCodes('&', titleArgs[1]);
            int fadeIn = Integer.parseInt(titleArgs[2]);
            int stay = Integer.parseInt(titleArgs[3]);
            int fadeOut = Integer.parseInt(titleArgs[4]);
            
            Title titleObject = new Title(title, subTitle, fadeIn, stay, fadeOut);
            this.titleMap.put(index, titleObject);
    
            if(index > endTick){
                endTick = index;
            }
        }
    }
}
