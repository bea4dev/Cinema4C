package be4rjp.cinema4c.player;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.data.play.MovieData;
import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
import be4rjp.cinema4c.event.AsyncMoviePlayFinishEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

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
    private Set<Player> audiences;
    //再生する基準位置、録画時に指定したリージョンの最小位置
    private final Location baseLocation;
    
    //現在の生成時間
    private int tick;
    //再生終了時間
    private int endTick;
    
    //次に再生するプレイヤー
    private ScenePlayer nextPlayer = null;
    
    private MovieData movieData = null;

    private int movieID = -1;
    
    private PlayMode playMode = PlayMode.ALL_PLAY;
    
    public ScenePlayer(RecordData recordData, Location baseLocation, int startTick, int stopTick){
        this.id = playerID;
        playerID++;
        
        this.recordData = recordData;
        this.audiences = new HashSet<>();
    
        this.baseLocation = baseLocation;
        
        this.tick = startTick;
        
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
    
    public void setMovieData(MovieData movieData, int movieID) {
        this.movieData = movieData;
        this.movieID = movieID;
    }
    
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
     * このプレイヤーの再生基準位置を取得
     * @return Location
     */
    public Location getBaseLocation() {return baseLocation.clone();}
    
    
    /**
     * 録画データを再生して見せるプレイヤーを追加
     * @param audience
     */
    public void addAudience(Player audience){audiences.add(audience);}
    
    
    /**
     * 録画データを再生して見せるプレイヤーを取得
     * @return Set<Player>
     */
    public Set<Player> getAudiences() {return audiences;}
    
    
    /**
     * 録画データを再生して見せるプレイヤーを設定
     * @return Set<Player>
     */
    public void setAudiences(Set<Player> audiences) {this.audiences = audiences;}
    
    @Override
    public void run() {
        
        recordData.playTrackData(this, tick);
        
        if(tick == endTick) this.cancel();
        
        tick++;
    }
    
    public void start(PlayMode playMode){
        this.playMode = playMode;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                ScenePlayer.this.initialize();
                ScenePlayer.this.runTaskTimerAsynchronously(Cinema4C.getPlugin(), 5, 1);
            }
        }.runTask(Cinema4C.getPlugin());
    }
    
    @Override
    public synchronized void cancel() throws IllegalStateException {
        for(TrackData trackData : recordData.getTrackData()){
            trackData.playEnd(this);
        }
        if(nextPlayer != null){
            nextPlayer.start(playMode);
        }
        if(movieData != null){
            if(movieData.getAfterLocation() != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player audience : audiences) {
                            audience.setGameMode(GameMode.ADVENTURE);
                            audience.teleport(movieData.getAfterLocation());
                        }
                    }
                }.runTask(Cinema4C.getPlugin());
            }
            AsyncMoviePlayFinishEvent event = new AsyncMoviePlayFinishEvent(movieID, movieData);
            Cinema4C.getPlugin().getServer().getPluginManager().callEvent(event);
        }
        super.cancel();
    }
    
    
    
    public enum PlayMode{
        ALL_PLAY,
        TRACK_DATA_ONLY,
        CAMERA_ONLY
    }
}
