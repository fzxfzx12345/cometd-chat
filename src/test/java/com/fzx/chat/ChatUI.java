package com.fzx.chat;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.ext.AckExtension;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.cometd.websocket.client.JettyWebSocketTransport;
import org.cometd.websocket.client.WebSocketTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.awt.*;
import java.util.HashMap;

public class ChatUI extends JFrame {

    static Logger logger = LoggerFactory.getLogger(ChatUI.class);

    private static JTextArea chatWindowMessages;
    private final JTextField chatWindowInput;

    private static final String CHANNEL = "/chat/privatechat";
    private static final ClientSessionChannel.MessageListener fooListener = new FooListener();

    static BayeuxClient bayeuxClient;


    private static class FooListener implements ClientSessionChannel.MessageListener {
        public void onMessage(ClientSessionChannel channel, Message message) {
            chatWindowMessages.append(message + "\n");
            logger.info("通道:{}, 消息:{}", channel, message);
        }
    }

    public ChatUI() {

        setTitle("Chat");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel chatContainer = new JPanel();
        chatContainer.setLayout(new BorderLayout());

        JPanel chatWindowPanel = new JPanel();
        chatWindowPanel.setLayout(new BorderLayout());

        JPanel chatInputPanel = new JPanel();
        chatInputPanel.setLayout(new BorderLayout());

        // Chat Window Title
        JLabel chatWindowTitle = new JLabel("chat");
        chatWindowTitle.setOpaque(true);
        chatWindowTitle.setBackground(Color.BLACK);
        chatWindowTitle.setForeground(Color.WHITE);
        chatWindowTitle.setHorizontalAlignment(JLabel.CENTER);
        chatWindowTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        chatWindowTitle.setFont(new Font("微软雅黑", Font.BOLD, 24));


        // Chat Window Messages
        chatWindowMessages = new JTextArea();
        chatWindowMessages.setEditable(false);
        chatWindowMessages.setBackground(Color.WHITE);
        chatWindowMessages.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chatWindowMessages.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        chatWindowMessages.setLineWrap(true);
        chatWindowMessages.setWrapStyleWord(true);


        JScrollPane chatScrollPane = new JScrollPane(chatWindowMessages);

        // Chat Window Input
        chatWindowInput = new JTextField();
        chatWindowInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chatWindowInput.setFont(new Font("微软雅黑", Font.PLAIN, 18));

        JButton chatWindowSend = new JButton("Send");
        chatWindowSend.setBackground(Color.BLACK);
        chatWindowSend.setForeground(Color.WHITE);
        chatWindowSend.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chatWindowSend.addActionListener(e -> sendMessage());

        JButton chatWindowUnsubscribe = new JButton("Unsubscribe");
        chatWindowUnsubscribe.addActionListener(e -> bayeuxClient.getChannel(CHANNEL).unsubscribe(fooListener, message -> {
            if (message.isSuccessful()) {
                chatWindowMessages.append("取消订阅返回：" + message + "\n");
            }
        }));

        chatWindowPanel.add(chatWindowTitle, BorderLayout.NORTH);
        chatWindowPanel.add(chatScrollPane, BorderLayout.CENTER);

        chatInputPanel.add(chatWindowInput, BorderLayout.CENTER);
        chatInputPanel.add(chatWindowSend, BorderLayout.EAST);
        chatInputPanel.add(chatWindowUnsubscribe, BorderLayout.WEST);

        chatContainer.add(chatWindowPanel, BorderLayout.CENTER);
        chatContainer.add(chatInputPanel, BorderLayout.SOUTH);

        add(chatContainer);
    }

    private void sendMessage() {
        String message = chatWindowInput.getText().trim();
        if (!message.isEmpty()) {
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("user", "Java");
            stringStringHashMap.put("content", message);
            stringStringHashMap.put("room", CHANNEL);
            stringStringHashMap.put("chat", "group");
            bayeuxClient.getChannel(CHANNEL).publish(stringStringHashMap, message1 -> {
                if (message1.isSuccessful()) {
                    chatWindowMessages.append("发布返回：" + message1 + "\n");
                }
            });
            chatWindowInput.setText("");
        }
    }


    public static BayeuxClient getBayeuxClient() throws Exception {

//        ClientConnector clientConnector = new ClientConnector();
//        clientConnector.setSelectors(1);
//        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
//        sslContextFactory.setKeyStorePath("src/test/resources/keystore.p12");
//        sslContextFactory.setKeyStoreType("pkcs12");
//        sslContextFactory.setKeyStorePassword("storepwd");
//        clientConnector.setSslContextFactory(sslContextFactory);

        // Starts the HTTP client.
//        HttpClient httpClient = new HttpClient(new org.eclipse.jetty.client.http.HttpClientTransportOverHTTP(clientConnector));

//        HttpClient httpClient = new HttpClient();
//        httpClient.start();
//        ClientTransport httpTransport = new LongPollingTransport(null, httpClient);
//        WebSocketClient webSocketClient = new WebSocketClient();
//        webSocketClient.start();
//        JettyWebSocketTransport wsTransport = new JettyWebSocketTransport(null, null, webSocketClient);
//        return getBayeuxClientLink(wsTransport, httpTransport);

        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.start();
        JettyWebSocketTransport wsTransport = new JettyWebSocketTransport(null, null, webSocketClient);
        return getBayeuxClientLink(wsTransport);

    }

    private static BayeuxClient getBayeuxClientLink(JettyWebSocketTransport wsTransport, ClientTransport httpTransport) {
        BayeuxClient bayeuxclient = new BayeuxClient("https://testmail.tom.com/chat/cometd", wsTransport, httpTransport);
        bayeuxclient.addExtension(new AckExtension());

        bayeuxclient.handshake(message -> {
            if (message.isSuccessful()) {
                chatWindowMessages.append("握手返回：" + message + "\n");
                bayeuxclient.getChannel(CHANNEL).subscribe(fooListener, message1 -> {
                    if (message1.isSuccessful()) {
                        chatWindowMessages.append("订阅返回：" + message1 + "\n");
                    }
                });
            }
        });
        return bayeuxclient;
    }

    private static BayeuxClient getBayeuxClientLink(JettyWebSocketTransport wsTransport) {
        BayeuxClient bayeuxclient = new BayeuxClient("https://testmail.tom.com/chat/cometd", wsTransport);
        bayeuxclient.addExtension(new AckExtension());

        bayeuxclient.handshake(message -> {
            if (message.isSuccessful()) {
                chatWindowMessages.append("握手返回：" + message + "\n");
                bayeuxclient.getChannel(CHANNEL).subscribe(fooListener, message1 -> {
                    if (message1.isSuccessful()) {
                        chatWindowMessages.append("订阅返回：" + message1 + "\n");
                    }
                });
            }
        });
        return bayeuxclient;
    }


    public static BayeuxClient getBayeuxClientJSR() throws Exception {

        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();

        ClientTransport wsTransport = new WebSocketTransport(null, null, webSocketContainer);

        HttpClient httpClient = new HttpClient();
        httpClient.addBean(webSocketContainer, true);
        httpClient.start();
        ClientTransport httpTransport = new LongPollingTransport(null, httpClient);

        return getBayeuxClientLink(wsTransport, httpTransport);
    }


    private static BayeuxClient getBayeuxClientLink(ClientTransport wsTransport, ClientTransport httpTransport) {
        BayeuxClient bayeuxclient = new BayeuxClient("https://testmail.tom.com/chat/cometd", wsTransport, httpTransport);
        bayeuxclient.addExtension(new AckExtension());

        bayeuxclient.handshake(message -> {
            if (message.isSuccessful()) {
                chatWindowMessages.append("握手返回：" + message + "\n");
                bayeuxclient.getChannel(CHANNEL).subscribe(fooListener, message1 -> {
                    if (message1.isSuccessful()) {
                        chatWindowMessages.append("订阅返回：" + message1 + "\n");
                    }
                });
            }
        });
        return bayeuxclient;
    }


    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                new ChatUI().setVisible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
//        bayeuxClient = getBayeuxClientJSR(); // JSR 356
        bayeuxClient = getBayeuxClient(); // Jetty WebSocket
    }
}
