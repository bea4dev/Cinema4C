package be4rjp.cinema4c.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CSound {
    
    private final Vector relative;
    private final Sound sound;
    private final float volume;
    private final float pitch;
    
    public CSound(Vector relative, Sound sound, float volume, float pitch){
        this.relative = relative;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public void play(Player player, Location baseLocation){
        player.playSound(baseLocation.clone().add(relative), sound, volume, pitch);
    }
    
    public Sound getSound() {return sound;}
    
    public float getPitch() {return pitch;}
    
    public float getVolume() {return volume;}
    
    public Vector getRelative() {return relative;}
}
