package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import be4rjp.cinema4c.util.Vec2f;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<Integer, EntityArmorStand> standMap = new HashMap<>();
    
    
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
        if(locationMap.keySet().contains(tick)) {
            Vector location = scenePlayer.getBaseLocation().clone().add(locationMap.get(tick)).toVector();
            Vec2f yawPitch = yawPitchMap.get(tick);
            
            EntityArmorStand armorStand = standMap.get(scenePlayer.getID());
            armorStand.setLocation(location.getX(), location.getY(), location.getZ(), yawPitch.x, yawPitch.y);
            PacketPlayOutCamera camera = new PacketPlayOutCamera(armorStand);
            PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(armorStand);
            PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookMove = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(armorStand.getBukkitEntity().getEntityId(), (short) 0, (short) 0, (short) 0, ((byte) ((yawPitch.x * 256.0F) / 360.0F)), ((byte) ((yawPitch.y * 256.0F) / 360.0F)), true);
            PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(armorStand, (byte) ((yawPitch.x * 256.0F) / 360.0F));
    
            for (Player player : scenePlayer.getAudiences()) {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                entityPlayer.playerConnection.sendPacket(camera);
                entityPlayer.playerConnection.sendPacket(teleport);
                entityPlayer.playerConnection.sendPacket(lookMove);
                entityPlayer.playerConnection.sendPacket(rotation);
            }
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
            WorldServer nmsWorld = ((CraftWorld) baseLocation.getWorld()).getHandle();
            EntityArmorStand entityArmorStand = new EntityArmorStand(nmsWorld, baseLocation.getX(), baseLocation.getY(), baseLocation.getZ());
            entityArmorStand.setInvisible(true);
            standMap.put(scenePlayer.getID(), entityArmorStand);
        }
        //最初の再生位置を取得する
        if(locationMap.size() > 0){
            for(int index = 0; index < endTick; index++){
                Vector location = locationMap.get(index);
                if(location != null){
                    Location loc = baseLocation.clone().add(location);
                    standMap.get(scenePlayer.getID()).setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), baseLocation.getYaw(), baseLocation.getPitch());
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
        EntityArmorStand entityArmorStand = standMap.get(scenePlayer.getID());
        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(entityArmorStand);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getBukkitEntity().getEntityId(), entityArmorStand.getDataWatcher(), true);
        for(Player player : scenePlayer.getAudiences()) {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            entityPlayer.playerConnection.sendPacket(spawn);
            entityPlayer.playerConnection.sendPacket(metadata);
        }
    }
    
    @Override
    public void playInitialize(ScenePlayer scenePlayer) {
        initializeStand(scenePlayer);
        spawnStand(scenePlayer);
        
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
        PacketPlayOutGameStateChange stateChange = new PacketPlayOutGameStateChange(3, 2);
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(standMap.get(scenePlayer.getID()).getBukkitEntity().getEntityId());
        for(Player player : scenePlayer.getAudiences()) {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            PacketPlayOutCamera camera = new PacketPlayOutCamera(entityPlayer);
            entityPlayer.playerConnection.sendPacket(stateChange);
            entityPlayer.playerConnection.sendPacket(destroy);
            entityPlayer.playerConnection.sendPacket(camera);
        }
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : scenePlayer.getAudiences()) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
            }
        };
        task.runTask(Cinema4C.getPlugin());
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
