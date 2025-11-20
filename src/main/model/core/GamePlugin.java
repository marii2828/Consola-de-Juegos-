package model.core;

import javax.swing.JPanel;

//Interfaz que deben implementar TODOS los juegos (internos y externos)
public interface GamePlugin {
    
    String getGameName();
    String getGameVersion();
    String getGameDescription();
    
    JPanel getGamePanel();
    
    void startGame();
    void pauseGame();
    void restartGame();
    void stopGame();
    
    void addGameListener(GameListener listener);
    void removeGameListener(GameListener listener);
    
    GamePlugin getInstance();

    boolean isGameRunning();
    int getCurrentScore();
}