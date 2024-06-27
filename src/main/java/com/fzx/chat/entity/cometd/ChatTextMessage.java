package com.fzx.chat.entity.cometd;

public class ChatTextMessage {

    /**
     * 消息发送者
     */
    private String from;

    /**
     * 消息接收者
     */
    private String to;

    /**
     * 房间号通道
     */
    private String room;

    /**
     * 消息内容
     */
    private Object content;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", room='" + room + '\'' +
                ", content=" + content +
                '}';
    }
}
