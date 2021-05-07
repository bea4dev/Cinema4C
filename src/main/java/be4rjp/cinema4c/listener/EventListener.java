package be4rjp.cinema4c.listener;

import be4rjp.cinema4c.data.record.tracking.PlayerTrackData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
import be4rjp.cinema4c.recorder.RecordManager;
import be4rjp.cinema4c.recorder.SceneRecorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;

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
}
