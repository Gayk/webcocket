package pl.gaik.webcocket;

import org.springframework.web.socket.WebSocketSession;

/**
 * Created by monik on 11.11.2017.
 */
public class User{
    private WebSocketSession socketSession;
    private String nick;


    public User(WebSocketSession socketSession) {
        this.socketSession = socketSession;
        nick=" ";
    }

    public User(WebSocketSession socketSession, String nick) {
        this.socketSession = socketSession;
        this.nick = nick;
    }

    public WebSocketSession getSocketSession() {
        return socketSession;
    }

    public void setSocketSession(WebSocketSession socketSession) {
        this.socketSession = socketSession;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }


    }

