package com.fzx.chat;

import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.cometd.websocket.client.JettyWebSocketTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


class TomChatApplicationTests {


    Logger logger = LoggerFactory.getLogger(TomChatApplicationTests.class);

    @Test
    void contextLoads() throws Exception {

        BayeuxClient bayeuxclient = getBayeuxClient();

        bayeuxclient.getChannel("/chat/privatechat").subscribe((channel, message) -> logger.info("通道:{}, 消息:{}", channel, message));

        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("content", "hello");
        stringStringHashMap.put("chat", "123");
        stringStringHashMap.put("user", "123");
        stringStringHashMap.put("room", "/chat/privatechat");
        bayeuxclient.getChannel("/chat/privatechat").publish(stringStringHashMap, message -> {
            if (message.isSuccessful()) {
                logger.info("发布成功");
            }
        });

        TimeUnit.SECONDS.sleep(1000);

    }

    private BayeuxClient getBayeuxClient() throws Exception {
        HttpClient httpclient = new HttpClient();
        httpclient.start();
        ClientTransport httptransport = new LongPollingTransport(null, httpclient);

        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.start();
        JettyWebSocketTransport wsTransport = new JettyWebSocketTransport(null, null, webSocketClient);

        BayeuxClient bayeuxclient = new BayeuxClient("http://localhost:8081/cometd", wsTransport, httptransport);

        bayeuxclient.handshake(message -> {
            if (message.isSuccessful()) {
                logger.info("握手成功");
            }
        });
        return bayeuxclient;
    }

}
