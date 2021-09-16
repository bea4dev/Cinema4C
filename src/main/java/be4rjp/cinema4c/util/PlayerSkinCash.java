package be4rjp.cinema4c.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSkinCash {
    
    private static Map<String, String[]> skinCash = new ConcurrentHashMap<>();
    
    public static String[] getPlayerSkin(String uuid){
        String[] skin = skinCash.get(uuid);
        
        if(skin == null){
            TaskHandler.runAsync(() -> {
                String[] skin1 = SkinManager.getSkin(uuid);
                skinCash.put(uuid, skin1);
            });
        }
        
        return skin;
    }
}
