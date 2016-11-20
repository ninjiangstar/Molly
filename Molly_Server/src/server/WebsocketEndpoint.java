package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.MainServer;

import javax.annotation.Resource;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import encoders.SongChangedMessageEncoder;
import messages.AddSongToPlaylistMessage;
import messages.ChangeSongMessage;
import messages.GoLiveMessage;
import messages.GoOfflineMessage;
import messages.JoinChannelMessage;
import messages.Message;
import messages.SongChangedMessage;
import messages.UpdatePlaylistMessage;

@ServerEndpoint(value = "/websocketEndpoint", decoders = { MessageDecoder.class }, encoders = {
		SongChangedMessageEncoder.class })

public class WebsocketEndpoint {
	private ArrayList<String> channelIDs = new ArrayList<String>();
	private Map<String, ArrayList<String>> DJToListenersMap = new HashMap<String, ArrayList<String>>();
	private static final Logger logger = Logger.getLogger("BotEndpoint");

	@OnOpen
	public void openConnection(Session session) {
		logger.log(Level.INFO, "Connection opened." + session.getId());
	}

	@OnMessage
	public void message(Session session, Message msg) {
		logger.log(Level.INFO, "Received: {0}", msg.toString());
		if (msg instanceof AddSongToPlaylistMessage) {
			AddSongToPlaylistMessage addSongToPlaylistMessage = (AddSongToPlaylistMessage) msg;
			//channelIDToChannelMap.get(addSongToPlaylistMessage.getClientID()).addSongToPlaylist(addSongToPlaylistMessage.getSongURI());
			//sendAll(); send update playlist message to everyone
		}else if(msg instanceof ChangeSongMessage){
			ChangeSongMessage changeSongMessage = (ChangeSongMessage) msg;

		}else if(msg instanceof GoLiveMessage){
			GoLiveMessage goLiveMessage = (GoLiveMessage) msg;

		}else if(msg instanceof GoOfflineMessage){
			GoOfflineMessage goOfflineMessage = (GoOfflineMessage) msg;

		}else if(msg instanceof JoinChannelMessage){
			JoinChannelMessage joinChannelMessage = (JoinChannelMessage) msg;

		}else if(msg instanceof SongChangedMessage){
			SongChangedMessage songChangedMessage = (SongChangedMessage) msg;

		}else if(msg instanceof UpdatePlaylistMessage){
			UpdatePlaylistMessage updatePlaylistMessage = (UpdatePlaylistMessage) msg;
			
		}
	}

	public void sendAll(Session session, Message msg) {
		try {
			for (Session sess : session.getOpenSessions()) {
				if (sess.isOpen())
					sess.getBasicRemote().sendObject(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addClientIDToDJArray(String channelID) {
		channelIDs.add(channelID);
	}

	public void removeClientIDToDJArray(String channelID) {
		channelIDs.remove(channelID);
	}

	public void addListenerToMapForDJ(String clientID, String channelID) {
		ArrayList<String> listenerArrayList = DJToListenersMap.get(channelID);
		listenerArrayList.add(clientID);
	}

	public void removeListenerFromMapForDJ(String clientID, String channelID) {
		ArrayList<String> listenerArrayList = DJToListenersMap.get(channelID);
		listenerArrayList.remove(clientID);
	}
}
