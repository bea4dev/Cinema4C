package be4rjp.cinema4c.recorder;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.data.record.tracking.CameraTrackData;
import be4rjp.cinema4c.data.record.tracking.PlayerTrackData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
import be4rjp.cinema4c.exception.DifferentVersionException;
import be4rjp.cinema4c.exception.DuplicateNameException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RecordManager {
    
    /**
     * 現在レコード中のレコーダー
     * パフォーマンスの低下と設計が複雑になるのを防ぐため、
     * 同時に複数のレコードを実行させないようにしている
     */
    private static SceneRecorder sceneRecorder = null;
    
    //レコードのデータを一時的に保持
    public static final Map<String, RecordData> recordDataMap = new HashMap<>();
    
    
    /**
     * 初期化
     */
    public static void initialize(){
        sceneRecorder = null;
        recordDataMap.clear();
        
        try{
            sceneRecorder.cancel();
        }catch (Exception e){}
    }
    
    /**
     * レコーダーを取得
     * @return Recorder
     */
    public static SceneRecorder getRecorder() {return sceneRecorder;}
    
    
    /**
     * 指定された名前のレコードデータを取得
     * 見つからない場合はnullを返す
     * @param name
     * @return
     */
    public static RecordData getRecordData(String name){
        return recordDataMap.get(name);
    }
    
    
    /**
     * 新しくレコードデータを作成
     * @return
     */
    public static RecordData createNewRecordData(String name){
        RecordData recordData = new RecordData(name);
        recordDataMap.put(name, recordData);
        
        return recordData;
    }
    
    /**
     * レコーダーとデータを作成
     * @param recordData
     * @param minLocation
     * @param maxLocation
     * @return Recorder
     * @throws DuplicateNameException データの名前が重複する場合
     */
    public static SceneRecorder createSceneRecorder(RecordData recordData, Location minLocation, Location maxLocation){
        sceneRecorder = new SceneRecorder(recordData, minLocation, maxLocation);
        return sceneRecorder;
    }
    
    public static void startRecording(){sceneRecorder.runTaskTimerAsynchronously(Cinema4C.getPlugin(), 0, 1);}
    
    public static void stopRecording(){sceneRecorder.cancel();}
    
    
    /**
     * トラッキングするプレイヤーを追加
     * @param player
     */
    public static void addTrackingPlayer(Player player){
        PlayerTrackData playerTrackData = new PlayerTrackData(player);
        sceneRecorder.getRecordData().addTrackData(playerTrackData);
    }
    
    
    /**
     * カメラとしてトラッキングするプレイヤーを追加
     * @param player
     */
    public static void addCameraTrackingPlayer(Player player){
        for(TrackData trackData : sceneRecorder.getRecordData().getTrackData()){
            if(trackData instanceof CameraTrackData) return;
        }
        CameraTrackData cameraTrackData = new CameraTrackData(player);
        sceneRecorder.getRecordData().addTrackData(cameraTrackData);
    }
    
    
    /**
     * 全てのレコードデータを読み込む
     */
    public static void loadAllRecordData(){
        initialize();
        
        Cinema4C.getPlugin().getLogger().info("Loading record data...");
        File dir = new File("plugins/Cinema4C/record");
        
        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if(files.length == 0){
            Cinema4C.getPlugin().saveResource("record/sample-record.yml", false);
            files = dir.listFiles();
        }
    
        if(files != null) {
            for (File file : files) {
                Cinema4C.getPlugin().getLogger().info(file.getName());
                String name = file.getName().replace(".yml", "");
                RecordData recordData = new RecordData(name);
                try {
                    recordData.loadData();
                } catch (DifferentVersionException e) {
                    Cinema4C.getPlugin().getLogger().warning(e.getMessage());
                }
                recordDataMap.put(name, recordData);
            }
        }
    }
}
