package be4rjp.cinema4c.util;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncPlayerManager {
    private static final Set<Player> players = ConcurrentHashMap.newKeySet();
    
    public static Set<Player> getPlayers() {return players;}
}
