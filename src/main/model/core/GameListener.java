package model.core;

//Interfaz para el patrón Observer - Notificaciones asíncronas
public interface GameListener {
    
    void onGameFinished(GameEvent event);
    
    void onScoreUpdated(GameEvent event);

    void onGameStateChanged(GameEvent event);

    void onGameError(GameEvent event);
}