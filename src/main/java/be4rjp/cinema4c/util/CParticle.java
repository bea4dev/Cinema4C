package be4rjp.cinema4c.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CParticle {
    
    private final Vector relative;
    private final Particle particle;
    private final int count;
    
    public CParticle(Vector relative, Particle particle, int count){
        this.relative = relative;
        this.particle = particle;
        this.count = count;
    }
    
    public void play(Player player, Location baseLocation){
        player.spawnParticle(particle, baseLocation.clone().add(relative), count);
    }
    
    public Particle getParticle() {return particle;}
    
    public int getCount() {return count;}
    
    public Vector getRelative() {return relative;}
}
