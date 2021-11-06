package be4rjp.cinema4c.player;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.data.play.MovieData;
import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.data.record.tracking.PlayerTrackData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
import be4rjp.cinema4c.event.AsyncMoviePlayFinishEvent;
import be4rjp.cinema4c.event.AsyncScenePlayFinishEvent;
import be4rjp.cinema4c.nms.NMSUtil;
import be4rjp.cinema4c.util.TaskHandler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * RecordDataを再生するためのクラス
 */
public class ScenePlayer extends BukkitRunnable {
    
    private static int playerID = 0;
    
    //このプレイヤーのID
    private final int id;
    //再生するデータ
    private final RecordData recordData;
    //データを再生して見せるプレイヤー
    private List<Player> audiences;
    //再生するワールド
    private final World world;
    
    //現在の生成時間
    private int tick;
    //再生開始時間
    private int startTick;
    //再生終了時間
    private int endTick;
    
    //次に再生するプレイヤー
    private ScenePlayer nextPlayer = null;
    
    private MovieData movieData = null;

    private int movieID = -1;
    
    private PlayMode playMode = PlayMode.ALL_PLAY;
    
    private boolean pausing = false;
    
    //再生を一時停止しているかどうか
    private boolean isPause = false;
    //他プラグインから実行するための拡張機能
    private Set<Runnable> runnableSet = new HashSet<>();
    //再生終了時に実行する別プラグインの拡張機能
    private Set<Runnable> cancelRunnableSet = new HashSet<>();
    
    
    public ScenePlayer(RecordData recordData, World world, int startTick, int stopTick){
        this.id = playerID;
        playerID++;
        
        this.recordData = recordData;
        this.audiences = new ArrayList<>();
        this.world = world;
        
        this.tick = startTick;
        this.startTick = startTick;
        
        if(stopTick == 0) {
            this.endTick = 0;
            for (TrackData trackData : recordData.getTrackData()) {
                int trackEnd = trackData.getEndTick();
                if (trackEnd > endTick) endTick = trackEnd;
            }
        }else{
            this.endTick = stopTick;
        }
    }
    
    
    public void setNextPlayer(ScenePlayer nextPlayer) {this.nextPlayer = nextPlayer;}
    
    public void setMovieData(MovieData movieData) {this.movieData = movieData;}

    public int getMoviePlayID() {return movieID;}

    public void setMoviePlayID(int moviePlayID) {this.movieID = moviePlayID;}
    
    public int getTick() {return tick;}
    
    public void setTick(int tick) {this.tick = tick;}
    
    public boolean isPause() {return isPause;}
    
    public void setPause(boolean pause) {
        isPause = pause;
        if(!pause) pausing = false;
    }
    
    public Set<Runnable> getRunnableSet() {return runnableSet;}
    
    public Set<Runnable> getCancelRunnableSet() {return cancelRunnableSet;}
    
    public World getWorld() {return world;}
    
    public void initialize(){
        for(TrackData trackData : recordData.getTrackData()){
            trackData.playInitialize(this);
        }
    }
    
    public boolean hasNextPlayer(){return this.nextPlayer != null;}
    
    /**
     * このプレイヤーのIDを取得
     * @return
     */
    public int getID() {return id;}
    
    
    /**
     * このプレイヤーが再生している録画データを取得
     * @return RecordData
     */
    public RecordData getRecordData() {return recordData;}
    
    
    /**
     * 録画データを再生して見せるプレイヤーを追加
     * @param audience
     */
    public void addAudience(Player audience){audiences.add(audience);}
    
    
    /**
     * 録画データを再生して見せるプレイヤーを取得
     * @return List<Player>
     */
    public List<Player> getAudiences() {return audiences;}
    
    
    /**
     * 録画データを再生して見せるプレイヤーを設定
     * @return List<Player>
     */
    public void setAudiences(List<Player> audiences) {this.audiences = audiences;}
    
    @Override
    public void run() {
        
        runnableSet.forEach(Runnable::run);
        
        if(isPause){
            for(TrackData trackData : recordData.getTrackData()){
                if(trackData instanceof PlayerTrackData){
                    PlayerTrackData playerTrackData = (PlayerTrackData) trackData;
                    try {
                        Location npcLocation = NMSUtil.getEntityLocation(playerTrackData.getNPC(this.getID()));
                        
                        TaskHandler.supplyWorldSync(npcLocation.getWorld(),
                                () -> npcLocation.clone().add(0.0, -0.1, 0.0).getBlock().getType())
                                .thenAccept(material -> {
                                    if(!material.toString().endsWith("AIR")){
                                        pausing = true;
                                    }else{
                                        recordData.playTrackData(this, tick);
    
                                        if(tick == endTick){
                                            if(playMode == PlayMode.LOOP) {
                                                tick = recordData.getLoopBackTick() == 0 ? startTick : recordData.getLoopBackTick();
                                            }else{
                                                this.cancel();
                                            }
                                        }
                                        
                                        tick++;
                                    }
                                });
                        
                    }catch (Exception e){e.printStackTrace();}
                    playerTrackData.playPlayerLook(this, tick);
                    
                    break;
                }
            }
            
            return;
        }
        
        recordData.playTrackData(this, tick);
        
        if(tick == endTick){
            if(playMode == PlayMode.LOOP) {
                tick = recordData.getLoopBackTick() == 0 ? startTick : recordData.getLoopBackTick();
            }else{
                this.cancel();
            }
        }
        
        tick++;
    }
    
    public void start(PlayMode playMode){
        this.playMode = playMode;
        
        TaskHandler.runSync(() -> {
            ScenePlayer.this.initialize();
            ScenePlayer.this.runTaskTimerAsynchronously(Cinema4C.getPlugin(), 5, 1);
        });
    }
    
    @Override
    public synchronized void cancel() throws IllegalStateException {
        AsyncScenePlayFinishEvent endEvent = new AsyncScenePlayFinishEvent(this);
        Cinema4C.getPlugin().getServer().getPluginManager().callEvent(endEvent);
        
        cancelRunnableSet.forEach(Runnable::run);
        
        for(TrackData trackData : recordData.getTrackData()){
            trackData.playEnd(this);
        }
        if(nextPlayer != null){
            nextPlayer.start(playMode);
        }
        if(movieData != null){
            if(movieData.getAfterLocation() != null) {
                TaskHandler.runSync(() -> {
                    for (Player audience : audiences) {
                        audience.setGameMode(GameMode.ADVENTURE);
                        audience.teleport(movieData.getAfterLocation());
                    }
                });
            }
            AsyncMoviePlayFinishEvent event = new AsyncMoviePlayFinishEvent(movieID, movieData);
            Cinema4C.getPlugin().getServer().getPluginManager().callEvent(event);
        }
        super.cancel();
    }
    
    
    
    public enum PlayMode{
        ALL_PLAY,
        TRACK_DATA_ONLY,
        CAMERA_ONLY,
        LOOP
    }
}
