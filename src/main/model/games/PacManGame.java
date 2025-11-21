//código obtenido y adaptado de https://github.com/ImKennyYip/pacman-java
package model.games;

import model.core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;

public class PacManGame extends AbstractGame {
    private static PacManGame instance;

    private JPanel mainPanel;
    private StartPanel startPanel;
    private GamePanel gamePanel;

    private static final Color COLOR_FONDO = new Color(20, 20, 40);
    private static final Color COLOR_EXITO = new Color(46, 204, 113);
    private static final Color COLOR_PRIMARIO = new Color(255, 215, 0);

    private PacManGame() {
        initializeGame();
    }

    public static PacManGame createInstance() {
        if (instance == null) {
            instance = new PacManGame();
        }
        return instance;
    }

    private void initializeGame() {
        this.mainPanel = new JPanel(new CardLayout());
        this.startPanel = new StartPanel();
        this.gamePanel = new GamePanel();

        mainPanel.add(startPanel, "START");
        mainPanel.add(gamePanel, "GAME");

        showStartScreen();
    }

    @Override
    public String getGameName() {
        return "PacMan";
    }

    @Override
    public String getGameVersion() {
        return "1.0";
    }

    @Override
    public String getGameDescription() {
        return "Juego clásico de Pac-Man. Come todos los puntos y evita a los fantasmas!";
    }

    @Override
    public JPanel getGamePanel() {
        return mainPanel;
    }

    @Override
    public void startGame() {
        if (!isGameRunning()) {
            gamePanel.resetGame();
            showGameScreen();
            isRunning = true;
            gamePanel.startGameLoop();
            notifyGameStateChanged(GameEvent.EventType.GAME_STARTED);
        }
    }

    @Override
    public void pauseGame() {
        if (isGameRunning()) {
            gamePanel.pauseGameLoop();
            isRunning = false;
            notifyGameStateChanged(GameEvent.EventType.GAME_PAUSED);
        }
    }

    @Override
    public void restartGame() {
        gamePanel.resetGame();
        if (!isGameRunning()) {
            startGame();
        } else {
            gamePanel.startGameLoop();
        }
    }

    @Override
    public void stopGame() {
        gamePanel.stopGameLoop();
        isRunning = false;
        showStartScreen();
        notifyGameStateChanged(GameEvent.EventType.GAME_FINISHED);
    }

    @Override
    public GamePlugin getInstance() {
        if (instance == null) {
            instance = this;
        }
        return instance;
    }

    private void showStartScreen() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "START");
    }

    private void showGameScreen() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "GAME");
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });
    }

    private class StartPanel extends JPanel {
        public StartPanel() {
            setLayout(new BorderLayout());
            setBackground(COLOR_FONDO);
            setPreferredSize(new Dimension(456, 504));

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));

            JLabel titleLabel = new JLabel("👾 PAC-MAN 👾");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
            titleLabel.setForeground(COLOR_PRIMARIO);
            titleLabel.setAlignmentX(CENTER_ALIGNMENT);
            centerPanel.add(titleLabel);

            centerPanel.add(Box.createVerticalStrut(30));

            JLabel instructions = new JLabel("<html><center>" +
                    "Controla Pac-Man con las flechas del teclado<br>" +
                    "Come todos los puntos blancos<br>" +
                    "Evita los fantasmas<br>" +
                    "Tienes 3 vidas" +
                    "</center></html>");
            instructions.setFont(new Font("Arial", Font.PLAIN, 16));
            instructions.setForeground(Color.WHITE);
            instructions.setAlignmentX(CENTER_ALIGNMENT);
            centerPanel.add(instructions);

            centerPanel.add(Box.createVerticalStrut(40));

            JButton startButton = new JButton("▶ COMENZAR JUEGO");
            startButton.setFont(new Font("Arial", Font.BOLD, 18));
            startButton.setPreferredSize(new Dimension(250, 50));
            startButton.setAlignmentX(CENTER_ALIGNMENT);
            startButton.setBackground(COLOR_EXITO);
            startButton.setForeground(Color.WHITE);
            startButton.setFocusPainted(false);
            startButton.addActionListener(e -> {
                PacManGame.this.startGame();
            });
            centerPanel.add(startButton);

            add(centerPanel, BorderLayout.CENTER);
        }
    }

    private class GamePanel extends JPanel implements ActionListener, KeyListener {

        class Block {
            int x;
            int y;
            int width;
            int height;
            Image image;

            int startX;
            int startY;
            char direction = 'U';
            int velocityX = 0;
            int velocityY = 0;

            Block(Image image, int x, int y, int width, int height) {
                this.image = image;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.startX = x;
                this.startY = y;
            }

            void updateDirection(char direction) {
                char prevDirection = this.direction;
                this.direction = direction;
                updateVelocity();
                this.x += this.velocityX;
                this.y += this.velocityY;
                for (Block wall : walls) {
                    if (collision(this, wall)) {
                        this.x -= this.velocityX;
                        this.y -= this.velocityY;
                        this.direction = prevDirection;
                        updateVelocity();
                    }
                }
            }

            void updateVelocity() {
                if (this.direction == 'U') {
                    this.velocityX = 0;
                    this.velocityY = -tileSize / 4;
                } else if (this.direction == 'D') {
                    this.velocityX = 0;
                    this.velocityY = tileSize / 4;
                } else if (this.direction == 'L') {
                    this.velocityX = -tileSize / 4;
                    this.velocityY = 0;
                } else if (this.direction == 'R') {
                    this.velocityX = tileSize / 4;
                    this.velocityY = 0;
                }
            }

            void reset() {
                this.x = this.startX;
                this.y = this.startY;
            }
        }

        private int rowCount = 21;
        private int columnCount = 19;
        private int tileSize = 24;
        private int boardWidth = columnCount * tileSize;
        private int boardHeight = rowCount * tileSize;

        private Image wallImage;
        private Image blueGhostImage;
        private Image orangeGhostImage;
        private Image pinkGhostImage;
        private Image redGhostImage;

        private Image pacmanUpImage;
        private Image pacmanDownImage;
        private Image pacmanLeftImage;
        private Image pacmanRightImage;

        private String[] tileMap = {
                "XXXXXXXXXXXXXXXXXXX",
                "X        X        X",
                "X XX XXX X XXX XX X",
                "X                 X",
                "X XX X XXXXX X XX X",
                "X    X       X    X",
                "XXXX XXXX XXXX XXXX",
                "OOOX X       X XOOO",
                "XXXX X XXrXX X XXXX",
                "O       bpo       O",
                "XXXX X XXXXX X XXXX",
                "OOOX X       X XOOO",
                "XXXX X XXXXX X XXXX",
                "X        X        X",
                "X XX XXX X XXX XX X",
                "X  X     P     X  X",
                "XX X X XXXXX X X XX",
                "X    X   X   X    X",
                "X XXXXXX X XXXXXX X",
                "X                 X",
                "XXXXXXXXXXXXXXXXXXX"
        };

        HashSet<Block> walls;
        HashSet<Block> foods;
        HashSet<Block> ghosts;
        Block pacman;

        Timer gameLoop;
        char[] directions = { 'U', 'D', 'L', 'R' };
        Random random = new Random();
        int score = 0;
        int lives = 3;
        boolean gameOver = false;

        public GamePanel() {
            setPreferredSize(new Dimension(boardWidth, boardHeight));
            setBackground(Color.BLACK);
            addKeyListener(this);
            setFocusable(true);

            loadImages();
            loadMap();
            for (Block ghost : ghosts) {
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection);
            }
            gameLoop = new Timer(50, this);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            requestFocusInWindow();
        }

        private void loadImages() {
            try {
                wallImage = new ImageIcon(getClass().getResource("/wall.png")).getImage();
                blueGhostImage = new ImageIcon(getClass().getResource("/blueGhost.png")).getImage();
                orangeGhostImage = new ImageIcon(getClass().getResource("/orangeGhost.png")).getImage();
                pinkGhostImage = new ImageIcon(getClass().getResource("/pinkGhost.png")).getImage();
                redGhostImage = new ImageIcon(getClass().getResource("/redGhost.png")).getImage();

                pacmanUpImage = new ImageIcon(getClass().getResource("/pacmanUp.png")).getImage();
                pacmanDownImage = new ImageIcon(getClass().getResource("/pacmanDown.png")).getImage();
                pacmanLeftImage = new ImageIcon(getClass().getResource("/pacmanLeft.png")).getImage();
                pacmanRightImage = new ImageIcon(getClass().getResource("/pacmanRight.png")).getImage();
            } catch (Exception e) {
                System.err.println("Error cargando imágenes: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void loadMap() {
            walls = new HashSet<Block>();
            foods = new HashSet<Block>();
            ghosts = new HashSet<Block>();

            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < columnCount; c++) {
                    String row = tileMap[r];
                    char tileMapChar = row.charAt(c);

                    int x = c * tileSize;
                    int y = r * tileSize;

                    if (tileMapChar == 'X') {
                        Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                        walls.add(wall);
                    } else if (tileMapChar == 'b') {
                        Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(ghost);
                    } else if (tileMapChar == 'o') {
                        Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(ghost);
                    } else if (tileMapChar == 'p') {
                        Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(ghost);
                    } else if (tileMapChar == 'r') {
                        Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                        ghosts.add(ghost);
                    } else if (tileMapChar == 'P') {
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                    } else if (tileMapChar == ' ') {
                        Block food = new Block(null, x + 10, y + 10, 4, 4);
                        foods.add(food);
                    }
                }
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw(g);
        }

        public void draw(Graphics g) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

            for (Block ghost : ghosts) {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }

            for (Block wall : walls) {
                g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
            }

            g.setColor(Color.WHITE);
            for (Block food : foods) {
                g.fillRect(food.x, food.y, food.width, food.height);
            }

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            if (gameOver) {
                g.setColor(Color.RED);
                g.drawString("Game Over: " + String.valueOf(score), tileSize / 2, tileSize / 2);
            } else {
                g.setColor(Color.WHITE);
                g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize / 2,
                        tileSize / 2);
            }
        }

        public void move() {
            if (!isRunning || gameOver) {
                return;
            }

            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;

            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    pacman.x -= pacman.velocityX;
                    pacman.y -= pacman.velocityY;
                    break;
                }
            }

            for (Block ghost : ghosts) {
                if (collision(ghost, pacman)) {
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
                        gameLoop.stop();
                        isRunning = false;
                        currentScore = score;
                        notifyGameFinished(score);

                        SwingUtilities.invokeLater(() -> {
                            try {
                                Thread.sleep(2500);
                                PacManGame.this.stopGame();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                        return;
                    }
                    resetPositions();
                }

                if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                    ghost.updateDirection('U');
                }
                ghost.x += ghost.velocityX;
                ghost.y += ghost.velocityY;
                for (Block wall : walls) {
                    if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                        ghost.x -= ghost.velocityX;
                        ghost.y -= ghost.velocityY;
                        char newDirection = directions[random.nextInt(4)];
                        ghost.updateDirection(newDirection);
                    }
                }
            }

            Block foodEaten = null;
            for (Block food : foods) {
                if (collision(pacman, food)) {
                    foodEaten = food;
                    score += 10;
                    currentScore = score;
                    notifyScoreUpdated(score);
                }
            }
            foods.remove(foodEaten);

            if (foods.isEmpty()) {
                loadMap();
                resetPositions();
                notifyGameFinished(score);
                currentScore = score;
            }
        }

        public boolean collision(Block a, Block b) {
            return a.x < b.x + b.width &&
                    a.x + a.width > b.x &&
                    a.y < b.y + b.height &&
                    a.y + a.height > b.y;
        }

        public void resetPositions() {
            pacman.reset();
            pacman.velocityX = 0;
            pacman.velocityY = 0;
            for (Block ghost : ghosts) {
                ghost.reset();
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection);
            }
        }

        public void resetGame() {
            score = 0;
            lives = 3;
            gameOver = false;
            currentScore = 0;
            loadMap();
            resetPositions();
            for (Block ghost : ghosts) {
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection);
            }
            repaint();
        }

        public void startGameLoop() {
            if (!gameLoop.isRunning()) {
                gameLoop.start();
            }
        }

        public void pauseGameLoop() {
            if (gameLoop.isRunning()) {
                gameLoop.stop();
            }
        }

        public void stopGameLoop() {
            gameLoop.stop();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isRunning && !gameOver) {
                move();
                repaint();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (!isRunning || gameOver) {
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_UP) {
                pacman.updateDirection('U');
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                pacman.updateDirection('D');
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                pacman.updateDirection('L');
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                pacman.updateDirection('R');
            }

            if (pacman.direction == 'U') {
                pacman.image = pacmanUpImage;
            } else if (pacman.direction == 'D') {
                pacman.image = pacmanDownImage;
            } else if (pacman.direction == 'L') {
                pacman.image = pacmanLeftImage;
            } else if (pacman.direction == 'R') {
                pacman.image = pacmanRightImage;
            }
        }
    }
}
