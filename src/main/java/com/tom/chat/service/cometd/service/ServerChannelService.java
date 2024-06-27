package com.tom.chat.service.cometd.service;

import org.cometd.annotation.Service;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Service
@Component
public class ServerChannelService implements ServerChannel.SubscriptionListener {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelService.class);

    @Override
    public void subscribed(ServerSession session, ServerChannel channel, ServerMessage message) {
        logger.info(session.toString() + "加入了群聊:" + channel.toString());
        for (ServerSession subscriber : channel.getSubscribers()) {
            logger.info("群聊情况：" + subscriber.toString());
        }
        logger.info("共" + channel.getSubscribers().size() + "人");
    }

    @Override
    public void unsubscribed(ServerSession session, ServerChannel channel, ServerMessage message) {
        logger.info(session.toString() + "退出入了群聊:" + channel.toString());
        for (ServerSession subscriber : channel.getSubscribers()) {
            logger.info("群聊情况：" + subscriber.toString());
        }
        logger.info("共" + channel.getSubscribers().size() + "人");
    }
}
