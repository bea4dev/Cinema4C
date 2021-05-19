package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import be4rjp.cinema4c.util.CParticle;
import be4rjp.cinema4c.util.CSound;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectData implements TrackData{
    
    private Map<Integer, CSound> soundMap = new HashMap<>();
    private Map<Integer, CParticle> particleMap = new HashMap<>();
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
        if(soundMap.containsKey(tick)){
            CSound cSound = soundMap.get(tick);
            scenePlayer.getAudiences().forEach(audience -> cSound.play(audience, scenePlayer.getBaseLocation()));
        }
    
        if(particleMap.containsKey(tick)){
            CParticle cParticle = particleMap.get(tick);
            scenePlayer.getAudiences().forEach(audience -> cParticle.play(audience, scenePlayer.getBaseLocation()));
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
        return DataType.EFFECT;
    }
    
    @Override
    public void write(FileConfiguration yml, String root) {
        if(yml.contains(root)){
            yml.set(root, null);
        }
    
        yml.set(root + ".type", this.getDataType().toString());
        
        //sound ([index] sound/volume/pitch/x, y, z)
        List<String> sounds = new ArrayList<>();
        for(Map.Entry<Integer, CSound> entry : soundMap.entrySet()){
            int index = entry.getKey();
            CSound cSound = entry.getValue();
    
            Sound sound = cSound.getSound();
            float volume = cSound.getVolume();
            float pitch = cSound.getPitch();
            Vector relative = cSound.getRelative();
            
            String line = "[" + index + "] " + sound + "/" + volume + "/" + pitch + "/" + relative.getX() + ", " + relative.getY() + ", " + relative.getZ();
            sounds.add(line);
        }
        yml.set(root + ".sound", sounds);
    
        
        //particle ([index] particle/count/x, y, z)
        List<String> particles = new ArrayList<>();
        for(Map.Entry<Integer, CParticle> entry : particleMap.entrySet()){
            int index = entry.getKey();
            CParticle cParticle = entry.getValue();
    
            Particle particle = cParticle.getParticle();
            int count = cParticle.getCount();
            Vector relative = cParticle.getRelative();
        
            String line = "[" + index + "] " + particle + "/" + count + "/" + relative.getX() + ", " + relative.getY() + ", " + relative.getZ();
            particles.add(line);
        }
        yml.set(root + ".particle", particles);
    }
    
    @Override
    public void load(FileConfiguration yml, String root) {
        //sound ([index] sound/volume/pitch/x, y, z)
        List<String> sounds = yml.getStringList(root + ".sound");
        for(String line : sounds){
            line = line.replace("[", "");
            line = line.replaceFirst(" ", "");
    
            String[] indexSound = line.split("]");
            int index = Integer.parseInt(indexSound[0]);
            
            String[] args = indexSound[1].split("/");
            Sound sound = Sound.valueOf(args[0]);
            float volume = Float.parseFloat(args[1]);
            float pitch = Float.parseFloat(args[2]);
            
            String[] vecArgs = args[3].split(",");
            double x = Double.parseDouble(vecArgs[0]);
            double y = Double.parseDouble(vecArgs[1]);
            double z = Double.parseDouble(vecArgs[2]);
            Vector relative = new Vector(x, y, z);
            
            CSound cSound = new CSound(relative, sound, volume, pitch);
            soundMap.put(index, cSound);
    
            if(index > endTick){
                endTick = index;
            }
        }
    
    
        //particle ([index] particle/count/x, y, z)
        List<String> particles = yml.getStringList(root + ".particle");
        for(String line : particles){
            line = line.replace("[", "");
            line = line.replaceFirst(" ", "");
        
            String[] indexParticle = line.split("]");
            int index = Integer.parseInt(indexParticle[0]);
        
            String[] args = indexParticle[1].split("/");
            Particle particle = Particle.valueOf(args[0]);
            int count = Integer.parseInt(args[1]);
        
            String[] vecArgs = args[2].split(",");
            double x = Double.parseDouble(vecArgs[0]);
            double y = Double.parseDouble(vecArgs[1]);
            double z = Double.parseDouble(vecArgs[2]);
            Vector relative = new Vector(x, y, z);
        
            CParticle cParticle = new CParticle(relative, particle, count);
            particleMap.put(index, cParticle);
        
            if(index > endTick){
                endTick = index;
            }
        }
    }
}
