package top.fissile.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import top.fissile.manager.SocketManager;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 服务端和客户端在进行握手挥手时会被执行
 * @Author duln
 * @Date 2019/12/27 11:22
 * @Version 1.0
 */
@Slf4j
@Component
public class WebSocketDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    @Autowired
    private SimpMessagingTemplate template;
    @Override
    public WebSocketHandler decorate(WebSocketHandler webSocketHandler) {
        return new WebSocketHandlerDecorator(webSocketHandler) {
            // 连接时执行（2）
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                log.info("有人连接啦  sessionId = {}", session.getId());
                log.info(session.toString());
                Principal principal = session.getPrincipal();
                if (principal != null) {
                    String name = principal.getName();
                    log.info("key = {} 存入", name);
                    // 身份校验成功，缓存socket连接
                    SocketManager.add(name, session);
                    msgType("1", name);
                }


                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                log.info("有人退出连接啦  sessionId = {}", session.getId());
                Principal principal = session.getPrincipal();
                if (principal != null) {
                    // 身份校验成功，移除socket连接
                    String name = principal.getName();
                    SocketManager.remove(name);
                    msgType("2", name);
                }
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }

    /**
     * @Description 消息类型
     * @param type 1进入 2退出
     * @param name 触发事件的人
     */
    public void msgType(String type, String name) {
        if ("1".equals(type)) {
            noticeMsg(name + "进来了");
        } else {
            noticeMsg(name + "离开了");
        }
        noticeCount();
    }

    /**
     * @Description 给订阅“/topic/sendTopic”的发消息
     * @param msg
     */
    public void noticeMsg(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", msg);
        map.put("type", "系统消息");
        template.convertAndSend("/topic/receiveMsg", map);
    }

    /**
     * @Description 给订阅“/topic/count”的发消息
     */
    public void noticeCount() {
        Map<String, Object> map = SocketManager.get();
        template.convertAndSend("/topic/count", map);
    }
}
