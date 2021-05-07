package be4rjp.cinema4c.recorder;

import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * シーン撮影用レコーダー
 */
public class SceneRecorder extends BukkitRunnable {
    
    //書き込む録画データ
    private final RecordData recordData;
    //ブロックをトラッキングする範囲の最小座標、録画データの基準座標としても使うためブロックをトラッキングしなくても必須
    private final Location regionMin;
    //ブロックをトラッキングする範囲の最大座標
    private final Location regionMax;
    
    //現在記録しているtick
    private int tick = 0;
    
    public SceneRecorder(RecordData recordData, Location regionMin, Location regionMax){
        this.recordData = recordData;
        this.regionMin = regionMin;
        this.regionMax = regionMax;
    }
    
    public RecordData getRecordData() {return recordData;}
    
    public Location getRegionMax() {return regionMax.clone();}
    
    public Location getRegionMin() {return regionMin.clone();}
    
    public int getTick() {return tick;}
    
    @Override
    public void run() {
        recordData.recordTrackData(this, tick);
        
        //情報表示
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        
        long totalMB = total / 1048576;
        long usedMB = used / 1048576;
        
        String info = "§c§l●REC  §bTick: §f" + tick + "  §aMem: §f" + usedMB + " §7/ " + totalMB + "MB";
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(info)));
        
        tick++;
    }
    
    
    @Override
    public synchronized void cancel() throws IllegalStateException {
        for(TrackData trackData : recordData.getTrackData()){
            trackData.recordEnd();
        }
        super.cancel();
    }
}
