package top.fissile.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description socket管理器
 * @Author duln
 * @Date 2019/12/27 11:21
 * @Version 1.0
 */
@Slf4j
public class SocketManager {
    private static ConcurrentHashMap<String, WebSocketSession> manager = new ConcurrentHashMap<String, WebSocketSession>();

    public static void add(String key, WebSocketSession webSocketSession) {
        log.info("新添加webSocket连接 {} ", key);
        log.info(webSocketSession.toString());
        manager.put(key, webSocketSession);
    }

    public static void remove(String key) {
        log.info("移除webSocket连接 {} ", key);
        manager.remove(key);
    }

    public static WebSocketSession get(String key) {
        log.info("获取webSocket连接 {}", key);
        return manager.get(key);
    }

    public static Integer count() {
        return CollectionUtils.isEmpty(manager) ? 0 : manager.size();
    }

    public static Map<String, Object> get() {
        Map<String, Object> map = new HashMap<>();
        map.put("count", count());
        map.put("list", manager.keySet());
        return map;
    }
}
