package controller;

import model.core.*;
import model.games.TicTacToeGame;
import model.games.AhorcadoGame;
import model.persistence.ScoreManager;
import view.MainView;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

//Controlador principal
public class MainController implements GameListener {

    private MainView view;
    private ScoreManager scoreManager;
    private GamePluginLoader pluginLoader;
    private List<GamePlugin> availableGames;
    private GamePlugin currentGame;

    private static MainController instance;

    private MainController() {
        this.availableGames = new ArrayList<>();
        initializeComponents();
    }

    public static MainController getInstance() {
        if (instance == null) {
            instance = new MainController();
        }
        return instance;
    }

    private void initializeComponents() {
        this.scoreManager = ScoreManager.getInstance();

        this.pluginLoader = GamePluginLoader.getInstance();

        pluginLoader.setOnPluginAddedCallback(() -> {
            reloadExternalGames();
        });

        loadInternalGames();

        loadExternalGames();

        SwingUtilities.invokeLater(() -> {
            this.view = new MainView(getInstance());
            this.view.setVisible(true);
        });
    }

    private void loadInternalGames() {
        try {
            GamePlugin ticTacToe = TicTacToeGame.createInstance();
            availableGames.add(ticTacToe);
            System.out.println(" Tic-Tac-Toe cargado correctamente");

            GamePlugin ahorcado = AhorcadoGame.createInstance();
            availableGames.add(ahorcado);
            System.out.println(" Ahorcado cargado correctamente");

            System.out.println(" Total de juegos internos: " + availableGames.size());

        } catch (Exception e) {
            System.err.println(" Error cargando juegos internos: " + e.getMessage());
            e.printStackTrace();
            handleException("Error cargando juegos internos", e);
        }
    }

    private void loadExternalGames() {
        try {
            System.out.println("Iniciando carga de plugins externos...");
            pluginLoader.loadExternalGames();
            List<GamePlugin> externalPlugins = pluginLoader.getLoadedPlugins();

            System.out.println("Plugins encontrados: " + externalPlugins.size());
            for (GamePlugin plugin : externalPlugins) {
                if (!availableGames.contains(plugin)) {
                    availableGames.add(plugin);
                    System.out.println("Plugin externo agregado: " + plugin.getGameName());
                }
            }

            if (externalPlugins.isEmpty()) {
                System.out.println("No se encontraron plugins externos");
            }

            System.out.println("Carga de plugins externos completada");

        } catch (Exception e) {
            System.err.println("Error cargando plugins externos: " + e.getMessage());
            e.printStackTrace();
            handleException("Error cargando plugins externos", e);
        }
    }

    private void reloadExternalGames() {
        try {
            List<GamePlugin> externalPlugins = pluginLoader.getLoadedPlugins();

            boolean updated = false;
            for (GamePlugin plugin : externalPlugins) {
                if (!availableGames.contains(plugin)) {
                    availableGames.add(plugin);
                    System.out.println("Nuevo plugin detectado: " + plugin.getGameName());
                    updated = true;
                }
            }

            if (updated && view != null) {
                SwingUtilities.invokeLater(() -> {
                    view.refreshGamesList();
                });
            }

        } catch (Exception e) {
            System.err.println("Error recargando plugins: " + e.getMessage());
        }
    }

    // Obtener jueos dispobibles
    public List<GamePlugin> getAvailableGames() {
        return new ArrayList<>(availableGames);
    }

    // Seleccionar y cargar un juego en la interfaz
    public void selectGame(GamePlugin game) {
        try {
            if (currentGame != null && currentGame.isGameRunning()) {
                currentGame.stopGame();
            }

            currentGame = game;
            currentGame.addGameListener(this);
            currentGame.restartGame();

            if (view != null) {
                view.displayGame(game.getGamePanel());
                view.updateGameInfo(game.getGameName(), game.getGameDescription());
                view.updateScoresDisplay(game.getGameName());
            }

        } catch (Exception e) {
            handleException("Error seleccionando juego", e);
        }
    }

    // Obtener mejores puntasjes de cada juego
    public List<String> getTopScores(String gameName) {
        return scoreManager.getTopScoresAsStrings(gameName);
    }

    @Override
    public void onGameFinished(GameEvent event) {
        System.out.println(" Juego terminado: " + event.getGameName() +
                " - Puntaje: " + event.getScore());

        scoreManager.updateScore(event.getGameName(), event.getScore());

        scoreManager.printAllScores();

        SwingUtilities.invokeLater(() -> {
            if (view != null) {
                view.showGameFinishedMessage(event.getGameName(), event.getScore());
                view.updateScoresDisplay(event.getGameName());
            }
        });
    }

    @Override
    public void onScoreUpdated(GameEvent event) {
        System.out.println(" Puntaje actualizado: " + event.getGameName() +
                " - Nuevo: " + event.getScore());

        SwingUtilities.invokeLater(() -> {
            if (view != null) {
                view.updateCurrentScore(event.getScore());
            }
        });
    }

    @Override
    public void onGameStateChanged(GameEvent event) {
        System.out.println(" Estado cambiado: " + event.getGameName() +
                " - Tipo: " + event.getEventType());
    }

    @Override
    public void onGameError(GameEvent event) {
        String errorMsg = (String) event.getData("errorMessage");
        Exception exception = (Exception) event.getData("exception");

        System.err.println(" Error en juego " + event.getGameName() + ": " + errorMsg);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(view,
                    "Error en " + event.getGameName() + ": " + errorMsg,
                    "Error del Juego",
                    JOptionPane.ERROR_MESSAGE);
        });

        handleException(errorMsg, exception);
    }

    private void handleException(String message, Exception exception) {
        System.err.println(message);
        if (exception != null) {
            exception.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            if (view != null) {
                JOptionPane.showMessageDialog(view,
                        message + "\n" + (exception != null ? exception.getMessage() : ""),
                        "Error del Sistema",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        MainController controller = MainController.getInstance();
        System.out.println(" Plataforma de Juegos Iniciada");
    }
}
