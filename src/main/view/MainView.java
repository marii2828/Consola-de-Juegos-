package view;

import controller.MainController;
import model.core.GamePlugin;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Vista principal mejorada estéticamente - mantiene toda la funcionalidad
 * original
 */
public class MainView extends JFrame {

    // 🎨 Paleta de colores moderna
    private static final Color PRIMARY_DARK = new Color(30, 39, 73);
    private static final Color PRIMARY_BLUE = new Color(52, 152, 219);
    private static final Color ACCENT_GREEN = new Color(46, 213, 115);
    private static final Color ACCENT_PURPLE = new Color(155, 89, 182);
    private static final Color BG_LIGHT = new Color(245, 246, 250);
    private static final Color CARD_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_DARK = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(149, 165, 166);

    private MainController controller;
    private JPanel gameContainer;
    private JLabel scoreLabel;
    private JTextArea scoresArea;

    public MainView(MainController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🎮 Plataforma de Juegos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        // Panel principal con fondo moderno
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_LIGHT);

        // Agregar componentes
        mainPanel.add(createModernTopBar(), BorderLayout.NORTH);
        mainPanel.add(createStyledGamesPanel(), BorderLayout.WEST);
        mainPanel.add(createElegantGameContainer(), BorderLayout.CENTER);
        mainPanel.add(createBeautifulScoresPanel(), BorderLayout.EAST);
        mainPanel.add(createModernInfoPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * 🎨 Barra superior moderna con gradiente
     */
    private JPanel createModernTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, new Color(41, 50, 80));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setPreferredSize(new Dimension(0, 70));
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // Título
        JLabel titleLabel = new JLabel("🎮 GAME HUB PRO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Plataforma de Juegos Modular");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(189, 195, 199));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(subtitleLabel);

        topBar.add(leftPanel, BorderLayout.WEST);

        return topBar;
    }

    /**
     * 🎨 Panel de juegos estilizado
     */
    private JPanel createStyledGamesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(CARD_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 2, BG_LIGHT),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Encabezado con icono
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(CARD_WHITE);

        JLabel iconLabel = new JLabel("🎯");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel title = new JLabel("JUEGOS DISPONIBLES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);

        headerPanel.add(iconLabel, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);

        // Lista de juegos con renderer personalizado
        List<GamePlugin> games = controller.getAvailableGames();
        String[] gameNames = games.stream()
                .map(GamePlugin::getGameName)
                .toArray(String[]::new);

        JList<String> gamesList = new JList<>(gameNames);
        gamesList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gamesList.setBackground(BG_LIGHT);
        gamesList.setSelectionBackground(PRIMARY_BLUE);
        gamesList.setSelectionForeground(Color.WHITE);
        gamesList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gamesList.setFixedCellHeight(50);
        gamesList.setCellRenderer(new GameListRenderer());

        gamesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = gamesList.getSelectedIndex();
                if (index >= 0) {
                    GamePlugin selectedGame = games.get(index);
                    controller.selectGame(selectedGame);
                    updateScoresDisplay(selectedGame.getGameName());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(gamesList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 🎨 Renderer personalizado para la lista de juegos
     */
    private class GameListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // Agregar icono según el juego
            String gameName = value.toString();
            String icon = getGameIcon(gameName);
            label.setText(icon + "  " + gameName);
            label.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            if (!isSelected) {
                label.setBackground(index % 2 == 0 ? CARD_WHITE : BG_LIGHT);
                label.setForeground(TEXT_DARK);
            } else {
                label.setBackground(PRIMARY_BLUE);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 15));
            }

            return label;
        }

        private String getGameIcon(String gameName) {
            String lower = gameName.toLowerCase();
            if (lower.contains("snake") || lower.contains("serpiente"))
                return "🐍";
            if (lower.contains("tic") || lower.contains("tac"))
                return "⭕";
            if (lower.contains("ahorcado") || lower.contains("hangman"))
                return "🎯";
            if (lower.contains("tetris"))
                return "🟦";
            if (lower.contains("pacman"))
                return "👾";
            if (lower.contains("puzzle"))
                return "🧩";
            if (lower.contains("prueba") || lower.contains("test"))
                return "🎯";
            return "🎮";
        }
    }

    /**
     * 🎨 Contenedor elegante para el juego
     */
    private JPanel createElegantGameContainer() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(BG_LIGHT);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Contenedor del juego con sombra
        gameContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sombra
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 15, 15);

                // Fondo blanco
                g2d.setColor(CARD_WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);
            }
        };
        gameContainer.setOpaque(false);
        gameContainer.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Mensaje de bienvenida elegante
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel iconWelcome = new JLabel("🎮");
        iconWelcome.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        gbc.gridy = 0;
        welcomePanel.add(iconWelcome, gbc);

        JLabel welcomeLabel = new JLabel("Selecciona un juego para comenzar");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        welcomeLabel.setForeground(TEXT_LIGHT);
        gbc.gridy = 1;
        welcomePanel.add(welcomeLabel, gbc);

        JLabel hintLabel = new JLabel("Explora los juegos disponibles en el menú lateral");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hintLabel.setForeground(new Color(189, 195, 199));
        gbc.gridy = 2;
        welcomePanel.add(hintLabel, gbc);

        gameContainer.add(welcomePanel, BorderLayout.CENTER);
        containerPanel.add(gameContainer, BorderLayout.CENTER);

        return containerPanel;
    }

    /**
     * 🎨 Panel de puntajes hermoso
     */
    private JPanel createBeautifulScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(CARD_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, BG_LIGHT),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Encabezado
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(CARD_WHITE);

        JLabel trophyIcon = new JLabel("🏆");
        trophyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel title = new JLabel("MEJORES PUNTAJES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);

        headerPanel.add(trophyIcon, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);

        // Área de texto estilizada
        scoresArea = new JTextArea(10, 15);
        scoresArea.setEditable(false);
        scoresArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        scoresArea.setBackground(BG_LIGHT);
        scoresArea.setForeground(TEXT_DARK);
        scoresArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        scoresArea.setLineWrap(true);
        scoresArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(scoresArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 🎨 Panel de información moderno
     */
    private JPanel createModernInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_WHITE);
        panel.setPreferredSize(new Dimension(0, 60));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, BG_LIGHT),
                BorderFactory.createEmptyBorder(15, 30, 15, 30)));

        // Score con diseño mejorado
        JPanel scorePanel = new JPanel(new BorderLayout(12, 0));
        scorePanel.setOpaque(false);
        scorePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 2, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));

        JLabel starIcon = new JLabel("⭐");
        starIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        scoreLabel = new JLabel("Puntaje actual: 0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setForeground(ACCENT_GREEN);

        scorePanel.add(starIcon, BorderLayout.WEST);
        scorePanel.add(scoreLabel, BorderLayout.CENTER);

        panel.add(scorePanel, BorderLayout.WEST);

        return panel;
    }

    // ✅ Métodos públicos ORIGINALES - Sin cambios funcionales

    public void displayGame(JPanel gamePanel) {
        gameContainer.removeAll();
        gameContainer.add(gamePanel, BorderLayout.CENTER);
        gameContainer.revalidate();
        gameContainer.repaint();
    }

    public void updateGameInfo(String gameName, String description) {
        setTitle("🎮 Plataforma de Juegos - " + gameName);
    }

    public void updateCurrentScore(int score) {
        scoreLabel.setText("Puntaje actual: " + score);
    }

    public void updateScoresDisplay(String gameName) {
        List<String> topScores = controller.getTopScores(gameName);

        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("  🏆 ").append(gameName).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━\n\n");

        if (topScores.isEmpty()) {
            sb.append("  Sin puntajes registrados\n");
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                if (i == 0)
                    sb.append("🥇 ");
                else if (i == 1)
                    sb.append("🥈 ");
                else if (i == 2)
                    sb.append("🥉 ");
                else
                    sb.append("   ");

                sb.append(topScores.get(i)).append("\n");
            }
        }

        scoresArea.setText(sb.toString());
    }

    public void showGameFinishedMessage(String gameName, int score) {
        // Panel personalizado para el diálogo
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel("🎉", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel congrats = new JLabel("¡Juego terminado!", SwingConstants.CENTER);
        congrats.setFont(new Font("Segoe UI", Font.BOLD, 20));
        congrats.setForeground(TEXT_DARK);

        JLabel gameLabel = new JLabel(gameName, SwingConstants.CENTER);
        gameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gameLabel.setForeground(TEXT_LIGHT);

        JLabel scoreLabel = new JLabel("Puntaje final: " + score, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setForeground(ACCENT_GREEN);

        panel.add(icon);
        panel.add(congrats);
        panel.add(gameLabel);
        panel.add(scoreLabel);

        JOptionPane.showMessageDialog(this, panel, "Fin del Juego", JOptionPane.PLAIN_MESSAGE);
    }
}