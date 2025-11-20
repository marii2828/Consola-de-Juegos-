package model.core;

import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

//Implementación base que proporciona funcionalidad común
public abstract class AbstractGame implements GamePlugin {
    
    protected List<GameListener> listeners;
    protected boolean isRunning;
    protected int currentScore;
    
    public AbstractGame() {
        this.listeners = new ArrayList<>();
        this.isRunning = false;
        this.currentScore = 0;
    }
   
    @Override
    public void addGameListener(GameListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeGameListener(GameListener listener) {
        listeners.remove(listener);
    }
     
    protected void notifyGameFinished(int finalScore) {
        GameEvent event = new GameEvent(getGameName(), 
            GameEvent.EventType.GAME_FINISHED, finalScore);
        
        for (GameListener listener : listeners) {
            listener.onGameFinished(event);
        }
    }
    
    protected void notifyScoreUpdated(int newScore) {
        this.currentScore = newScore;
        GameEvent event = new GameEvent(getGameName(), 
            GameEvent.EventType.SCORE_UPDATED, newScore);
        
        for (GameListener listener : listeners) {
            listener.onScoreUpdated(event);
        }
    }
    
    protected void notifyGameStateChanged(GameEvent.EventType state) {
        GameEvent event = new GameEvent(getGameName(), state, currentScore);
        
        for (GameListener listener : listeners) {
            listener.onGameStateChanged(event);
        }
    }
    
    protected void notifyGameError(String errorMessage, Exception exception) {
        GameEvent event = new GameEvent(getGameName(), 
            GameEvent.EventType.GAME_ERROR, currentScore);
        event.addData("errorMessage", errorMessage);
        event.addData("exception", exception);
        
        for (GameListener listener : listeners) {
            listener.onGameError(event);
        }
    }
    
    @Override
    public boolean isGameRunning() {
        return isRunning;
    }
    
    @Override
    public int getCurrentScore() {
        return currentScore;
    }
    
    @Override
    public abstract String getGameName();
    
    @Override
    public abstract JPanel getGamePanel();
    
    @Override
    public abstract void startGame();
    
    @Override
    public abstract void pauseGame();
    
    @Override
    public abstract void restartGame();
    
    @Override
    public abstract void stopGame();
    
    @Override
    public abstract GamePlugin getInstance();
}