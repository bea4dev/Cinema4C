package be4rjp.cinema4c.event;

import be4rjp.cinema4c.player.ScenePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncScenePlayFinishEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    @Override
    public HandlerList getHandlers() {return HANDLERS;}
    
    public static HandlerList getHandlerList() {return HANDLERS;}
    
    
    private final ScenePlayer scenePlayer;
    
    public AsyncScenePlayFinishEvent(ScenePlayer scenePlayer){
        super(true);
        this.scenePlayer = scenePlayer;
    }
    
    
    public ScenePlayer getScenePlayer(){return this.scenePlayer;}
}
