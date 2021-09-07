package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.nms.NMSUtil;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import be4rjp.cinema4c.util.IntegerRange;
import be4rjp.cinema4c.util.LocationUtil;
import be4rjp.cinema4c.util.SkinManager;
import be4rjp.cinema4c.util.Vec2f;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * NPCの録画データ
 */
public class PlayerTrackData implements TrackData{
    
    //トラッキングするプレイヤー
    private Player actor;
    //位置と方向のマップ
    private final Map<Integer, Vector> locationMap = new HashMap<>();
    private final Map<Integer, Vec2f> yawPitchMap = new HashMap<>();
    //プレイヤーのいる方向を向かせるtick
    private final Set<IntegerRange> playerLookTick = new HashSet<>();
    //スニーク状態になっているときのTick
    private final Set<Integer> sneak = new HashSet<>();
    //腕を降った時のTick
    private final Set<Integer> swing = new HashSet<>();
    //この録画データの最終Tick
    private int endTick = 0;
    //NPCのマップ keyはScenePlayerのID
    private final Map<Integer, Object> npcMap = new HashMap<>();
    //NPCのスキンデータ
    private String[] skin = null;
    //NPCの名前
    private String npcName = null;
    
    public PlayerTrackData(Player actor){
        this.actor = actor;
    }
    
    public PlayerTrackData(String[] skin, String name){
        this.skin = skin;
        this.npcName = name;
    }
    
    @Override
    public DataType getDataType() {
        return DataType.NPC;
    }
    
    public Player getActor() {return actor;}
    
    public Object getNPC(int playerID) {return npcMap.get(playerID);}
    
    public void setSwing(int tick){
        swing.add(tick);
    }
    
    public void setSneak(int tick){
        sneak.add(tick);
    }
    
    @Override
    public int getEndTick() {return endTick;}
    
    @Override
    public void setEndTick(int endTick) {this.endTick = endTick;}
    
    @Override
    public void recordEnd(){
        if(this.actor == null) return;
        
        this.npcName = actor.getName();
        this.skin = SkinManager.getSkin(actor.getUniqueId().toString());
        this.actor = null;
    }
    
    /**
     * NPCを作成
     * @param scenePlayer
     */
    public void initializeNPC(ScenePlayer scenePlayer){
        try {
            //NPCを作成してScenePlayerのIDと紐づけて保存
            Location baseLocation = scenePlayer.getBaseLocation();
            if (npcMap.get(scenePlayer.getID()) == null) {
                Object nmsServer = NMSUtil.getNMSServer(Bukkit.getServer());
                Object nmsWorld = NMSUtil.getNMSWorld(Objects.requireNonNull(baseLocation.getWorld()));
                String name = npcName == null ? actor.getName() : npcName;
                GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
                if (skin != null) {
                    gameProfile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));
                }
                npcMap.put(scenePlayer.getID(), NMSUtil.createEntityPlayer(nmsServer, nmsWorld, gameProfile));
            }
            //最初の再生位置を取得する
            if (locationMap.size() > 0) {
                for (int index = 0; index < endTick; index++) {
                    Vector location = locationMap.get(index);
                    if (location != null) {
                        Location loc = baseLocation.clone().add(location);
                        NMSUtil.setEntityPositionRotation(npcMap.get(scenePlayer.getID()), loc.getX(), loc.getY(), loc.getZ(), baseLocation.getYaw(), baseLocation.getPitch());
                        return;
                    }
                }
            }
            NMSUtil.setEntityPositionRotation(npcMap.get(scenePlayer.getID()), baseLocation.getX(), baseLocation.getY(), baseLocation.getZ(), baseLocation.getYaw(), baseLocation.getPitch());
        }catch (Exception e){e.printStackTrace();}
    }
    
    public void spawnNPC(ScenePlayer scenePlayer){
        try {
            Object npc = npcMap.get(scenePlayer.getID());
            Object infoPacket = NMSUtil.createPlayerInfoPacket("ADD_PLAYER", npc);
            Object spawnPacket = NMSUtil.createNamedEntitySpawnPacket(npc);
            Object dataWatcher = NMSUtil.getDataWatcher(npc);
            NMSUtil.setSkinOption(dataWatcher);
            Object metadata = NMSUtil.createEntityMetadataPacket(npc);
            Object look = NMSUtil.createEntityMoveLookPacket(NMSUtil.getEntityID(npc), 0F, 0F);
    
            for (Player player : scenePlayer.getAudiences()) {
                NMSUtil.sendPacket(player, infoPacket);
                NMSUtil.sendPacket(player, spawnPacket);
                NMSUtil.sendPacket(player, metadata);
                NMSUtil.sendPacket(player, look);
            }
        }catch (Exception e){e.printStackTrace();}
    }
    
    @Override
    public void record(SceneRecorder sceneRecorder, int tick){
        if(actor == null) return;
        
        if(actor.isSneaking()) setSneak(tick);
        
        Location location = actor.getLocation();
        location.setX(location.getX() - sceneRecorder.getRegionMin().getX());
        location.setY(location.getY() - sceneRecorder.getRegionMin().getY());
        location.setZ(location.getZ() - sceneRecorder.getRegionMin().getZ());
        Location eyeLocation = actor.getEyeLocation();
        this.locationMap.put(tick, location.toVector());
        this.yawPitchMap.put(tick, new Vec2f(eyeLocation.getYaw(), eyeLocation.getPitch()));
        
        if(tick > endTick){
            endTick = tick;
        }
    }
    
    
    @Override
    public void playEnd(ScenePlayer scenePlayer){
        try {
            Object npc = npcMap.get(scenePlayer.getID());
    
            Object destroy = NMSUtil.createEntityDestroyPacket(npc);
            Object infoPacket = NMSUtil.createPlayerInfoPacket("REMOVE_PLAYER", npc);
            for (Player player : scenePlayer.getAudiences()) {
                NMSUtil.sendPacket(player, destroy);
                NMSUtil.sendPacket(player, infoPacket);
            }
        }catch (Exception e){e.printStackTrace();}
    }
    
    
    @Override
    public void playInitialize(ScenePlayer scenePlayer){
        initializeNPC(scenePlayer);
        spawnNPC(scenePlayer);
    }
    
    
    @Override
    public void play(ScenePlayer scenePlayer, int tick){
        try {
            Object npc = npcMap.get(scenePlayer.getID());
    
            if (locationMap.containsKey(tick)) {
        
                Vector location = scenePlayer.getBaseLocation().clone().add(locationMap.get(tick)).toVector();
                Vec2f yawPitch = yawPitchMap.get(tick);
                
                
                boolean playerLook = false;
                for(IntegerRange integerRange : playerLookTick){
                    if(integerRange.isInRange(tick)){
                        playerLook = true;
                        break;
                    }
                }
                
                if(playerLook){
                    Player lookPlayer = null;
                    double distance = Double.MAX_VALUE;
                    for(Player player : scenePlayer.getAudiences()){
                        double d = LocationUtil.distanceSquaredSafeDifferentWorld(player.getLocation(),
                                location.toLocation(Objects.requireNonNull(scenePlayer.getBaseLocation().getWorld())));
                        
                        if(d < distance){
                            
                            lookPlayer = player;
                            distance = d;
                        }
                    }
                    
                    if(lookPlayer == null){
                        yawPitch = new Vec2f(0.0F, 0.0F);
                    }else{
                        Location playerLocation = lookPlayer.getLocation();
                        Location temp = scenePlayer.getBaseLocation().clone();
                        temp.setDirection(new Vector(playerLocation.getX() - location.getX(), playerLocation.getY() - location.getY(), playerLocation.getZ() - location.getZ()));
                        yawPitch = new Vec2f(temp.getYaw(), temp.getPitch());
                    }
                }
        
                NMSUtil.setEntityPositionRotation(npc, location.getX(), location.getY(), location.getZ(), yawPitch.x, yawPitch.y);
        
                Object teleport = NMSUtil.createEntityTeleportPacket(npc);
                Object lookMove = NMSUtil.createEntityMoveLookPacket(NMSUtil.getEntityID(npc), (byte) yawPitch.x, (byte) yawPitch.y);
                Object rotation = NMSUtil.createEntityHeadRotationPacket(npc, (yawPitch.x * 256.0F) / 360.0F);
        
                for (Player player : scenePlayer.getAudiences()) {
                    NMSUtil.sendPacket(player, lookMove);
                    NMSUtil.sendPacket(player, teleport);
                    NMSUtil.sendPacket(player, rotation);
                }
            }
    
            if (swing.contains(tick)) {
                Object animation = NMSUtil.createEntityAnimationPacket(npc, 0);
        
                for (Player player : scenePlayer.getAudiences()) {
                    NMSUtil.sendPacket(player, animation);
                }
            }
    
    
            boolean changeStatus = false;
            if (sneak.contains(tick)) {
                if (!NMSUtil.isSneaking(npc)) changeStatus = true;
                NMSUtil.setSneaking(npc, true);
            } else {
                if (NMSUtil.isSneaking(npc)) changeStatus = true;
                NMSUtil.setSneaking(npc, false);
            }
    
            if (changeStatus) {
                Object dataWatcher = NMSUtil.getDataWatcher(npc);
        
                if (NMSUtil.isSneaking(npc)) {
                    NMSUtil.setEntityPose(dataWatcher, "CROUCHING");
                } else {
                    NMSUtil.setEntityPose(dataWatcher, "STANDING");
                }
        
                Object metadata = NMSUtil.createEntityMetadataPacket(npc);
                for (Player player : scenePlayer.getAudiences()) {
                    NMSUtil.sendPacket(player, metadata);
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }
    
    
    @Override
    public void write(FileConfiguration yml, String root){
        if(yml.contains(root)){
            yml.set(root, null);
        }
        
        yml.set(root + ".type", this.getDataType().toString());
        yml.set(root + ".name", this.npcName);
        
        if(this.skin != null){
            yml.set(root + ".skin.value", this.skin[0]);
            yml.set(root + ".skin.signature", this.skin[1]);
        }
        
        //locations  ([index] x, y, z, yaw, pitch)
        List<String> locationLines = new ArrayList<>();
        for(Map.Entry<Integer, Vector> entry : locationMap.entrySet()){
            double x = entry.getValue().getX();
            double y = entry.getValue().getY();
            double z = entry.getValue().getZ();
            
            Vec2f yawPitch = yawPitchMap.get(entry.getKey());
            float yaw = yawPitch.x;
            float pitch = yawPitch.y;
            
            String line = "[" + entry.getKey() + "] " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch;
            locationLines.add(line);
        }
        yml.set(root + ".location", locationLines);
        
        
        //sneaks (0, 1, 2, 3, ...)
        StringBuilder sneakData = new StringBuilder();
        int sneakCount = 1;
        for(Integer index : sneak){
            sneakData.append(index);
            
            if(sneakCount != sneak.size()){
                sneakData.append(", ");
            }
            sneakCount++;
        }
        yml.set(root + ".sneak", sneakData.toString());
        
        
        //swings  (0, 1, 2, 3, ...)
        StringBuilder swingData = new StringBuilder();
        int swingCount = 1;
        for(Integer index : swing){
            swingData.append(index);
        
            if(swingCount != swing.size()){
                swingData.append(", ");
            }
            swingCount++;
        }
        yml.set(root + ".swing", swingData.toString());
        
        
        //look-player (- '0 - 100')
        List<String> lookPlayerLines = new ArrayList<>();
        playerLookTick.forEach(integerRange -> lookPlayerLines.add(integerRange.toString()));
        yml.set(root + ".look-player", lookPlayerLines);
    }
    
    @Override
    public void load(FileConfiguration yml, String root){
        //locations  ([index] x, y, z, yaw, pitch)
        List<String> locations = yml.getStringList(root + ".location");
        for(String line : locations){
            line = line.replace("[", "");
            line = line.replace(" ", "");
            
            String[] indexLocation = line.split("]");
            int index = Integer.parseInt(indexLocation[0]);
            
            String[] locationArgs = indexLocation[1].split(",");
            double x = Double.parseDouble(locationArgs[0]);
            double y = Double.parseDouble(locationArgs[1]);
            double z = Double.parseDouble(locationArgs[2]);
            
            float yaw = Float.parseFloat(locationArgs[3]);
            float pitch = Float.parseFloat(locationArgs[4]);
            
            this.locationMap.put(index, new Vector(x, y, z));
            this.yawPitchMap.put(index, new Vec2f(yaw, pitch));
    
            if(index > endTick){
                endTick = index;
            }
        }
    
        //sneaks (0, 1, 2, 3, ...)
        String sneaks = yml.getString(root + ".sneak");
        sneaks = sneaks.replace(" ", "");
        if(sneaks.contains(",")) {
            String[] args = sneaks.split(",");
            for (String number : args) {
                int index = Integer.parseInt(number);
                sneak.add(index);
        
                if (index > endTick) {
                    endTick = index;
                }
            }
        }
    
        //swing (0, 1, 2, 3, ...)
        String swings = yml.getString(root + ".swing");
        swings = swings.replace(" ", "");
        if(swings.contains(",")) {
            String[] swingArgs = swings.split(",");
            for (String number : swingArgs) {
                int index = Integer.parseInt(number);
                swing.add(index);
        
                if (index > endTick) {
                    endTick = index;
                }
            }
        }
    
        //look-player (- '0 - 100')
        List<String> lookPlayerLines = yml.getStringList(root + ".look-player");
        lookPlayerLines.forEach(line -> playerLookTick.add(IntegerRange.fromString(line)));
    }
}
