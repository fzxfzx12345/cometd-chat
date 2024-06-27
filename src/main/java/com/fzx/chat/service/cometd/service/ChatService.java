package com.fzx.chat.service.cometd.service;

import com.fzx.chat.enums.ChatMessageEnum;
import com.fzx.chat.utils.SpringBeanUtil;
import org.cometd.annotation.Configure;
import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.*;
import org.cometd.server.authorizer.GrantAuthorizer;
import org.cometd.server.filter.DataFilterMessageListener;
import org.cometd.server.filter.JSONDataFilter;
import org.cometd.server.filter.NoMarkupFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ConcurrentMap<String, Map<String, String>> _members = new ConcurrentHashMap<>();

    @Session
    private ServerSession _session;

    @Inject
    private BayeuxServer _bayeux;


    @Configure({"/chat/privatechat"})
    protected void configureChatStar1(ConfigurableServerChannel channel) {
        channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
    }

    @Listener({"/chat/privatechat"})
    public void handleChatStar1(ServerSession client, ServerMessage message) {
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(RedisTemplate.class, String.class, String.class);
        RedisTemplate<String, String> redisTemplate = SpringBeanUtil.getBeanWithResolvableType(resolvableType, "redisStrTemplate");

        redisTemplate.opsForValue().set("lookin:privatechat:chatService", "123");

        String string = redisTemplate.opsForValue().get("lookin:privatechat:chatService");
        logger.info("lookin:privatechat:chatService:{}", string);

    }


    /**
     * 单聊
     *
     * @param channel
     */
    @Configure({"/chat/lookin/privatechat/**"})
    protected void configureChatStar(ConfigurableServerChannel channel) {
        channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
    }

    /**
     * 单聊监听
     *
     * @param client
     * @param message
     */
    @Listener({"/chat/lookin/privatechat/**"})
    public void handleChatStar(ServerSession client, ServerMessage message) {
        Map<String, Object> dataAsMap = message.getDataAsMap();
        String messageToUserID = dataAsMap.get(ChatMessageEnum.to_user_id.name()).toString();
        String messageFromUserID = dataAsMap.get(ChatMessageEnum.from_user_id.name()).toString();
        String messageContent = dataAsMap.get(ChatMessageEnum.content.name()).toString();
        String messageRoom = dataAsMap.get(ChatMessageEnum.room.name()).toString();

        String messageToClientID = "";

        ServerSession session = _bayeux.getSession(messageToClientID); //目标用户
        if (session != null) {
            ServerMessage.Mutable forward = _bayeux.newMessage();
            forward.setChannel(messageRoom);
            forward.setId(message.getId());
            forward.setData(dataAsMap);
            session.deliver(_session, forward, Promise.noop());
        } else {
            logger.info("用户不在线");
        }

    }


    /**
     * 群聊
     *
     * @param channel
     */
    @Configure({"/chat/group"})
    protected void configureChatStarStar(ConfigurableServerChannel channel) {
        DataFilterMessageListener noMarkup = new DataFilterMessageListener(new NoMarkupFilter(), new BadWordFilter());
        channel.addListener(noMarkup);
        channel.addAuthorizer(GrantAuthorizer.GRANT_SUBSCRIBE_PUBLISH);
        channel.setPersistent(true);
    }

    /**
     * 群聊监听
     *
     * @param client
     * @param message
     */
    @Listener({"/chat/group"})
    public void handleChat(ServerSession client, ServerMessage message) {
        logger.info("Session: {}, Message{}", client, message);
    }


    /**
     * @param channel
     */
    @Configure("/service/notice")
    protected void configureMembers(ConfigurableServerChannel channel) {
        channel.addAuthorizer(GrantAuthorizer.GRANT_PUBLISH);
        channel.setPersistent(true);
    }


    @Listener("/service/notice")
    public void handleMembership(ServerSession client, ServerMessage message) {
        logger.info("Session: {}, Message{}", client, message);
        Map<String, Object> data = message.getDataAsMap();
        String room = ((String) data.get("room")).substring("/chat/".length());
        Map<String, String> roomMembers = _members.get(room);
        if (roomMembers == null) {
            Map<String, String> new_room = new ConcurrentHashMap<>();
            roomMembers = _members.putIfAbsent(room, new_room);
            if (roomMembers == null) {
                roomMembers = new_room;
            }
        }
        Map<String, String> members = roomMembers;
        String userName = (String) data.get("user");
        members.put(userName, client.getId());
        /**
         * 删除会话时调用的回调。
         * 形参:
         * session – 已删除的会话
         * timeout – 会话是否因超时而被删除
         */
        client.addListener(new ServerSession.RemoveListener() {
            @Override
            public void removed(ServerSession session, boolean timeout) {
                members.values().remove(session.getId());
                broadcastMembers(room, members.keySet());
            }
        });
        broadcastMembers(room, members.keySet());
    }

    private void broadcastMembers(String room, Set<String> members) {
        // Broadcast the new members list
        ClientSessionChannel channel = _session.getLocalSession().getChannel("/chat/" + room);
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("user", members.toString());
        stringStringHashMap.put("content", "chat" + members);
        channel.publish(stringStringHashMap);

        ClientSessionChannel channel1 = _session.getLocalSession().getChannel("/chat/" + room);
        HashMap<String, String> stringStringHashMap1 = new HashMap<>();
        stringStringHashMap1.put("content", "消息发送成功");
        channel1.publish(stringStringHashMap1);

    }

    @Configure("/service/privatechat")
    protected void configurePrivateChat(ConfigurableServerChannel channel) {
        DataFilterMessageListener noMarkup = new DataFilterMessageListener(new NoMarkupFilter(), new BadWordFilter());
        channel.setPersistent(true);
        channel.addListener(noMarkup);
        channel.addAuthorizer(GrantAuthorizer.GRANT_PUBLISH);
    }

    @Listener("/service/privatechat")
    public void privateChat(ServerSession client, ServerMessage message) {
        logger.info("Session: {}, Message{}", client, message);
        Map<String, Object> data = message.getDataAsMap();
        String room = ((String) data.get("room")).substring("/chat/".length());
        Map<String, String> membersMap = _members.get(room);
        if (membersMap == null) {
            Map<String, String> new_room = new ConcurrentHashMap<>();
            membersMap = _members.putIfAbsent(room, new_room);
            if (membersMap == null) {
                membersMap = new_room;
            }
        }
        String[] peerNames = ((String) data.get("peer")).split(",");
        ArrayList<ServerSession> peers = new ArrayList<>(peerNames.length);

        for (String peerName : peerNames) {
            String peerId = membersMap.get(peerName);
            if (peerId != null) {
                ServerSession peer = _bayeux.getSession(peerId);
                if (peer != null) {
                    peers.add(peer);
                }
            }
        }

        if (!peers.isEmpty()) {
            Map<String, Object> chat = new HashMap<>();
            String text = (String) data.get("chat");
            chat.put("chat", text);
            chat.put("user", data.get("user"));
            chat.put("scope", "private");
            ServerMessage.Mutable forward = _bayeux.newMessage();
            forward.setChannel("/chat/" + room);
            forward.setId(message.getId());
            forward.setData(chat);

            // test for lazy messages
            if (text.lastIndexOf("lazy") > 0) {
                forward.setLazy(true);
            }

            for (ServerSession peer : peers) {
                if (peer != client) {
                    peer.deliver(_session, forward, Promise.noop());
                }
            }
            client.deliver(_session, forward, Promise.noop());
        }
    }

    static class BadWordFilter extends JSONDataFilter {
        @Override
        protected Object filterString(ServerSession session, ServerChannel channel, String string) {
            if (string.contains("dang")) {
                throw new AbortException();
            }
            return string;
        }
    }
}
