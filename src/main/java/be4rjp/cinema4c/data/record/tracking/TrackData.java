package be4rjp.cinema4c.data.record.tracking;

import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.SceneRecorder;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 各種の録画データ
 */
public interface TrackData {
    
    /**
     * 現在の状態を記録する
     * @param sceneRecorder
     * @param tick
     */
    void record(SceneRecorder sceneRecorder, int tick);
    
    /**
     * 録画終了処理
     */
    void recordEnd();
    
    /**
     * 指定されたtickのデータを再生
     * @param scenePlayer
     * @param tick
     */
    void play(ScenePlayer scenePlayer, int tick);
    
    /**
     * 再生前の準備
     * @param scenePlayer
     */
    void playInitialize(ScenePlayer scenePlayer);
    
    /**
     * この録画データの最終tickを取得する
     * @return
     */
    int getEndTick();
    
    /**
     * この録画データの最終tickを設定する
     * @param endTick
     */
    void setEndTick(int endTick);
    
    /**
     * 再生終了処理
     * @param scenePlayer
     */
    void playEnd(ScenePlayer scenePlayer);
    
    /**
     * このデータのタイプを取得
     * @return
     */
    DataType getDataType();
    
    /**
     * 指定されたymlに書き込み
     * @param yml
     * @param root
     */
    void write(FileConfiguration yml, String root);
    
    /**
     * 指定されたymlからロード
     * @param yml
     * @param root
     */
    void load(FileConfiguration yml, String root);
    
    public enum DataType{
        CAMERA,
        NPC,
        MESSAGE
    }
}
