package be4rjp.cinema4c.data.play;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.exception.DifferentVersionException;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.RecordManager;
import be4rjp.cinema4c.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 複数のシーン(RecordData)から成るムービーデータ
 */
public class MovieData {
    
    private static final int VERSION = 1;

    private static int playID = 0;
    
    private final String name;
    private final List<SceneData> sceneDataList = new ArrayList<>();
    //再生後にテレポートさせる場所
    private Location afterLocation = null;
    
    public MovieData(String name){
        this.name = name;
    }
    
    public List<SceneData> getSceneDataList() {return sceneDataList;}
    
    public void setAfterLocation(Location afterLocation) {this.afterLocation = afterLocation;}
    
    public Location getAfterLocation() {return afterLocation;}
    
    public void loadData() throws DifferentVersionException {
        this.sceneDataList.clear();
    
        File file = new File("plugins/Cinema4C/movie", name + ".yml");
        createFile(file);
    
        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
    
        int ver = yml.getInt("version");
        if (ver != VERSION)
            throw new DifferentVersionException("Cannot load movie data due to different version. System version->" + VERSION + " File version->" + ver);
        
        if(yml.contains("after-location")){
            this.afterLocation = ConfigUtil.toLocation(yml.getString("after-location"));
        }
        
        List<String> sceneList = yml.getStringList("scenes");
        for(String line : sceneList){
            //record-data, start-tick, stop-tick, [world, x, y, z]
            line = line.replace(" ", "");
            line = line.replace("[", "");
            line = line.replace("]", "");
            
            String[] args = line.split(",");
            String dataName = args[0];
            
            RecordData recordData = RecordManager.getRecordData(dataName);
            
            if(recordData == null){
                Cinema4C.getPlugin().getLogger().warning("At '" + name + "'");
                Cinema4C.getPlugin().getLogger().warning("Record data for the name '" + dataName + "' was not found.");
                continue;
            }
            
            int startTick = Integer.parseInt(args[1]);
            int stopTick = Integer.parseInt(args[2]);
    
            World world = Bukkit.getWorld(args[3]);
            double x = Double.parseDouble(args[4]);
            double y = Double.parseDouble(args[5]);
            double z = Double.parseDouble(args[6]);
            Location location = new Location(world, x, y, z);
            
            SceneData sceneData = new SceneData(recordData, startTick, stopTick, location);
            this.sceneDataList.add(sceneData);
        }
    }
    
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
     * ムービーを再生する
     * @param audiences ムービーを再生するプレイヤー
     * @return ムービーの再生ID、AsyncMoviePlayFinishEventで取得できるIDと一致します
     */
    public int play(Set<Player> audiences){
        if(this.sceneDataList.size() == 0) return -1;

        playID++;
        int index = 1;
        ScenePlayer beforePlayer = null;
        ScenePlayer firstPlayer = null;
        for(SceneData sceneData : sceneDataList){
            ScenePlayer scenePlayer = new ScenePlayer(sceneData.getRecordData(), sceneData.getBaseLocation(), sceneData.getStartTick(), sceneData.getStopTick());
            scenePlayer.setAudiences(audiences);
            if(beforePlayer != null) beforePlayer.setNextPlayer(scenePlayer);
            if(index == sceneDataList.size()){
                scenePlayer.setMovieData(this);
            }
            scenePlayer.setMoviePlayID(playID);
            if(index == 1) firstPlayer = scenePlayer;
            beforePlayer = scenePlayer;
            index++;
        }
        //firstPlayer.initialize();
        firstPlayer.start(ScenePlayer.PlayMode.ALL_PLAY);

        return playID;
    }
    
    
    public class SceneData {
        
        private final RecordData recordData;
        private final Location baseLocation;
        private final int startTick;
        private final int stopTick;
        
        public SceneData(RecordData recordData, int startTick, int stopTick, Location baseLocation){
            this.recordData = recordData;
            this.baseLocation = baseLocation;
            this.startTick = startTick;
            this.stopTick = stopTick;
        }
    
        public int getStartTick() {return startTick;}
    
        public int getStopTick() {return stopTick;}
    
        public Location getBaseLocation() {return baseLocation;}
    
        public RecordData getRecordData() {return recordData;}
    }
}
