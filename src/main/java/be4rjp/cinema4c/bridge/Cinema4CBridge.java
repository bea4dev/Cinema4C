package be4rjp.cinema4c.bridge;

import java.util.HashMap;
import java.util.Map;

public class Cinema4CBridge {
    
    private static Map<String, PluginBridge> bridgeMap;
    
    static {
        initialize();
    }
    
    public static void initialize(){
        bridgeMap = new HashMap<>();
    }
    
    
    /**
     * 拡張機能を実装するサブプラグインを登録します
     * @param name 登録するプラグイン名。録画データのコマンド等で使用される
     * @param pluginBridge PluginBridgeを継承したサブプラグイン側実装クラスのインスタンス
     */
    public void registerPluginBridge(String name, PluginBridge pluginBridge){
        bridgeMap.put(name, pluginBridge);
    }
    
    
    /**
     * 名前からサブプラグインを取得
     * @param name
     * @return
     */
    public PluginBridge getPluginBridge(String name){
        return bridgeMap.get(name);
    }
}
