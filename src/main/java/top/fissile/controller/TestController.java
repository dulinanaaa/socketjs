package top.fissile.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;
import top.fissile.manager.SocketManager;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 哥，写点什么吧
 * demo地址：http://123.56.157.29:3333/html/index.html
 * @Author duln
 * @Date 2019/12/27 11:27
 * @Version 1.0
 */
@RestController
@Slf4j
public class TestController {
    @Autowired
    private SimpMessagingTemplate template;

    /**
     * @Description 接收客户端的消息
     * @param principal 这是在拦截器中设置的数据，之前只用token给设置个name值
     * @param msg 客户端发送过来的消息（也可以用map接收，前端就需要obj转json）
     */
    // 只和前端的发送参数相关，类似RequestMapping，前端直接clent.send('/sendAllUser',...)就可以
    @MessageMapping("/sendAllUser")
    // 貌似得和@MessageMapping连用才生效，也就限制了一般只能用在controller中
    // 区别于@SendToUser，@SendTo是用来服务器给客户端群发的：value就是群发的destination，方法返回值就是群发数据
    // 如果返回值为Map，则客户端收到的是application/json格式；如果返回值是String，则收到的是xxx/text格式
    // @SendTo同template.convertAndSend()，只是template可用于任何地方
    @SendTo("/topic/receiveMsg")
    public Map<String, Object> sendAllUser(Principal principal, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "公开");
        map.put("userName", principal.getName());
        map.put("message", msg);
        return map;
    }

    /**
     * 客户端请求获取当前人数信息
     * @return
     */
    @MessageMapping("/getCount")
    public void getCount(Principal principal) {
        Map<String, Object> map = SocketManager.get();
        // 与convertAndSend不同的是：this.destinationPrefix + user
        // super.convertAndSend(this.destinationPrefix + user + destination, payload, headers, postProcessor);
        template.convertAndSendToUser(principal.getName(), "/queue/count", map);
    }

    /**
     * 点对点用户聊天
     * @param map
     */
    @MessageMapping("/sendOneUser")
    public void sendOneUser(Principal principal, Map<String, String> map) {
        log.info("map = {}", map);
        String toName = map.get("userName");
        String fromName = principal.getName();
        WebSocketSession webSocketSession = SocketManager.get(toName);
        if (webSocketSession != null) {
            Map<String, Object> toMap = new HashMap<>();
            toMap.put("type", "私聊");
            toMap.put("message", map.get("message"));
            Map<String, Object> fromMap = new HashMap<>(toMap);
            toMap.put("fromName", fromName);
            toMap.put("toName", "你");

            fromMap.put("fromName", "你");
            fromMap.put("toName", toName);
            template.convertAndSendToUser(toName, "/queue/sendUser", toMap);
            template.convertAndSendToUser(fromName, "/queue/sendUser", fromMap);
        }
    }

}
