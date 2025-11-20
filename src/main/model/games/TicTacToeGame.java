package model.games;

import model.core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TicTacToeGame extends AbstractGame {
    private static TicTacToeGame instance;

    private JPanel mainPanel;
    private StartPanel startPanel;
    private GamePanel gamePanel;

    private int score;
    private int winsX;
    private int winsO;
    private int ties;
    private JButton[][] board;
    private String playerX = "X";
    private String playerO = "O";
    private String currentPlayer = playerX;
    private boolean gameOver = false;
    private int turns = 0;

    private static final int BOARD_SIZE = 3;
    private static final int BUTTON_SIZE = 150;

    private TicTacToeGame() {
        this.board = new JButton[BOARD_SIZE][BOARD_SIZE];
        initializeGame();
    }

    public static TicTacToeGame createInstance() {
        if (instance == null) {
            instance = new TicTacToeGame();
        }
        return instance;
    }

    private void initializeGame() {
        this.mainPanel = new JPanel(new CardLayout());
        this.startPanel = new StartPanel();
        this.gamePanel = new GamePanel();
        resetGame();

        mainPanel.add(startPanel, "START");
        mainPanel.add(gamePanel, "GAME");

        showStartScreen();
    }

    @Override
    public String getGameName() {
        return "Tic-Tac-Toe";
    }

    @Override
    public String getGameVersion() {
        return "1.0";
    }

    @Override
    public String getGameDescription() {
        return "Juego clásico de Tres en Raya. Gana el primero en formar una línea!";
    }

    @Override
    public JPanel getGamePanel() {
        return mainPanel;
    }

    @Override
    public void startGame() {
        if (!isGameRunning()) {
            resetGame();
            showGameScreen();
            isRunning = true;
            notifyGameStateChanged(GameEvent.EventType.GAME_STARTED);
        }
    }

    @Override
    public void pauseGame() {
        if (isGameRunning()) {
            notifyGameStateChanged(GameEvent.EventType.GAME_PAUSED);
        }
    }

    @Override
    public void restartGame() {
        resetGame();
        if (!isGameRunning()) {
            startGame();
        }
    }

    @Override
    public void stopGame() {
        isRunning = false;
        gameOver = false;
        showStartScreen();
        notifyGameStateChanged(GameEvent.EventType.GAME_FINISHED);
    }

    private void showStartScreen() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "START");
    }

    private void showGameScreen() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "GAME");
    }

    @Override
    public GamePlugin getInstance() {
        if (instance == null) {
            instance = this;
        }
        return instance;
    }

    private void resetGame() {
        if (board == null) {
            board = new JButton[BOARD_SIZE][BOARD_SIZE];
        }
        
        currentPlayer = playerX;
        gameOver = false;
        turns = 0;
        score = 0;
        
        if (gamePanel != null) {
            gamePanel.initializeBoard();
            gamePanel.updateStatus("Turno de " + currentPlayer);
        }
    }

    private void checkWinner() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            if (board[r][0].getText().isEmpty()) continue;
            
            if (board[r][0].getText().equals(board[r][1].getText()) &&
                board[r][1].getText().equals(board[r][2].getText())) {
                
                highlightWinner(board[r][0], board[r][1], board[r][2]);
                gameFinished(currentPlayer);
                return;
            }
        }

        for (int c = 0; c < BOARD_SIZE; c++) {
            if (board[0][c].getText().isEmpty()) continue;
            
            if (board[0][c].getText().equals(board[1][c].getText()) &&
                board[1][c].getText().equals(board[2][c].getText())) {
                
                highlightWinner(board[0][c], board[1][c], board[2][c]);
                gameFinished(currentPlayer);
                return;
            }
        }

        if (!board[0][0].getText().isEmpty() &&
            board[0][0].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][2].getText())) {
            
            highlightWinner(board[0][0], board[1][1], board[2][2]);
            gameFinished(currentPlayer);
            return;
        }

        if (!board[0][2].getText().isEmpty() &&
            board[0][2].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][0].getText())) {
            
            highlightWinner(board[0][2], board[1][1], board[2][0]);
            gameFinished(currentPlayer);
            return;
        }

        if (turns == 9) {
            highlightTie();
            gameFinished("TIE");
            return;
        }

        currentPlayer = currentPlayer.equals(playerX) ? playerO : playerX;
        if (gamePanel != null) {
            gamePanel.updateStatus("Turno de " + currentPlayer);
        }
    }

    private void highlightWinner(JButton... tiles) {
        for (JButton tile : tiles) {
            tile.setForeground(Color.GREEN);
            tile.setBackground(Color.GRAY);
        }
    }

    private void highlightTie() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c].setForeground(Color.ORANGE);
                board[r][c].setBackground(Color.GRAY);
            }
        }
    }

    private void gameFinished(String result) {
        gameOver = true;
        isRunning = false;
        
        if (result.equals(playerX)) {
            winsX++;
            score = 100 + (winsX * 10);
            if (gamePanel != null) {
                gamePanel.updateStatus(playerX + " ha ganado! Puntos: " + score);
            }
        } else if (result.equals(playerO)) {
            winsO++;
            score = 100 + (winsO * 10);
            if (gamePanel != null) {
                gamePanel.updateStatus(playerO + " ha ganado! Puntos: " + score);
            }
        } else if (result.equals("TIE")) {
            ties++;
            score = 50 + (ties * 5);
            if (gamePanel != null) {
                gamePanel.updateStatus("Empate! Puntos: " + score);
            }
        }

        notifyGameFinished(score);
    }

    private class StartPanel extends JPanel {
        public StartPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(20, 20, 40));
            setPreferredSize(new Dimension(BOARD_SIZE * BUTTON_SIZE + 20, BOARD_SIZE * BUTTON_SIZE + 100));

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));

            JLabel titleLabel = new JLabel("⚫ TIC-TAC-TOE ⚪");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setAlignmentX(CENTER_ALIGNMENT);
            centerPanel.add(titleLabel);

            centerPanel.add(Box.createVerticalStrut(30));

            JLabel instructions = new JLabel("<html><center>" +
                    "Juega tres en raya<br>" +
                    "X juega primero<br>" +
                    "Gana quien forme una línea" +
                    "</center></html>");
            instructions.setFont(new Font("Arial", Font.PLAIN, 14));
            instructions.setForeground(Color.WHITE);
            instructions.setAlignmentX(CENTER_ALIGNMENT);
            centerPanel.add(instructions);

            centerPanel.add(Box.createVerticalStrut(40));

            JButton startButton = new JButton("▶ COMENZAR JUEGO");
            startButton.setFont(new Font("Arial", Font.BOLD, 18));
            startButton.setPreferredSize(new Dimension(200, 50));
            startButton.setAlignmentX(CENTER_ALIGNMENT);
            startButton.setBackground(new Color(0, 150, 0));
            startButton.setForeground(Color.WHITE);
            startButton.setFocusPainted(false);
            startButton.addActionListener(e -> {
                TicTacToeGame.this.startGame();
            });
            centerPanel.add(startButton);

            add(centerPanel, BorderLayout.CENTER);
        }
    }

    private class GamePanel extends JPanel {
        JPanel boardPanel;
        JLabel statusLabel;
        JPanel statusPanel;

        public GamePanel() {
            setLayout(new BorderLayout());
            setBackground(Color.DARK_GRAY);
            setPreferredSize(new Dimension(BOARD_SIZE * BUTTON_SIZE + 20, BOARD_SIZE * BUTTON_SIZE + 100));
            
            initializeBoard();
        }

        public void initializeBoard() {
            removeAll();
            
            if (TicTacToeGame.this.board == null) {
                TicTacToeGame.this.board = new JButton[BOARD_SIZE][BOARD_SIZE];
            }
            
            statusPanel = new JPanel(new BorderLayout());
            statusPanel.setBackground(Color.DARK_GRAY);
            
            statusLabel = new JLabel("Turno de " + currentPlayer);
            statusLabel.setBackground(Color.DARK_GRAY);
            statusLabel.setForeground(Color.WHITE);
            statusLabel.setFont(new Font("Arial", Font.BOLD, 24));
            statusLabel.setHorizontalAlignment(JLabel.CENTER);
            statusLabel.setOpaque(true);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            statusPanel.add(statusLabel, BorderLayout.CENTER);
            
            JButton exitButton = new JButton("✖ Salir");
            exitButton.setFont(new Font("Arial", Font.BOLD, 14));
            exitButton.setBackground(new Color(150, 0, 0));
            exitButton.setForeground(Color.WHITE);
            exitButton.addActionListener(e -> {
                TicTacToeGame.this.stopGame();
            });
            statusPanel.add(exitButton, BorderLayout.EAST);
            
            add(statusPanel, BorderLayout.NORTH);

            boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
            boardPanel.setBackground(Color.DARK_GRAY);
            boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    JButton tile = new JButton();
                    TicTacToeGame.this.board[r][c] = tile;
                    boardPanel.add(tile);

                    tile.setBackground(Color.DARK_GRAY);
                    tile.setForeground(Color.WHITE);
                    tile.setFont(new Font("Arial", Font.BOLD, 80));
                    tile.setFocusable(false);
                    tile.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

                    tile.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JButton tile = (JButton) e.getSource();
                            
                            if (gameOver || !isRunning) return;
                            
                            if (tile.getText().isEmpty()) {
                                tile.setText(currentPlayer);
                                turns++;
                                
                                updateStatus("Turno de " + (currentPlayer.equals(playerX) ? playerO : playerX));
                                checkWinner();
                            }
                        }
                    });
                }
            }
            
            add(boardPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        public void updateStatus(String text) {
            if (statusLabel != null) {
                statusLabel.setText(text);
            }
        }
    }
}
