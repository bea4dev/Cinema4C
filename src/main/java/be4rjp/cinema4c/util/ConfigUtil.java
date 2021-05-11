package be4rjp.cinema4c.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ConfigUtil {
    /**
     * World,X,Y,Z,Yaw,Pitchの形式からLocationを返す
     * @param locString
     * @return
     */
    public static Location toLocation(String locString){
        
        locString = locString.replace(" ", "");
        
        String[] args = locString.split(",");
        
        if(args.length != 6) return null;
        
        World world = Bukkit.getWorld(args[0]);
        double x = Double.parseDouble(args[1]);
        double y = Double.parseDouble(args[2]);
        double z = Double.parseDouble(args[3]);
        float yaw = Float.parseFloat(args[4]);
        float pitch = Float.parseFloat(args[5]);
    
        return new Location(world, x, y, z, yaw, pitch);
    }
}
