package model.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//Encapsula toda la información de un evento del juego
public class GameEvent {
    private final String gameName;
    private final EventType eventType;
    private final int score;
    private final Date timestamp;
    private final Map<String, Object> additionalData;
    
    public enum EventType {
        GAME_STARTED,
        GAME_FINISHED,
        SCORE_UPDATED,
        GAME_PAUSED,
        GAME_RESUMED,
        GAME_ERROR
    }
    
    public GameEvent(String gameName, EventType eventType, int score) {
        this.gameName = gameName;
        this.eventType = eventType;
        this.score = score;
        this.timestamp = new Date();
        this.additionalData = new HashMap<>();
    }
    
    public GameEvent(String gameName, EventType eventType) {
        this(gameName, eventType, 0);
    }

    public String getGameName() { return gameName; }
    public EventType getEventType() { return eventType; }
    public int getScore() { return score; }
    public Date getTimestamp() { return timestamp; }
    public Map<String, Object> getAdditionalData() { return additionalData; }
    
    public void addData(String key, Object value) {
        additionalData.put(key, value);
    }
    
    public Object getData(String key) {
        return additionalData.get(key);
    }
    
    @Override
    public String toString() {
        return String.format("GameEvent[%s - %s - Score: %d]", 
            gameName, eventType, score);
    }
}