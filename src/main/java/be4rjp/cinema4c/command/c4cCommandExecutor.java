package be4rjp.cinema4c.command;

import be4rjp.cinema4c.Cinema4C;
import be4rjp.cinema4c.data.play.MovieData;
import be4rjp.cinema4c.data.record.RecordData;
import be4rjp.cinema4c.data.record.tracking.TrackData;
import be4rjp.cinema4c.player.PlayManager;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.cinema4c.recorder.RecordManager;
import be4rjp.cinema4c.recorder.SceneRecorder;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class c4cCommandExecutor implements CommandExecutor, TabExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args == null) return false;
        if (args.length == 0) return false;
    
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "このコマンドはコンソールから実行できません。");
            return true;
        }
        
        switch (args[0]){
            case "record":{
                if(args.length < 3){
                    sender.sendMessage(ChatColor.RED + "録画データの名前を指定してください。");
                    return true;
                }
    
                //c4c record create [name]
                if(args[1].equals("create")) {
                    if (RecordManager.getRecordData(args[2]) != null) {
                        sender.sendMessage(ChatColor.RED + "指定された名前の録画データが既に存在しています。");
                        return true;
                    }
    
                    RecordManager.createNewRecordData(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "作成しました。");
                    return true;
                }
    
                //c4c record remove [name]
                if(args[1].equals("remove")) {
                    RecordManager.recordDataMap.remove(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "削除しました。");
                    return true;
                }
    
                //c4c record play [name]
                if(args[1].equals("play")) {
                    RecordData recordData = RecordManager.getRecordData(args[2]);
                    if(recordData == null){
                        sender.sendMessage(ChatColor.RED + "指定された名前の録画データが見つかりません。");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
                    SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
                    LocalSession localSession = sessionManager.get(wePlayer);
    
                    com.sk89q.worldedit.world.World selectionWorld = localSession.getSelectionWorld();
                    Region region;
                    try {
                        if (selectionWorld == null) throw new IncompleteRegionException();
                        region = localSession.getSelection(selectionWorld);
                    } catch (IncompleteRegionException ex) {
                        sender.sendMessage(ChatColor.GREEN + "範囲が指定されていません。");
                        return true;
                    }
                    
                    BlockVector3 min = region.getMinimumPoint();
                    World world = BukkitAdapter.adapt(region.getWorld());
                    Vector minLocation = new Vector(min.getX(), min.getY(), min.getZ());
                    
                    ScenePlayer scenePlayer = PlayManager.createScenePlayer(recordData, minLocation.toLocation(world), 0, 0);
                    scenePlayer.addAudience(player);
                    //scenePlayer.initialize();
                    scenePlayer.start(ScenePlayer.PlayMode.ALL_PLAY);
                    sender.sendMessage(ChatColor.GREEN + "再生を開始しました。");
                    return true;
                }
    
                //c4c record save [name]
                if(args[1].equals("save")) {
                    RecordData recordData = RecordManager.getRecordData(args[2]);
                    if(recordData == null){
                        sender.sendMessage(ChatColor.RED + "指定された名前の録画データが見つかりません。");
                        return true;
                    }
                    recordData.saveData();
                    sender.sendMessage(ChatColor.GREEN + "保存しました。");
                    return true;
                }
                
                break;
            }
            
            
            case "recorder":{
                if(args.length < 2){
                    return false;
                }
    
                //c4c recorder create [record-data]
                if(args[1].equals("create")){
                    if(args.length < 3){
                        return false;
                    }
                    RecordData recordData = RecordManager.getRecordData(args[2]);
                    
                    if(recordData == null){
                        sender.sendMessage(ChatColor.RED + "指定された名前の録画データが見つかりません。");
                        return true;
                    }
    
                    Player player = (Player) sender;
                    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
                    SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
                    LocalSession localSession = sessionManager.get(wePlayer);
    
                    com.sk89q.worldedit.world.World selectionWorld = localSession.getSelectionWorld();
                    Region region;
                    try {
                        if (selectionWorld == null) throw new IncompleteRegionException();
                        region = localSession.getSelection(selectionWorld);
                    } catch (IncompleteRegionException ex) {
                        sender.sendMessage(ChatColor.GREEN + "範囲が指定されていません。");
                        return true;
                    }
    
                    BlockVector3 max = region.getMaximumPoint();
                    BlockVector3 min = region.getMinimumPoint();
    
                    World world = BukkitAdapter.adapt(region.getWorld());
    
                    Vector maxLocation = new Vector(max.getX(), max.getY(), max.getZ());
                    Vector minLocation = new Vector(min.getX(), min.getY(), min.getZ());
    
                    RecordManager.createSceneRecorder(recordData, minLocation.toLocation(world), maxLocation.toLocation(world));
                    sender.sendMessage(ChatColor.GREEN + "作成しました。");
                    return true;
                }
                
                //c4c recorder add [type] [player]
                if(args[1].equals("add")){
                    if(args.length < 4){
                        return false;
                    }
    
                    TrackData.DataType dataType;
                    try{
                        dataType = TrackData.DataType.valueOf(args[2]);
                    }catch (IllegalArgumentException e){
                        sender.sendMessage(ChatColor.RED + "間違ったタイプが指定されました。");
                        return true;
                    }
                    
                    Player player = Bukkit.getPlayer(args[3]);
                    if(player == null){
                        sender.sendMessage(ChatColor.RED + "指定されたプレイヤーが見つかりませんでした。");
                        return true;
                    }
    
                    SceneRecorder sceneRecorder = RecordManager.getRecorder();
                    if(sceneRecorder == null){
                        sender.sendMessage(ChatColor.RED + "レコーダーがまだ作成されていません。");
                        return true;
                    }
                    
                    switch (dataType){
                        case MESSAGE:{
                            sender.sendMessage(ChatColor.RED + "有効なタイプではありません。");
                            return true;
                        }
                        
                        case CAMERA:{
                            RecordManager.addCameraTrackingPlayer(player);
                            break;
                        }
                        
                        case NPC:{
                            RecordManager.addTrackingPlayer(player);
                            break;
                        }
                    }
                    
                    sender.sendMessage(ChatColor.GREEN + "追加しました。");
                    return true;
                }
    
                //c4c recorder start
                if(args[1].equals("start")) {
                    SceneRecorder sceneRecorder = RecordManager.getRecorder();
                    if(sceneRecorder == null){
                        sender.sendMessage(ChatColor.RED + "レコーダーがまだ作成されていません。");
                        return true;
                    }
                    RecordManager.startRecording();
                }
    
                //c4c recorder stop
                if(args[1].equals("stop")) {
                    SceneRecorder sceneRecorder = RecordManager.getRecorder();
                    if(sceneRecorder == null){
                        sender.sendMessage(ChatColor.RED + "レコーダーがまだ作成されていません。");
                        return true;
                    }
                    RecordManager.stopRecording();
                }
                break;
            }
            
            case "movie":{
                if(args.length < 3){
                    return false;
                }
    
                //c4c movie play [movie-data]
                if(args[1].equals("play")){
                    Player player = (Player) sender;
                    MovieData movieData = PlayManager.getMovieData(args[2]);
                    if(movieData == null){
                        sender.sendMessage(ChatColor.RED + "指定された名前のムービーデータが見つかりません。");
                        return true;
                    }
    
                    List<Player> players = new ArrayList<>();
                    players.add(player);
                    movieData.play(players);
                    sender.sendMessage(ChatColor.GREEN + "再生を開始しました。");
                    return true;
                }
                
                break;
            }
            
            case "reload":{
                Cinema4C.loadData();
                sender.sendMessage(ChatColor.GREEN + "完了しました。");
                break;
            }
    
            case "help":{
                sender.sendMessage(ChatColor.GREEN + "---------------- Command list ------------------");
                sender.sendMessage("/c4c record create [name]");
                sender.sendMessage("/c4c record remove [name]");
                sender.sendMessage("/c4c record play [name]");
                sender.sendMessage("/c4c record save [name]");
                sender.sendMessage("/c4c recorder create [record-data]");
                sender.sendMessage("/c4c recorder add [type] [player]");
                sender.sendMessage("/c4c recorder start");
                sender.sendMessage("/c4c movie play [movie-data]");
                sender.sendMessage(ChatColor.GREEN + "-----------------------------------------------");
                break;
            }
        }
        
        return true;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        
        List<String> list = new ArrayList<>();
        
        //c4c record create [name]
        //c4c record remove [name]
        //c4c record play [name]
        //c4c record save [name]
        //c4c recorder create [record-data]
        //c4c recorder add [type] [player]
        //c4c recorder start
        //c4c recorder stop
        //c4c movie play [movie-data]
        
        if (args.length == 1) {
            list.add("record");
            list.add("recorder");
            list.add("movie");
            list.add("reload");
            list.add("help");
            
            return list;
        }
        
        if(args.length == 2){
            switch (args[0]){
                case "record":{
                    list.add("create");
                    list.add("remove");
                    list.add("play");
                    list.add("save");
                    break;
                }
                
                case "recorder":{
                    list.add("create");
                    list.add("add");
                    list.add("start");
                    list.add("stop");
                    break;
                }
                
                case "movie":{
                    list.add("play");
                    break;
                }
            }
            return list;
        }
    
        if(args.length == 3){
            switch (args[0]){
                case "record":{
                    if(!args[1].equals("create")) {
                        list = new ArrayList<>(RecordManager.recordDataMap.keySet());
                    }else{
                        list.add("[data-name]");
                    }
                    break;
                }
            
                case "recorder":{
                    if(args[1].equals("create")){
                        list = new ArrayList<>(RecordManager.recordDataMap.keySet());
                    }
                    if(args[1].equals("add")){
                        for(TrackData.DataType dataType : TrackData.DataType.values()){
                            if(dataType != TrackData.DataType.MESSAGE){
                                list.add(dataType.toString());
                            }
                        }
                    }
                    break;
                }
            
                case "movie":{
                    if(args[1].equals("play")){
                        list = new ArrayList<>(PlayManager.movieDataMap.keySet());
                    }
                    break;
                }
            }
            return list;
        }
        return null;
    }
}
