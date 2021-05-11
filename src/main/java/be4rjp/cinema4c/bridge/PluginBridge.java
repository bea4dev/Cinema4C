package be4rjp.cinema4c.bridge;

public interface PluginBridge {
    
    /**
     * コマンドを実行
     * @param command コマンド本文
     */
    void executeCommand(String command);
    
}
