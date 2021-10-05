package be4rjp.cinema4c.player;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.data.play.MovieData;
import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.exception.DifferentVersionException;
import org.bukkit.Location;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PlayManager {
    
    public static Map<String, MovieData> movieDataMap = new HashMap<>();
    
    /**
     * 初期化
     */
    public static void initialize(){
        movieDataMap.clear();
    }
    
    /**
     * シーンプレイヤーを作成
     * @param recordData
     * @param minLocation
     * @param startTick
     * @param endTick
     * @return ScenePlayer
     */
    public static ScenePlayer createScenePlayer(RecordData recordData, Location minLocation, int startTick, int endTick){
        ScenePlayer scenePlayer = new ScenePlayer(recordData, minLocation.getWorld(), startTick, endTick);
        return scenePlayer;
    }
    
    
    /**
     * 指定された名前のムービーデータを取得する
     * @param name
     * @return
     */
    public static MovieData getMovieData(String name){
        return movieDataMap.get(name);
    }
    
    
    /**
     * 全てのムービーデータを読み込む
     */
    public static void loadAllMovieData(){
        Cinema4C.getPlugin().getLogger().info("Loading movie data...");
        File dir = new File("plugins/Cinema4C/movie");
    
        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if(files.length == 0){
            Cinema4C.getPlugin().saveResource("movie/sample-movie.yml", false);
            files = dir.listFiles();
        }
    
        if(files != null) {
            for(File file : files){
                Cinema4C.getPlugin().getLogger().info(file.getName());
                String name = file.getName().replace(".yml", "");
                MovieData movieData = new MovieData(name);
                try {
                    movieData.loadData();
                } catch (DifferentVersionException e) {
                    Cinema4C.getPlugin().getLogger().warning(e.getMessage());
                }
                movieDataMap.put(name, movieData);
            }
        }
    }
}
