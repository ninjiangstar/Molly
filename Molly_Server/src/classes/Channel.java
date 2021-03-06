package classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import server.ChannelManager;
import server.SocketListener;

public class Channel {

private Lock lock = new ReentrantLock();
private Condition queueNotEmpty = lock.newCondition();
private ChannelRunner runner;

public String channelId;
public String clientId;
public String channelName;
public int listenerCount;
public ArrayList<Song> songQueue;
public ArrayList<User> userQueue;
public boolean isLive;

public String currentSongURI;
public long startTime;     // SYSTEM milliseconds
public long currentTime;   // ms, percent of duration
public long duration;      // ms, duration given by spotify
public int numLikes;
public int numDislikes;


public Channel(String clientId, String channelId, String channelName){
	this.clientId = clientId;
	this.channelId = channelId;
	this.channelName = channelName;
	this.songQueue = new ArrayList<Song>();
	this.userQueue = new ArrayList<User>();
	listenerCount = 0;
	numLikes = 0;
	numDislikes = 0;

	startTime = System.currentTimeMillis();
	isLive = false;
}

private class ChannelRunner extends Thread {
	private boolean alive = true;
	@Override
	public void run() {
		while (alive) {
			lock.lock();
			try {
				if (currentSongURI != null && currentTime < duration) {
					currentTime = System.currentTimeMillis() - startTime;
				} else {
					while (songQueue.isEmpty() && currentSongURI == null) {
						System.out.println("Waiting for more songs in queue.");
						queueNotEmpty.await();
					}
					startNextSong();
				}

			} catch (InterruptedException e) {
				// todo
			}
			lock.unlock();
		}
	}
	public void end() {
		alive = false;
	}
}

public void startNextSong() {
	lock.lock();
	if (songQueue.isEmpty()) {
		currentSongURI = null;
		duration = 0;
		currentTime = 0;
		startTime = 0;
		emitUpdate();
	} else {
		// after queue isn't empty...
		Song nextSong = songQueue.get(0);
		songQueue.remove(0);
		// replace current song
		currentSongURI = nextSong.getSongURI();
		duration = nextSong.getDuration();
		currentTime = 0;
		startTime = System.currentTimeMillis();
		System.out.println("START SONG " + currentSongURI);
		emitUpdate();
	}
	lock.unlock();
}

public void addSong(Song song) {
	lock.lock();
	songQueue.add(song);
	emitUpdate();
	queueNotEmpty.signal();
	lock.unlock();
}

public void addUser(User user) {
	lock.lock();
	userQueue.add(user);
	listenerCount = userQueue.size();
	emitUpdate();
	lock.unlock();
}

public void removeUser(String clientId) {
	lock.lock();
	userQueue.remove(clientId);
	listenerCount = userQueue.size();
	emitUpdate();
	lock.unlock();
}

public void replaceQueue(List<Song> newQueue) {
	lock.lock();
	this.songQueue = (ArrayList<Song>) newQueue;
	emitUpdate();
	if (!newQueue.isEmpty())
		queueNotEmpty.signal();
	lock.unlock();
}

public void setChannelName(String channelName) {
	lock.lock();
	this.channelName = channelName;
	lock.unlock();
}

public void setNumChannelLikes(int likes) {
	lock.lock();
	this.numLikes = likes;
	lock.unlock();
}

public void setNumChannelDislikes(int dislikes) {
	lock.lock();
	this.numDislikes = dislikes;
	lock.unlock();
}

public void goLive() {
	lock.lock();
	// stop current runner
	if (runner != null) {
		runner.end();
	}
	// update current song
	if (currentSongURI != null) {
		startTime = System.currentTimeMillis() - currentTime;
	}

	System.out.println("Going online...");
	isLive = true;
	runner = new ChannelRunner();
	runner.start();
	emitUpdate();
	ChannelManager.emitUpdate();
	lock.unlock();
}

public void goOffline() {
	lock.lock();
	System.out.println("Going offline...");
	isLive = false;
	if (runner != null) {
		runner.end();
		runner = null;
	}
	emitUpdate();
	ChannelManager.emitUpdate();
	queueNotEmpty.signal();
	lock.unlock();
}

public void emitUpdate() {
	lock.lock();
	JSONObject msg = new JSONObject();
	msg.put("emit", "channel_updated");
	msg.put("channel", this.channelId);
	msg.put("data", this.toJSON());
	ChannelManager.ws.sendAll(msg);
	lock.unlock();
}

public JSONObject toJSON() {
	/* {
	    id: string,
	    hostId: string,
	    name: string,
	    favorite: bool,
	    isLive: bool,
	    listenerCount: int,
	    currentTrackURI: string, (optional)
	    currentTrackTime: number, (optional)
	    currentTrackDuration: number (optional)
	   } */

	JSONObject obj = new JSONObject();

	obj.put("id", channelId);
	obj.put("hostId", clientId);
	obj.put("name", channelName);
	obj.put("favorite", false);
	obj.put("isLive", isLive);
	obj.put("listenerCount", listenerCount);

	if (currentSongURI != null) {
		obj.put("currentTrackURI", currentSongURI);
		obj.put("currentTrackStartTime", startTime);
		obj.put("currentTrackTime", currentTime);
		obj.put("currentTrackDuration", duration);
	}

	JSONArray songQueueJSON = new JSONArray();
	for (Song s : songQueue) {
		songQueueJSON.add(s.getSongURI());
	}

	obj.put("upNext", songQueueJSON);

	return obj;
}

}
