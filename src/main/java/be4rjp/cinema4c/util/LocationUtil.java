package be4rjp.cinema4c.util;

import org.bukkit.Location;

public class LocationUtil {
    
    /**
     * 二つの座標を安全に比較する
     * @param location1 比較する座標
     * @param location2 比較する座標
     * @return 二つの距離の二乗、ワールドが一致しない場合はdoubleの最大値が返ってくる
     */
    public static double distanceSquaredSafeDifferentWorld(Location location1, Location location2){
        if(location1.getWorld() != location2.getWorld()) return Double.MAX_VALUE;
        return location1.distanceSquared(location2);
    }
    
}
