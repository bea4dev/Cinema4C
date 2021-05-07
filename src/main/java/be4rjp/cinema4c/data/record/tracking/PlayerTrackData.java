package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import be4rjp.cinema4c.util.SkinManager;
import be4rjp.cinema4c.util.Vec2f;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
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
    //スニーク状態になっているときのTick
    private final Set<Integer> sneak = new HashSet<>();
    //腕を降った時のTick
    private final Set<Integer> swing = new HashSet<>();
    //この録画データの最終Tick
    private int endTick = 0;
    //NPCのマップ keyはScenePlayerのID
    private final Map<Integer, EntityPlayer> npcMap = new HashMap<>();
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
    
    public EntityPlayer getNPC(int playerID) {return npcMap.get(playerID);}
    
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
        //NPCを作成してScenePlayerのIDと紐づけて保存
        Location baseLocation = scenePlayer.getBaseLocation();
        if(npcMap.get(scenePlayer.getID()) == null) {
            MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
            WorldServer nmsWorld = ((CraftWorld) baseLocation.getWorld()).getHandle();
            String name = npcName == null ? actor.getName() : npcName;
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
            if(skin != null){
                gameProfile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));
            }
            npcMap.put(scenePlayer.getID(), new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld)));
        }
        //最初の再生位置を取得する
        if(locationMap.size() > 0){
            for(int index = 0; index < endTick; index++){
                Vector location = locationMap.get(index);
                if(location != null){
                    Location loc = baseLocation.clone().add(location);
                    npcMap.get(scenePlayer.getID()).setLocation(loc.getX(), loc.getY(), loc.getZ(), baseLocation.getYaw(), baseLocation.getPitch());
                    return;
                }
            }
        }
        npcMap.get(scenePlayer.getID()).setLocation(baseLocation.getX(), baseLocation.getY(), baseLocation.getZ(), baseLocation.getYaw(), baseLocation.getPitch());
    }
    
    public void spawnNPC(ScenePlayer scenePlayer){
        EntityPlayer npc = npcMap.get(scenePlayer.getID());
        PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc);
        PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(npc);
        DataWatcher dataWatcher = npc.getDataWatcher();
        dataWatcher.set(DataWatcherRegistry.a.a(16), (byte)127);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(npc.getBukkitEntity().getEntityId(), dataWatcher, true);
        PacketPlayOutEntity.PacketPlayOutEntityLook look = new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.getBukkitEntity().getEntityId(), (byte) MathHelper.d((0F * 256.0F) / 360.0F), (byte) MathHelper.d((0F * 256.0F) / 360.0F), true);
        
        for(Player player : scenePlayer.getAudiences()){
            EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
            entityPlayer.playerConnection.sendPacket(infoPacket);
            entityPlayer.playerConnection.sendPacket(spawnPacket);
            entityPlayer.playerConnection.sendPacket(metadata);
            entityPlayer.playerConnection.sendPacket(look);
        }
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
        EntityPlayer npc = npcMap.get(scenePlayer.getID());
        
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(npc.getBukkitEntity().getEntityId());
        PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc);
        for (Player player : scenePlayer.getAudiences()) {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            entityPlayer.playerConnection.sendPacket(destroy);
            entityPlayer.playerConnection.sendPacket(infoPacket);
        }
    }
    
    
    @Override
    public void playInitialize(ScenePlayer scenePlayer){
        initializeNPC(scenePlayer);
        spawnNPC(scenePlayer);
    }
    
    
    @Override
    public void play(ScenePlayer scenePlayer, int tick){
        EntityPlayer npc = npcMap.get(scenePlayer.getID());
        
        if(locationMap.keySet().contains(tick)) {
    
            Vector location = scenePlayer.getBaseLocation().clone().add(locationMap.get(tick)).toVector();
            Vec2f yawPitch = yawPitchMap.get(tick);
    
            npc.setLocation(location.getX(), location.getY(), location.getZ(), yawPitch.x, yawPitch.y);
    
            PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(npc);
            PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookMove = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(npc.getBukkitEntity().getEntityId(), (short) 0, (short) 0, (short) 0, ((byte) yawPitch.x), ((byte) yawPitch.y), true);
            PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(npc, (byte) ((yawPitch.x * 256.0F) / 360.0F));
    
            for (Player player : scenePlayer.getAudiences()) {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                entityPlayer.playerConnection.sendPacket(lookMove);
                entityPlayer.playerConnection.sendPacket(teleport);
                entityPlayer.playerConnection.sendPacket(rotation);
            }
        }
        
        if(swing.contains(tick)){
            PacketPlayOutAnimation animation = new PacketPlayOutAnimation(npc, 0);
    
            for (Player player : scenePlayer.getAudiences()) {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                entityPlayer.playerConnection.sendPacket(animation);
            }
        }
        
        boolean changeStatus = false;
        if(sneak.contains(tick)){
            if(!npc.isSneaking()) changeStatus = true;
            npc.setSneaking(true);
        }else{
            if(npc.isSneaking()) changeStatus = true;
            npc.setSneaking(false);
        }
        
        if(changeStatus) {
            DataWatcher dataWatcher = npc.getDataWatcher();
            
            if(npc.isSneaking()){
                dataWatcher.set(DataWatcherRegistry.s.a(6), EntityPose.CROUCHING);
            }else{
                dataWatcher.set(DataWatcherRegistry.s.a(6), EntityPose.STANDING);
            }
            
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(npc.getBukkitEntity().getEntityId(), dataWatcher, true);
            for (Player player : scenePlayer.getAudiences()) {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                entityPlayer.playerConnection.sendPacket(metadata);
            }
        }
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
    }
}
