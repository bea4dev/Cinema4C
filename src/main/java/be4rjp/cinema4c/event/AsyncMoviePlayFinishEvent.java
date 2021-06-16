package be4rjp.cinema4c.event;

import be4rjp.cinema4c.data.play.MovieData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncMoviePlayFinishEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {return HANDLERS;}

    public static HandlerList getHandlerList() {return HANDLERS;}


    private final int playID;
    private final MovieData movieData;

    public AsyncMoviePlayFinishEvent(int playID, MovieData movieData){
        super(true);
        this.playID = playID;
        this.movieData = movieData;
    }


    public int getPlayID() {return playID;}

    public MovieData getMovieData() {return movieData;}
}
