package be4rjp.cinema4c.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class C4CLocation {
    
    private String world;
    
    private double x;
    
    private double y;
    
    private double z;
    
    public C4CLocation(String world, double x, double y, double z){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    
    public Location getBukkitLocation(){
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
