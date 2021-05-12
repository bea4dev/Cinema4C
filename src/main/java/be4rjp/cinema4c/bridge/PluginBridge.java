package be4rjp.cinema4c.bridge;

import be4rjp.cinema4c.player.ScenePlayer;

public interface PluginBridge {
    
    /**
     * コマンドを実行
     * @param command コマンド本文
     */
    void executeCommand(ScenePlayer scenePlayer, String command);
    
}
