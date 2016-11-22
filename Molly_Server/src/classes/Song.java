package classes;

import org.json.simple.JSONObject;

public class Song {
private String songURI;
private long duration;

public Song(String songURI, long duration){
	this.songURI = songURI;
	this.duration = duration;
}

public String getSongURI() {
	return songURI;
}

public long getDuration() {
	return duration;
}

}
