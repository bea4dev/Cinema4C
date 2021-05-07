package be4rjp.cinema4c.util;

import org.bukkit.entity.Player;

public class Title {
    
    private String title = "";
    private String subTitle = "";
    private int fadeIn = 0;
    private int stay = 0;
    private int fadeOut = 0;
    
    public Title(String title, String subTitle, int fadeIn, int stay, int fadeOut){
        this.title = title;
        this.subTitle = subTitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }
    
    public void sendTitle(Player player){
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }
    
    public int getFadeIn() {return fadeIn;}
    
    public int getFadeOut() {return fadeOut;}
    
    public int getStay() {return stay;}
    
    public String getSubTitle() {return subTitle;}
    
    public String getTitle() {return title;}
}
