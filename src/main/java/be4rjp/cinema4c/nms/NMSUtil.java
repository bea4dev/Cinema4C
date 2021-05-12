package be4rjp.cinema4c.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.*;
import java.util.*;

public class NMSUtil {
    
    //微妙にCPU負荷が小さくなるおまじないキャッシュ
    private static Map<String, Class<?>> nmsClassMap = new HashMap<>();
    private static Map<String, Class<?>> craftBukkitClassMap = new HashMap<>();
    
    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        Class<?> nmsClass = nmsClassMap.get(nmsClassString);
        
        if(nmsClass == null){
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
            String name = "net.minecraft.server." + version + nmsClassString;
            nmsClass = Class.forName(name);
            nmsClassMap.put(nmsClassString, nmsClass);
        }
        
        return nmsClass;
    }
    
    public static Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        Class<?> craftBukkitClass = craftBukkitClassMap.get(className);
        
        if(craftBukkitClass == null){
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
            craftBukkitClass = Class.forName("org.bukkit.craftbukkit." + version + className);
            craftBukkitClassMap.put(className, craftBukkitClass);
        }
        
        return craftBukkitClass;
    }
    
    
    public static Object getConnection(Player player) throws SecurityException, NoSuchMethodException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }
    
    
    public static Channel getChannel(Player player) throws SecurityException, NoSuchMethodException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        
        Field netField = con.getClass().getField("networkManager");
        Object net = netField.get(con);
        
        Field chaField = net.getClass().getField("channel");
        Object channel = chaField.get(net);
        
        return (Channel)channel;
    }
    
    
    public static Object getNMSPlayer(Player player) throws SecurityException, NoSuchMethodException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        Method getHandle = player.getClass().getMethod("getHandle");
        return getHandle.invoke(player);
    }
    
    
    public static Object getNMSWorld(World world) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        Method getHandle = world.getClass().getMethod("getHandle");
        return getHandle.invoke(world);
    }
    
    
    public static Object getNMSServer(Server server) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        Method getServer = server.getClass().getMethod("getServer");
        return getServer.invoke(server);
    }
    
    
    public static int getEntityID(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> Entity = getNMSClass("Entity");
        Method getBukkitEntity = Entity.getMethod("getBukkitEntity");
        Object bukkitEntity = getBukkitEntity.invoke(entity);
        
        return ((org.bukkit.entity.Entity)bukkitEntity).getEntityId();
    }
    
    
    public static Location getEntityLocation(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> Entity = getNMSClass("Entity");
        Method getBukkitEntity = Entity.getMethod("getBukkitEntity");
        Object bukkitEntity = getBukkitEntity.invoke(entity);
        return ((org.bukkit.entity.Entity)bukkitEntity).getLocation();
    }
    
    
    public static Object createEntityPlayer(Object nmsServer, Object nmsWorld, GameProfile gameProfile)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> PlayerInteractManager = getNMSClass("PlayerInteractManager");
        Class<?> WorldServer = getNMSClass("WorldServer");
        Object interactManager = PlayerInteractManager.getConstructor(WorldServer).newInstance(nmsWorld);
        
        Class<?> MinecraftServer = getNMSClass("MinecraftServer");
        Class<?> EntityPlayer = getNMSClass("EntityPlayer");
        return EntityPlayer.getConstructor(MinecraftServer, WorldServer, GameProfile.class, PlayerInteractManager).newInstance(nmsServer, nmsWorld, gameProfile, interactManager);
    }
    
    
    public static Object createEntityArmorStand(World world, double x, double y, double z)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> EntityArmorStand = getNMSClass("EntityArmorStand");
        Class<?> NMSWorld = getNMSClass("World");
        Object entityArmorStand = null;
        try {
            entityArmorStand = EntityArmorStand.getConstructor(NMSWorld).newInstance(getNMSWorld(world));
        }catch (NoSuchMethodException e){
            entityArmorStand = EntityArmorStand.getConstructor
                    (NMSWorld, double.class, double.class, double.class).newInstance(getNMSWorld(world), x, y, z);
        }
        
        Method setInvisible = EntityArmorStand.getMethod("setInvisible", boolean.class);
        setInvisible.invoke(entityArmorStand, true);
        
        Method setNoGravity = EntityArmorStand.getMethod("setNoGravity", boolean.class);
        setNoGravity.invoke(entityArmorStand, true);
        
        return entityArmorStand;
    }
    
    
    public static void setEntityPositionRotation(Object entity, double x, double y, double z, float yaw, float pitch)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> Entity = getNMSClass("Entity");
        Method setPositionRotation = Entity.getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
        setPositionRotation.invoke(entity, x, y, z, yaw, pitch);
    }
    
    
    public static void sendPacket(Player player, Object packet)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", getNMSClass("Packet"));
        sendPacket.invoke(getConnection(player), packet);
    }
    
    
    public static Object createGameStateChangePacket(int i, float j)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutGameStateChange");
        try {
            Constructor<?> packetConstructor = packetClass.getConstructor(int.class, float.class);
            return packetConstructor.newInstance(i, j);
        }catch (NoSuchMethodException e){
            Class<?> classA = getNMSClass("PacketPlayOutGameStateChange$a");
            Field a = packetClass.getFields()[i];
            Constructor<?> packetConstructor = packetClass.getConstructor(classA, float.class);
            return packetConstructor.newInstance(a.get(null), j);
        }
    }
    
    
    public static Object createCameraPacket(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutCamera");
        Class<?> Entity = getNMSClass("Entity");
        Constructor<?> packetConstructor = packetClass.getConstructor(Entity);
        return packetConstructor.newInstance(entity);
    }
    
    
    public static Object createEntityMoveLookPacket(int entityID, float yaw, float pitch)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> PacketPlayOutEntity = getNMSClass("PacketPlayOutEntity");
        Object packet = PacketPlayOutEntity.getConstructor(int.class).newInstance(entityID);
        
        Class<?> packetClass = getNMSClass("PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
        Constructor<?> packetConstructor = packetClass.getConstructor(int.class, short.class, short.class, short.class, byte.class, byte.class, boolean.class);
        return packetConstructor.newInstance(entityID, (short) 0, (short) 0, (short) 0, (byte)yaw, (byte)pitch, true);
    }
    
    
    public static Object createEntityHeadRotationPacket(Object entity, float yaw)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityHeadRotation");
        Class<?> Entity = getNMSClass("Entity");
        Constructor<?> packetConstructor = packetClass.getConstructor(Entity, byte.class);
        return packetConstructor.newInstance(entity, (byte)yaw);
    }
    
    
    public static Object createEntityTeleportPacket(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityTeleport");
        Class<?> Entity = getNMSClass("Entity");
        Constructor<?> packetConstructor = packetClass.getConstructor(Entity);
        return packetConstructor.newInstance(entity);
    }
    
    
    public static Object createEntityDestroyPacket(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityDestroy");
        Class<?> Entity = getNMSClass("Entity");
        
        Method getBukkitEntity = Entity.getMethod("getBukkitEntity");
        Object bukkitEntity = getBukkitEntity.invoke(entity);
        
        Constructor<?> packetConstructor = packetClass.getConstructor(int[].class);
        int[] ints = {((org.bukkit.entity.Entity)bukkitEntity).getEntityId()};
        return packetConstructor.newInstance(ints);
    }
    
    
    public static Object createEntityAnimationPacket(Object entity, int animation)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutAnimation");
        Class<?> Entity = getNMSClass("Entity");
        Constructor<?> packetConstructor = packetClass.getConstructor(Entity, int.class);
        return packetConstructor.newInstance(entity, animation);
    }
    
    
    public static Object createSpawnEntityLivingPacket(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutSpawnEntityLiving");
        Class<?> LivingEntity = getNMSClass("EntityLiving");
        Constructor<?> packetConstructor = packetClass.getConstructor(LivingEntity);
        return packetConstructor.newInstance(entity);
    }
    
    
    public static Object createNamedEntitySpawnPacket(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutNamedEntitySpawn");
        Class<?> EntityHuman = getNMSClass("EntityHuman");
        Constructor<?> packetConstructor = packetClass.getConstructor(EntityHuman);
        return packetConstructor.newInstance(entity);
    }
    
    
    public static Object getDataWatcher(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> Entity = getNMSClass("Entity");
        Method getDataWatcher = Entity.getMethod("getDataWatcher");
        return getDataWatcher.invoke(entity);
    }
    
    
    public static void setSkinOption(Object dataWatcher)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> DataWatcher = getNMSClass("DataWatcher");
        Class<?> DataWatcherRegistry = getNMSClass("DataWatcherRegistry");
        
        Field a = DataWatcherRegistry.getField("a");
        Object serializer = a.get(null);
        Method ma = serializer.getClass().getMethod("a", int.class);
        Object dataObject = ma.invoke(serializer, 16);
        
        Method set = null;
        for(Method method : DataWatcher.getMethods()){
            if(method.getName().equals("set")){
                set = method;
            }
        }
        set.invoke(dataWatcher, dataObject, (byte)127);
    }
    
    
    public static void setEntityPose(Object dataWatcher, String pose)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> DataWatcher = getNMSClass("DataWatcher");
        Class<?> DataWatcherRegistry = getNMSClass("DataWatcherRegistry");
        
        Field s = DataWatcherRegistry.getField("s");
        Object serializer = s.get(null);
        Method ma = serializer.getClass().getMethod("a", int.class);
        Object dataObject = ma.invoke(serializer, 6);
    
        Class<?> EntityPose = getNMSClass("EntityPose");
        Object pe = null;
        for (Object o: EntityPose.getEnumConstants()) {
            if(o.toString().equals(pose))
                pe = o;
        }
        
        Method set = null;
        for(Method method : DataWatcher.getMethods()){
            if(method.getName().equals("set")){
                set = method;
            }
        }
        set.invoke(dataWatcher, dataObject, pe);
    }
    
    
    public static boolean isSneaking(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
    
        Class<?> EntityPlayer = getNMSClass("EntityPlayer");
        return (boolean)EntityPlayer.getMethod("isSneaking").invoke(entity);
    }
    
    
    public static void setSneaking(Object entity, boolean flag)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> EntityPlayer = getNMSClass("EntityPlayer");
        EntityPlayer.getMethod("setSneaking", boolean.class).invoke(entity, flag);
    }
    
    
    public static Object createEntityMetadataPacket(Object entity)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityMetadata");
        Class<?> Entity = getNMSClass("Entity");
        Class<?> DataWatcher = getNMSClass("DataWatcher");
        
        Method getDataWatcher = Entity.getMethod("getDataWatcher");
        Object dataWatcher = getDataWatcher.invoke(entity);
        
        Method getBukkitEntity = Entity.getMethod("getBukkitEntity");
        Object bukkitEntity = getBukkitEntity.invoke(entity);
        
        Constructor<?> packetConstructor = packetClass.getConstructor(int.class, DataWatcher, boolean.class);
        return packetConstructor.newInstance(((org.bukkit.entity.Entity)bukkitEntity).getEntityId(), dataWatcher, true);
    }
    
    
    public static Object createPlayerInfoPacket(String action, Object npc)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
    
        Class<?> PacketPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");
        Class<?> EntityPlayer = getNMSClass("EntityPlayer");
    
        Class<?> EnumPlayerInfoAction = getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        Object e = null;
        for (Object o: EnumPlayerInfoAction.getEnumConstants()) {
            if(o.toString().equals(action))
                e = o;
        }
        
        if(e == null) throw new IllegalArgumentException(action + " is not found.");
        
        Object array = Array.newInstance(EntityPlayer, 1);
        Array.set(array, 0, npc);
        
        return PacketPlayOutPlayerInfo.getConstructor(EnumPlayerInfoAction, array.getClass()).newInstance(e, array);
    }
        
        
        public static Object createEntityEquipmentPacket(Object entity, ItemStack itemStack)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        
        Class<?> packetClass = getNMSClass("PacketPlayOutEntityEquipment");
        Class<?> CraftItemStack = getCraftBukkitClass("inventory.CraftItemStack");
        Class<?> ItemStack = getNMSClass("ItemStack");
        Class<?> EnumItemSlot = getNMSClass("EnumItemSlot");
        
        Object slot = null;
        for (Object o: EnumItemSlot.getEnumConstants()) {
            if(o.toString().equals("HEAD"))
                slot = o;
        }
        
        Method asNMSCopy = CraftItemStack.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
        Object nmsItemStack = asNMSCopy.invoke(null, itemStack);
        
        Object packet = null;
        try {
            Constructor<?> packetConstructor = packetClass.getConstructor(int.class, EnumItemSlot, ItemStack);
            packet = packetConstructor.newInstance(getEntityID(entity), slot, nmsItemStack);
        }catch (NoSuchMethodException e){
            Constructor<?> packetConstructor = packetClass.getConstructor(int.class, List.class);
            Pair pair = new Pair(slot, nmsItemStack);
            List<Pair> list = new ArrayList<>();
            list.add(pair);
            packet = packetConstructor.newInstance(getEntityID(entity), list);
        }
        
        return packet;
    }
}
