package be4rjp.cinema4c.listener;

import be4rjp.cinema4c.data.record.tracking.PlayerTrackData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
import be4rjp.cinema4c.recorder.RecordManager;
import be4rjp.cinema4c.recorder.SceneRecorder;
import be4rjp.cinema4c.util.AsyncPlayerManager;
import be4rjp.cinema4c.util.PlayerSkinCash;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerClick(PlayerAnimationEvent event){
        Player player = event.getPlayer();
        
        SceneRecorder sceneRecorder = RecordManager.getRecorder();
        
        if(sceneRecorder == null) return;
        if(sceneRecorder.isCancelled()) return;
        
        for(TrackData trackData : sceneRecorder.getRecordData().getTrackData()){
            if(trackData instanceof PlayerTrackData){
                PlayerTrackData playerTrackData = (PlayerTrackData) trackData;
                if(playerTrackData.getActor() == null) continue;
                if(playerTrackData.getActor() != player) continue;
                
                playerTrackData.setSwing(sceneRecorder.getTick());
            }
        }
    }
    
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        AsyncPlayerManager.getPlayers().add(player);
    
        PlayerSkinCash.getPlayerSkin(player.getUniqueId().toString());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        AsyncPlayerManager.getPlayers().add(player);
    }
}
