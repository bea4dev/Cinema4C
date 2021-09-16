package be4rjp.cinema4c.util;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncPlayerManager {
    private static final Set<Player> onlinePlayers = ConcurrentHashMap.newKeySet();
    
    public static void addPlayer(Player player) {onlinePlayers.add(player);}
    
    public static void removePlayer(Player player) {onlinePlayers.remove(player);}
    
    public static Set<Player> getPlayers() {return new HashSet<>(onlinePlayers);}
}
