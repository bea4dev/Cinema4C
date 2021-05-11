package be4rjp.cinema4c.player;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.data.play.MovieData;
import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
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
    
    public void setMovieData(MovieData movieData) {this.movieData = movieData;}
    
    public void initialize(){
        for(TrackData trackData : recordData.getTrackData()){
            trackData.playInitialize(this);
        }
    }
    
    public int getID() {return id;}
    
    public RecordData getRecordData() {return recordData;}
    
    public Location getBaseLocation() {return baseLocation.clone();}
    
    public void addAudience(Player audience){audiences.add(audience);}
    
    public Set<Player> getAudiences() {return audiences;}
    
    public void setAudiences(Set<Player> audiences) {this.audiences = audiences;}
    
    @Override
    public void run() {
        
        recordData.playTrackData(this, tick);
        
        if(tick == endTick) this.cancel();
        
        tick++;
    }
    
    public void start(PlayMode playMode){
        this.playMode = playMode;
        
        this.runTaskTimerAsynchronously(Cinema4C.getPlugin(), 0, 1);
    }
    
    @Override
    public synchronized void cancel() throws IllegalStateException {
        for(TrackData trackData : recordData.getTrackData()){
            trackData.playEnd(this);
        }
        if(nextPlayer != null){
            nextPlayer.initialize();
            nextPlayer.runTaskTimerAsynchronously(Cinema4C.getPlugin(), 0, 1);
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
        }
        super.cancel();
    }
    
    
    
    public enum PlayMode{
        ALL_PLAY,
        TRACK_DATA_ONLY,
        CAMERA_ONLY
    }
}
