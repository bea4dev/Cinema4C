package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.nms.NMSUtil;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import be4rjp.cinema4c.util.Vec2f;
import io.papermc.lib.PaperLib;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * カメラ用の録画データ
 */
public class CameraTrackData implements TrackData{
    
    //トラッキングするプレイヤー
    private Player actor;
    //プレイヤーの位置と方向のマップ
    private final Map<Integer, Vector> locationMap = new HashMap<>();
    private final Map<Integer, Vec2f> yawPitchMap = new HashMap<>();
    //この録画データの最終Tick
    private int endTick = 0;
    
    //再生時にカメラとして設定するエンティティ
    private final Map<Integer, Object> standMap = new HashMap<>();
    
    
    public CameraTrackData(Player actor){
        this.actor = actor;
    }
    
    public CameraTrackData(){
        this.actor = null;
    }
    
    @Override
    public DataType getDataType() {
        return DataType.CAMERA;
    }
    
    
    @Override
    public void record(SceneRecorder sceneRecorder, int tick) {
        if(actor == null) return;
    
        Location location = actor.getLocation();
        location.setX(location.getX() - sceneRecorder.getRegionMin().getX());
        location.setY(location.getY() - sceneRecorder.getRegionMin().getY());
        location.setZ(location.getZ() - sceneRecorder.getRegionMin().getZ());
        this.locationMap.put(tick, location.toVector());
        this.yawPitchMap.put(tick, new Vec2f(location.getYaw(), location.getPitch()));
    
        if(tick > endTick){
            endTick = tick;
        }
    }
    
    @Override
    public void recordEnd() {
        this.actor = null;
    }
    
    @Override
    public void play(ScenePlayer scenePlayer, int tick) {
        if(locationMap.containsKey(tick)) {
            Vector location = scenePlayer.getBaseLocation().clone().add(locationMap.get(tick)).toVector();
            Vec2f yawPitch = yawPitchMap.get(tick);
            
            try {
                Object stand = standMap.get(scenePlayer.getID());
                NMSUtil.setEntityPositionRotation(stand, location.getX(), location.getY(), location.getZ(), yawPitch.x, yawPitch.y);
                Object camera = NMSUtil.createCameraPacket(stand);
                Object teleport = NMSUtil.createEntityTeleportPacket(stand);
                Object lookMove = NMSUtil.createEntityMoveLookPacket(NMSUtil.getEntityID(stand), (yawPitch.x * 256.0F) / 360.0F, (yawPitch.y * 256.0F) / 360.0F);
                Object rotation = NMSUtil.createEntityHeadRotationPacket(stand, (yawPitch.x * 256.0F) / 360.0F);
    
                for (Player player : scenePlayer.getAudiences()) {
                    NMSUtil.sendPacket(player, rotation);
                    NMSUtil.sendPacket(player, lookMove);
                    NMSUtil.sendPacket(player, teleport);
                    NMSUtil.sendPacket(player, camera);
                }
            }catch (Exception e){e.printStackTrace();}
        }
    }
    
    /**
     * 再生時にカメラとして設定するエンティティの準備
     * @param scenePlayer
     */
    public void initializeStand(ScenePlayer scenePlayer){
        //エンティティを作成してScenePlayerのIDと紐づけて保存
        Location baseLocation = scenePlayer.getBaseLocation();
        if(standMap.get(scenePlayer.getID()) == null){
            try {
                Object stand = NMSUtil.createEntityArmorStand(baseLocation.getWorld(), baseLocation.getX(), baseLocation.getY(), baseLocation.getZ());
                standMap.put(scenePlayer.getID(), stand);
            }catch (Exception e){e.printStackTrace();}
        }
        //最初の再生位置を取得する
        if(locationMap.size() > 0){
            for(int index = 0; index < endTick; index++){
                Vector location = locationMap.get(index);
                Vec2f yawPitch = yawPitchMap.get(index);
                if(location != null && yawPitch != null){
                    Location loc = baseLocation.clone().add(location);
                    try {
                        NMSUtil.setEntityPositionRotation(standMap.get(scenePlayer.getID()), loc.getX(), loc.getY(), loc.getZ(), yawPitch.x, yawPitch.y);
                    }catch (Exception e){e.printStackTrace();}
                    return;
                }
            }
        }
    }
    
    /**
     * 再生時にカメラとして設定するエンティティをスポーン
     * @param scenePlayer
     */
    public void spawnStand(ScenePlayer scenePlayer){
        try {
            Object stand = standMap.get(scenePlayer.getID());
            Object spawn = NMSUtil.createSpawnEntityLivingPacket(stand);
            Object metadata = NMSUtil.createEntityMetadataPacket(stand);
            Location loc = NMSUtil.getEntityLocation(stand);
            Object lookMove = NMSUtil.createEntityMoveLookPacket(NMSUtil.getEntityID(stand), (loc.getYaw() * 256.0F) / 360.0F, (loc.getPitch() * 256.0F) / 360.0F);
            Object rotation = NMSUtil.createEntityHeadRotationPacket(stand, (loc.getYaw() * 256.0F) / 360.0F);
            for (Player player : scenePlayer.getAudiences()) {
                NMSUtil.sendPacket(player, spawn);
                NMSUtil.sendPacket(player, lookMove);
                NMSUtil.sendPacket(player, rotation);
                NMSUtil.sendPacket(player, metadata);
            }
        }catch (Exception e){e.printStackTrace();}
    }
    
    @Override
    public void playInitialize(ScenePlayer scenePlayer) {
        initializeStand(scenePlayer);
        spawnStand(scenePlayer);
        
        Object stand = standMap.get(scenePlayer.getID());
        
        try{
            Location location = NMSUtil.getEntityLocation(stand);
            scenePlayer.getAudiences().forEach(player -> PaperLib.teleportAsync(player, location));
        }catch (Exception e){e.printStackTrace();}
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : scenePlayer.getAudiences()) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }
        };
        task.runTask(Cinema4C.getPlugin());
    }
    
    @Override
    public int getEndTick() {
        return this.endTick;
    }
    
    @Override
    public void setEndTick(int endTick) {
        this.endTick = endTick;
    }
    
    @Override
    public void playEnd(ScenePlayer scenePlayer) {
        try {
            //Object stateChange = NMSUtil.createGameStateChangePacket(3, 2);
            Object destroy = NMSUtil.createEntityDestroyPacket(standMap.get(scenePlayer.getID()));
            for (Player player : scenePlayer.getAudiences()) {
                Object entityPlayer = NMSUtil.getNMSPlayer(player);
                Object camera = NMSUtil.createCameraPacket(entityPlayer);
                //NMSUtil.sendPacket(player, stateChange);
                NMSUtil.sendPacket(player, destroy);
                NMSUtil.sendPacket(player, camera);
            }
        }catch (Exception e){e.printStackTrace();}
        
        /*
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : scenePlayer.getAudiences()) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
            }
        };
        task.runTask(Cinema4C.getPlugin());
         */
    }
    
    @Override
    public void write(FileConfiguration yml, String root){
        if(yml.contains(root)){
            yml.set(root, null);
        }
        
        yml.set(root + ".type", this.getDataType().toString());
        
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
    }
}
