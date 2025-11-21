package model.games;

import model.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class AhorcadoGame extends AbstractGame {
    private static AhorcadoGame instance;

    private JPanel mainPanel;
    private StartPanel startPanel;
    private GamePanel gamePanel;

    private String[] palabras = {
        "pluma", "programacion", "java", "computadora", "algoritmo", 
        "desarrollo", "software", "hardware", "aplicacion",
        "tecnologia", "informatica", "sistema", "proyecto"
    };
    
    private String palabraSecreta;
    private StringBuilder palabraMostrada;
    private int intentos;
    private int intentosIniciales = 6;
    private Set<Character> letrasUsadas;
    private int score;

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(52, 73, 94);
    private static final Color COLOR_EXITO = new Color(46, 204, 113);
    private static final Color COLOR_ERROR = new Color(231, 76, 60);
    private static final Color COLOR_FONDO = new Color(236, 240, 241);
    private static final Color COLOR_BOTON = new Color(52, 152, 219);
    private static final Color COLOR_BOTON_HOVER = new Color(41, 128, 185);

    private AhorcadoGame() {
        this.letrasUsadas = new HashSet<>();
        initializeGame();
    }

    public static AhorcadoGame createInstance() {
        if (instance == null) {
            instance = new AhorcadoGame();
        }
        return instance;
    }

    private void initializeGame() {
        this.mainPanel = new JPanel(new CardLayout());
        
        palabraSecreta = "pluma";
        palabraMostrada = new StringBuilder();
        for (int i = 0; i < palabraSecreta.length(); i++) {
            palabraMostrada.append("_ ");
        }
        intentos = intentosIniciales;
        letrasUsadas.clear();
        score = 0;
        
        this.startPanel = new StartPanel();
        this.gamePanel = new GamePanel();

        mainPanel.add(startPanel, "START");
        mainPanel.add(gamePanel, "GAME");

        showStartScreen();
    }

    @Override
    public String getGameName() {
        return "Ahorcado";
    }

    @Override
    public String getGameVersion() {
        return "1.0";
    }

    @Override
    public String getGameDescription() {
        return "Adivina la palabra letra por letra antes de quedarte sin intentos!";
    }

    @Override
    public JPanel getGamePanel() {
        if (mainPanel == null) {
            initializeGame();
        }
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
    }

    private void resetGame() {
        palabraSecreta = "pluma";
        palabraMostrada = new StringBuilder();
        for (int i = 0; i < palabraSecreta.length(); i++) {
            palabraMostrada.append("_ ");
        }
        intentos = intentosIniciales;
        letrasUsadas.clear();
        score = 0;
        
        if (gamePanel != null) {
            gamePanel.reset();
            gamePanel.limpiarJuego();
        }
    }

    private void procesarLetra(char letra) {
        if (letrasUsadas.contains(letra)) {
            return;
        }

        letrasUsadas.add(letra);
        boolean letraCorrecta = false;

        for (int i = 0; i < palabraSecreta.length(); i++) {
            if (palabraSecreta.charAt(i) == letra) {
                palabraMostrada.setCharAt(i * 2, letra);
                letraCorrecta = true;
            }
        }

        if (gamePanel != null) {
            gamePanel.actualizarPalabra(palabraMostrada.toString());
            gamePanel.actualizarLetrasUsadas(letrasUsadas);
        }

        if (!letraCorrecta) {
            intentos--;
            if (gamePanel != null) {
                gamePanel.actualizarIntentos(intentos);
                gamePanel.actualizarDibujo(6 - intentos);
            }
            if (intentos == 0) {
                finDelJuego(false);
            }
        } else {
            if (!palabraMostrada.toString().contains("_")) {
                finDelJuego(true);
            }
        }
    }

    private void finDelJuego(boolean ganado) {
        isRunning = false;
        
        if (ganado) {
            score = (intentos * 50) + (palabraSecreta.length() * 20);
        } else {
            score = letrasUsadas.size() * 10;
        }

        if (gamePanel != null) {
            gamePanel.gameOver(ganado, palabraSecreta);
        }

        notifyGameFinished(score);
        
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                SwingUtilities.invokeLater(() -> {
                    resetGame();
                    showStartScreen();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> {
                    resetGame();
                    showStartScreen();
                });
            }
        }).start();
    }

    private class StartPanel extends JPanel {
        public StartPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(20, 20, 40));
            setPreferredSize(new Dimension(700, 600));

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));

            JLabel titleLabel = new JLabel("🎯 AHORCADO");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setAlignmentX(CENTER_ALIGNMENT);
            centerPanel.add(titleLabel);

            centerPanel.add(Box.createVerticalStrut(30));

            JLabel instructions = new JLabel("<html><center>" +
                    "Adivina la palabra letra por letra<br>" +
                    "Tienes 6 intentos<br>" +
                    "¡Buena suerte!" +
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
                AhorcadoGame.this.startGame();
            });
            centerPanel.add(startButton);

            add(centerPanel, BorderLayout.CENTER);
        }
    }

    private class GamePanel extends JPanel {
        private JLabel lblPalabra;
        private JLabel lblIntentos;
        private JLabel lblLetrasUsadas;
        private JPanel panelAhorcado;
        private JPanel panelTeclado;
        private JButton[] botonesLetras;
        private DibujoAhorcado dibujoAhorcado;

        public GamePanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(COLOR_FONDO);
            setBorder(new EmptyBorder(10, 10, 10, 10));
            crearComponentes();
        }

        private void crearComponentes() {
            JPanel topPanel = new JPanel(new BorderLayout(1, 1));
            topPanel.setBackground(COLOR_FONDO);
            topPanel.setBorder(new EmptyBorder(2, 15, 10, 15));

            panelAhorcado = new JPanel(new BorderLayout(1, 1));
            panelAhorcado.setBackground(Color.WHITE);
            panelAhorcado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SECUNDARIO, 2),
                new EmptyBorder(1, 15, 15, 15)
            ));

            dibujoAhorcado = new DibujoAhorcado();
            dibujoAhorcado.setPreferredSize(new Dimension(220, 220));
            dibujoAhorcado.setBackground(Color.WHITE);

            JPanel panelStats = new JPanel(new BorderLayout(20, 10));
            panelStats.setBackground(Color.WHITE);
            panelStats.setBorder(new EmptyBorder(0, 0, 1, 0));

            JPanel leftStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            leftStats.setBackground(Color.WHITE);
            
            lblIntentos = new JLabel("Intentos restantes: " + intentos);
            lblIntentos.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblIntentos.setForeground(COLOR_SECUNDARIO);

            JPanel rightStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            rightStats.setBackground(Color.WHITE);
            
            lblLetrasUsadas = new JLabel("Letras usadas: ");
            lblLetrasUsadas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblLetrasUsadas.setForeground(COLOR_SECUNDARIO);

            leftStats.add(lblIntentos);
            rightStats.add(lblLetrasUsadas);
            
            panelStats.add(leftStats, BorderLayout.WEST);
            panelStats.add(rightStats, BorderLayout.EAST);

            String palabraInicial = (palabraMostrada != null && palabraMostrada.length() > 0) 
                ? palabraMostrada.toString().trim() 
                : "_ _ _ _ _";
            lblPalabra = new JLabel(palabraInicial, SwingConstants.CENTER);
            lblPalabra.setFont(new Font("Courier New", Font.BOLD, 32));
            lblPalabra.setForeground(COLOR_PRIMARIO);
            lblPalabra.setBorder(new EmptyBorder(5, 15, 5, 15));
            lblPalabra.setOpaque(false);
            lblPalabra.setBackground(Color.WHITE);

            JPanel palabraPanel = new JPanel(new BorderLayout());
            palabraPanel.setBackground(Color.WHITE);
            palabraPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, COLOR_PRIMARIO),
                new EmptyBorder(3, 0, 2, 0)
            ));
            palabraPanel.add(lblPalabra, BorderLayout.CENTER);
            lblPalabra.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
            centerPanel.setBackground(Color.WHITE);
            centerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            centerPanel.add(dibujoAhorcado, BorderLayout.NORTH);
            centerPanel.add(palabraPanel, BorderLayout.SOUTH);
            
            dibujoAhorcado.setAlignmentY(Component.TOP_ALIGNMENT);
            dibujoAhorcado.setAlignmentX(Component.CENTER_ALIGNMENT);

            panelAhorcado.add(panelStats, BorderLayout.NORTH);
            panelAhorcado.add(centerPanel, BorderLayout.CENTER);
            
            ((BorderLayout)panelAhorcado.getLayout()).setVgap(0);

            topPanel.add(panelAhorcado, BorderLayout.CENTER);

            panelTeclado = new JPanel();
            panelTeclado.setLayout(new BoxLayout(panelTeclado, BoxLayout.Y_AXIS));
            panelTeclado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(COLOR_SECUNDARIO, 1),
                    "Teclado",
                    0, 0,
                    new Font("Segoe UI", Font.BOLD, 12),
                    COLOR_SECUNDARIO
                ),
                new EmptyBorder(8, 10, 8, 10)
            ));
            panelTeclado.setBackground(COLOR_FONDO);

            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            row1.setBackground(COLOR_FONDO);
            row2.setBackground(COLOR_FONDO);
            row3.setBackground(COLOR_FONDO);

            botonesLetras = new JButton[26];
            char letra = 'a';
            for (int i = 0; i < 26; i++) {
                botonesLetras[i] = crearBotonLetra(String.valueOf(letra));
                final char currentLetra = letra;
                botonesLetras[i].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isRunning) {
                            procesarLetra(currentLetra);
                            JButton btn = (JButton)e.getSource();
                            btn.setEnabled(false);
                            btn.setBackground(COLOR_SECUNDARIO);
                            btn.setForeground(Color.WHITE);
                        }
                    }
                });
                
                if (i < 9) {
                    row1.add(botonesLetras[i]);
                } else if (i < 18) {
                    row2.add(botonesLetras[i]);
                } else {
                    row3.add(botonesLetras[i]);
                }
                
                letra++;
            }

            panelTeclado.add(row1);
            panelTeclado.add(Box.createVerticalStrut(2));
            panelTeclado.add(row2);
            panelTeclado.add(Box.createVerticalStrut(2));
            panelTeclado.add(row3);

            add(topPanel, BorderLayout.CENTER);
            add(panelTeclado, BorderLayout.SOUTH);
        }

        private JButton crearBotonLetra(String letra) {
            JButton boton = new JButton(letra.toUpperCase());
            boton.setFont(new Font("Segoe UI", Font.BOLD, 11));
            boton.setForeground(Color.WHITE);
            boton.setBackground(COLOR_BOTON);
            boton.setFocusPainted(false);
            boton.setBorderPainted(true);
            boton.setBorder(BorderFactory.createRaisedBevelBorder());
            boton.setPreferredSize(new Dimension(28, 28));
            boton.setMinimumSize(new Dimension(28, 28));
            boton.setMaximumSize(new Dimension(28, 28));
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            boton.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    if (boton.isEnabled()) {
                        boton.setBackground(COLOR_BOTON_HOVER);
                        boton.setBorder(BorderFactory.createLoweredBevelBorder());
                    }
                }
                public void mouseExited(MouseEvent evt) {
                    if (boton.isEnabled()) {
                        boton.setBackground(COLOR_BOTON);
                        boton.setBorder(BorderFactory.createRaisedBevelBorder());
                    }
                }
            });

            return boton;
        }

        public void reset() {
            if (lblPalabra != null) {
                actualizarPalabra(palabraMostrada.toString());
                actualizarIntentos(intentos);
                actualizarDibujo(0);
                actualizarLetrasUsadas(letrasUsadas);
                
                if (botonesLetras != null) {
                    for (JButton boton : botonesLetras) {
                        boton.setEnabled(true);
                        boton.setBackground(COLOR_BOTON);
                    }
                }
                
                revalidate();
                repaint();
            }
        }

        public void actualizarPalabra(String palabra) {
            if (lblPalabra != null) {
                lblPalabra.setText(palabra.trim());
                lblPalabra.revalidate();
                lblPalabra.repaint();
            }
        }

        public void actualizarIntentos(int intentosRestantes) {
            if (lblIntentos != null) {
                lblIntentos.setText("Intentos restantes: " + intentosRestantes);
                lblIntentos.setForeground(intentosRestantes <= 2 ? COLOR_ERROR : COLOR_SECUNDARIO);
            }
        }

        public void actualizarLetrasUsadas(Set<Character> letras) {
            if (lblLetrasUsadas != null) {
                StringBuilder letrasStr = new StringBuilder("Letras usadas: ");
                if (letras != null && !letras.isEmpty()) {
                    for (char letra : letras) {
                        letrasStr.append(letra).append(" ");
                    }
                }
                String texto = letrasStr.toString().trim();
                lblLetrasUsadas.setText(texto);
                lblLetrasUsadas.setToolTipText(texto);
            }
        }

        public void actualizarDibujo(int partes) {
            if (dibujoAhorcado != null) {
                dibujoAhorcado.setPartesVisible(partes);
            }
        }

        public void gameOver(boolean ganado, String palabra) {
            for (JButton boton : botonesLetras) {
                boton.setEnabled(false);
                boton.setBackground(COLOR_SECUNDARIO);
                boton.setForeground(Color.WHITE);
            }

            if (dibujoAhorcado != null && !ganado) {
                dibujoAhorcado.setPartesVisible(6);
            }

            String mensaje = ganado ? 
                "¡Felicidades! Has adivinado la palabra: " + palabra :
                "¡Has perdido! La palabra era: " + palabra;
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(AhorcadoGame.this.mainPanel,
                    mensaje + "\nPuntos: " + score,
                    ganado ? "¡Victoria!" : "Fin del juego",
                    ganado ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            });
        }
        
        public void limpiarJuego() {
            if (botonesLetras != null) {
                for (JButton boton : botonesLetras) {
                    boton.setEnabled(true);
                    boton.setBackground(COLOR_BOTON);
                    boton.setForeground(Color.WHITE);
                }
            }
            
            if (dibujoAhorcado != null) {
                dibujoAhorcado.setPartesVisible(0);
            }
        }
    }

    private class DibujoAhorcado extends JPanel {
        private int partesVisibles = 0;

        public void setPartesVisible(int partes) {
            this.partesVisibles = partes;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int offsetX = (getWidth() - 200) / 2;
            int offsetY = 20;

            g2d.setColor(new Color(139, 69, 19));
            g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(50 + offsetX, 180 + offsetY, 150 + offsetX, 180 + offsetY);
            g2d.drawLine(100 + offsetX, 180 + offsetY, 100 + offsetX, 50 + offsetY);
            g2d.drawLine(100 + offsetX, 50 + offsetY, 150 + offsetX, 50 + offsetY);

            g2d.setColor(new Color(189, 195, 199));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(150 + offsetX, 50 + offsetY, 150 + offsetX, 70 + offsetY);

            g2d.setColor(COLOR_ERROR);
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (partesVisibles >= 1) {
                g2d.drawOval(140 + offsetX, 70 + offsetY, 20, 20);
                g2d.setColor(COLOR_SECUNDARIO);
                g2d.fillOval(144 + offsetX, 76 + offsetY, 3, 3);
                g2d.fillOval(153 + offsetX, 76 + offsetY, 3, 3);
                g2d.setColor(COLOR_ERROR);
            }

            if (partesVisibles >= 2) {
                g2d.drawLine(150 + offsetX, 90 + offsetY, 150 + offsetX, 130 + offsetY);
            }

            if (partesVisibles >= 3) {
                g2d.drawLine(150 + offsetX, 100 + offsetY, 130 + offsetX, 115 + offsetY);
            }

            if (partesVisibles >= 4) {
                g2d.drawLine(150 + offsetX, 100 + offsetY, 170 + offsetX, 115 + offsetY);
            }

            if (partesVisibles >= 5) {
                g2d.drawLine(150 + offsetX, 130 + offsetY, 135 + offsetX, 155 + offsetY);
            }

            if (partesVisibles >= 6) {
                g2d.drawLine(150 + offsetX, 130 + offsetY, 165 + offsetX, 155 + offsetY);
            }
        }
    }
}
