package com.fzx.chat.controller;


import com.fzx.chat.service.cometd.util.LocalSessionManager;
import com.fzx.chat.utils.IpUtil;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.LocalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/cometdAp")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private final RedisTemplate<String, String> redisTemplate;

    private final LocalSessionManager localSessionManager;

    @Autowired
    public TestController(LocalSessionManager localSessionManager,
                          @Qualifier("redisStrTemplate") RedisTemplate<String, String> redisTemplate) {
        this.localSessionManager = localSessionManager;
        this.redisTemplate = redisTemplate;
    }


    @RequestMapping("index")
    public String index(HttpServletRequest request) {
        String clientIpAddr = IpUtil.getClientIpAddr(request);

        LocalSession localSession = localSessionManager.getLocalSession();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("content", clientIpAddr + "：" + LocalDateTime.now() + "调用了/cometdAp/index接口");
        hashMap.put("chat", "123");
        hashMap.put("user", "123");
        hashMap.put("room", "/chat/privatechat");

        ClientSessionChannel channel = localSession.getChannel("/chat/privatechat");
        channel.publish(hashMap, message -> {
            if (message.isSuccessful()) {
                logger.info("发布成功");
            }
        });

        redisTemplate.opsForValue().set("lookin:privatechat:TestController", "123");

        String string = redisTemplate.opsForValue().get("lookin:privatechat:TestController");
        logger.info("lookin:privatechat:TestController:{}", string);

        return "index";
    }


}
