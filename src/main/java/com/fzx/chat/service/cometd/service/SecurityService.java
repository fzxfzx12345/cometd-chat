package com.fzx.chat.service.cometd.service;

import org.cometd.annotation.Service;
import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Service
@Component
public class SecurityService implements SecurityPolicy, ServerSession.RemoveListener {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    private final RedisTemplate<String, String> redisTemplate;

    public SecurityService(@Qualifier("redisStrTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean canCreate(BayeuxServer server, ServerSession session, String channelId, ServerMessage message) {
        return session != null && session.isLocalSession() || !ChannelId.isMeta(channelId);
    }

    @Override
    public boolean canHandshake(BayeuxServer server, ServerSession session, ServerMessage message) {

        redisTemplate.opsForValue().set("lookin:privatechat:SecurityService", "123");
        String string = redisTemplate.opsForValue().get("lookin:privatechat:SecurityService");
        logger.info("lookin:privatechat:SecurityService:{}", string);

        //TODO 握手认证
        logger.info("canHandshake: session={}, message={}", session, message);
        Map<String, Object> credentials = (Map<String, Object>) message.get("com.acme.credentials");
//        String user = credentials.get("user").toString();
//        String token = credentials.get("token").toString();
//        logger.info("user={}, token={}", user, token);
//
//        if (!"xyzsecretabc".equals(token)) {
//            logger.info("canHandshake: false");
//            return true;
//        }
//        String sessionId = session.getId();
//        logger.info("canHandshake: sessionId={}", sessionId);
//        //TODO:绑定用户Id和SessionId

        session.addListener(this);


//        redisTemplate.opsForValue().set("lookin:privatechat:" + user, sessionId);

        logger.info("canHandshake: true");
        return true;
    }

    @Override
    public boolean canPublish(BayeuxServer server, ServerSession session, ServerChannel channel, ServerMessage message) {

        if (session != null && session.isLocalSession()) {
            logger.info("本地session");
        }

        return session != null && session.isHandshook() && !channel.isMeta();
    }


    @Override
    public boolean canSubscribe(BayeuxServer server, ServerSession session, ServerChannel channel, ServerMessage message) {
        // TODO 自定义订阅认证
        channel.addListener(new ServerChannelService());
        return session != null && session.isLocalSession() || !channel.isMeta();
    }

    @Override
    public void removed(ServerSession session, boolean timeout) {
        logger.info(session.toString() + "退出了系统" + (timeout ? "超时" : "自己退出"));
    }
}
