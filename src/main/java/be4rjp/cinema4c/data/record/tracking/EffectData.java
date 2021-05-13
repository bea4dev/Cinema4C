package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import org.bukkit.configuration.file.FileConfiguration;

public class EffectData implements TrackData{
    @Override
    public void record(SceneRecorder sceneRecorder, int tick) {
    
    }
    
    @Override
    public void recordEnd() {
    
    }
    
    @Override
    public void play(ScenePlayer scenePlayer, int tick) {
    
    }
    
    @Override
    public void playInitialize(ScenePlayer scenePlayer) {
    
    }
    
    @Override
    public int getEndTick() {
        return 0;
    }
    
    @Override
    public void setEndTick(int endTick) {
    
    }
    
    @Override
    public void playEnd(ScenePlayer scenePlayer) {
    
    }
    
    @Override
    public DataType getDataType() {
        return null;
    }
    
    @Override
    public void write(FileConfiguration yml, String root) {
    
    }
    
    @Override
    public void load(FileConfiguration yml, String root) {
    
    }
}
