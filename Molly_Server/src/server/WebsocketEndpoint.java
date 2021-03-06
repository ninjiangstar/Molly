package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import classes.Song;
import encoders.SongChangedMessageEncoder;
import messages.AddSongToPlaylistMessage;
import messages.ChangeSongMessage;
import messages.DJIsOfflineMessage;
import messages.GoLiveMessage;
import messages.GoOfflineMessage;
import messages.JoinChannelMessage;
import messages.JoinedChannelSuccessfullyMessage;
import messages.Message;
import messages.SongChangedMessage;
import messages.UpdatePlaylistMessage;

@ServerEndpoint(value = "/ws")
//@ServerEndpoint("/ws")

public class WebsocketEndpoint {
private static final Logger logger = Logger.getLogger("BotEndpoint");
private static final JSONParser parser = new JSONParser();
private static final Map<String, Session> sessions = new HashMap<String, Session>();
private static Lock lock = new ReentrantLock();

public WebsocketEndpoint() {
	ChannelManager.ws = this;
	SocketListener.ws = this;
}

@OnOpen
public void openConnection(Session session) {
	lock.lock();
	logger.log(Level.INFO, "Connection opened. (id:)" + session.getId());
	sessions.put(session.getId(), session);
	lock.unlock();
}

@OnClose
public void close(Session session) {
	lock.lock();
	logger.log(Level.INFO, "Connection closed. (id:)" + session.getId());
	sessions.remove(session.getId());
	lock.unlock();
}

@OnError
public void onError(Throwable error) {
	error.printStackTrace();
}

@OnMessage
public void onMessage(String message, Session session) {

	JSONObject msg;

	lock.lock();
	try {
		msg = (JSONObject) parser.parse(message);
		logger.log(Level.INFO, "Received: {0}", msg.toJSONString());
		SocketListener.route(msg);
	} catch (ParseException e) {
		e.printStackTrace();
		return;
	}
	lock.unlock();
}

public void sendAll(JSONObject msg) {
	lock.lock();
	for (Session e : sessions.values()) {
		sendToSession(e, msg);
	}
	lock.unlock();
}

private void sendToSession(Session session, JSONObject message) {
	try {
		session.getBasicRemote().sendText(message.toJSONString());
	} catch (IOException ex) {
		sessions.remove(session.getId());
		logger.log(Level.SEVERE, ex.getMessage(), ex.getStackTrace());
	}
}
}
