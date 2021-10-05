package be4rjp.cinema4c.data.record;

import be4rjp.cinema4c.data.record.tracking.*;
import be4rjp.cinema4c.exception.DifferentVersionException;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 一つのシーンとして、各種録画データをまとめたもの
 */
public class RecordData {
    
    //過去バージョンとの互換性を確保できなくなった時に変更する
    private static final int VERSION = 2;
    
    private final String name;
    //各種の録画データ
    private final Set<TrackData> trackData;
    //ループしたときに戻すtick
    private int loopBackTick = 0;
    
    public RecordData(String name) {
        this.name = name;
        this.trackData = new HashSet<>();
    }
    
    public Set<TrackData> getTrackData(){return this.trackData;}
    
    public void addTrackData(TrackData data){this.trackData.add(data);}
    
    /**
     * 各種の録画データに現在の状態を記録
     * @param sceneRecorder
     * @param tick
     */
    public void recordTrackData(SceneRecorder sceneRecorder, int tick){this.trackData.forEach(data -> data.record(sceneRecorder, tick));}
    
    /**
     * 指定されたtickのデータを再生
     * @param scenePlayer
     * @param tick
     */
    public void playTrackData(ScenePlayer scenePlayer, int tick){
        this.trackData.forEach(data -> data.play(scenePlayer, tick));
    }
    
    /**
     * ループしたときに戻すtickを取得する
     * @return int
     */
    public int getLoopBackTick() {return loopBackTick;}
    
    public void createFile(File file){
        file.getParentFile().mkdir();
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * ymlに保存
     */
    public void saveData(){
        File file = new File("plugins/Cinema4C/record", name + ".yml");
    
        FileConfiguration yml = new YamlConfiguration();
        yml.set("version", VERSION);
        
        yml.set("loop-back-tick", loopBackTick);
        
        int index = 0;
        for(TrackData data : this.trackData){
            data.write(yml, "track-data.data" + index);
            index++;
        }
    
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * ymlからロード
     * @throws DifferentVersionException バージョンが異なる場合
     */
    public void loadData() throws DifferentVersionException {
        this.trackData.clear();
        
        File file = new File("plugins/Cinema4C/record", name + ".yml");
        createFile(file);
    
        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        int ver = yml.getInt("version");
        if(ver != VERSION) throw new DifferentVersionException("Cannot load record data due to different version. System version->" + VERSION + " File version->" + ver);
        
        this.loopBackTick = yml.getInt("loop-back-tick");
    
        for (String dataName : yml.getConfigurationSection("track-data").getKeys(false)){
            String type = yml.getString("track-data." + dataName + ".type");
            TrackData.DataType dataType = TrackData.DataType.valueOf(type);
            
            switch (dataType){
                case CAMERA:{
                    CameraTrackData cameraTrackData = new CameraTrackData();
                    cameraTrackData.load(yml, "track-data." + dataName);
                    this.trackData.add(cameraTrackData);
                    break;
                }
                
                case NPC:{
                    String name = yml.getString("track-data." + dataName + ".name");
                    String[] skin = null;
                    int audienceSkin = -1;
                    
                    if(yml.contains("track-data." + dataName + ".skin")) {
                        if(yml.contains("track-data." + dataName + ".skin.audience-skin")){
                            audienceSkin = yml.getInt("track-data." + dataName + ".skin.audience-skin");
                            skin = new String[]{"", ""};
                        } else {
                            String skin_value = yml.getString("track-data." + dataName + ".skin.value");
                            String skin_signature = yml.getString("track-data." + dataName + ".skin.signature");
                            skin = new String[]{skin_value, skin_signature};
                        }
                    }
                    PlayerTrackData playerTrackData = new PlayerTrackData(skin, name);
                    if(audienceSkin != -1) playerTrackData.setAudienceSkin(audienceSkin);
                    playerTrackData.load(yml, "track-data." + dataName);
                    this.trackData.add(playerTrackData);
                    break;
                }
                
                case MESSAGE:{
                    MessageData messageData = new MessageData();
                    messageData.load(yml, "track-data." + dataName);
                    this.trackData.add(messageData);
                    break;
                }
                
                case EXTENSIONS:{
                    ExtensionsData extensionsData = new ExtensionsData();
                    extensionsData.load(yml, "track-data." + dataName);
                    this.trackData.add(extensionsData);
                    break;
                }
                
                case EFFECT:{
                    EffectData effectData = new EffectData();
                    effectData.load(yml, "track-data." + dataName);
                    this.trackData.add(effectData);
                    break;
                }
            }
        }
    }
}
