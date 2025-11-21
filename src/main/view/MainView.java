package view;

import controller.MainController;
import model.core.GamePlugin;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class MainView extends JFrame {

    private static final Color PRIMARY_DARK = new Color(15, 23, 42);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_GREEN = new Color(34, 197, 94);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color BG_DARK = new Color(30, 41, 59);
    private static final Color BG_DARKER = new Color(15, 23, 42);
    private static final Color CARD_DARK = new Color(51, 65, 85);
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color TEXT_GRAY = new Color(148, 163, 184);

    private MainController controller;
    private JPanel gameContainer;
    private JLabel scoreLabel;
    private JTextArea scoresArea;
    private JList<String> gamesList;
    private List<GamePlugin> currentGamesList;
    private JPanel gamesPanel;

    public MainView(MainController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🎮 Plataforma de Juegos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_DARKER);

        mainPanel.add(createModernTopBar(), BorderLayout.NORTH);
        mainPanel.add(createStyledGamesPanel(), BorderLayout.WEST);
        mainPanel.add(createElegantGameContainer(), BorderLayout.CENTER);
        mainPanel.add(createBeautifulScoresPanel(), BorderLayout.EAST);

        add(mainPanel);
    }

    private JPanel createModernTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, new Color(30, 41, 59));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setPreferredSize(new Dimension(0, 90));
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titleLabel = new JLabel("🎮 Plataforma de Juegos");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_WHITE);

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);

        topBar.add(leftPanel, BorderLayout.WEST);

        return topBar;
    }

    private JPanel createStyledGamesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, PRIMARY_DARK),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(BG_DARK);

        JLabel iconLabel = new JLabel("🎯");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel title = new JLabel("JUEGOS DISPONIBLES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);

        headerPanel.add(iconLabel, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);

        currentGamesList = controller.getAvailableGames();
        String[] gameNames = currentGamesList.stream()
                .map(GamePlugin::getGameName)
                .toArray(String[]::new);

        gamesList = new JList<>(gameNames);
        gamesList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gamesList.setBackground(CARD_DARK);
        gamesList.setSelectionBackground(PRIMARY_BLUE);
        gamesList.setSelectionForeground(TEXT_WHITE);
        gamesList.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        gamesList.setFixedCellHeight(65);
        gamesList.setCellRenderer(new GameListRenderer());

        gamesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = gamesList.getSelectedIndex();
                if (index >= 0 && index < currentGamesList.size()) {
                    GamePlugin selectedGame = currentGamesList.get(index);
                    controller.selectGame(selectedGame);
                    updateScoresDisplay(selectedGame.getGameName());
                }
            }
        });

        gamesPanel = panel;

        JScrollPane scrollPane = new JScrollPane(gamesList);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_DARK, 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBackground(CARD_DARK);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void refreshGamesList() {
        if (gamesList != null) {
            currentGamesList = controller.getAvailableGames();
            String[] gameNames = currentGamesList.stream()
                    .map(GamePlugin::getGameName)
                    .toArray(String[]::new);

            gamesList.setListData(gameNames);
            gamesList.revalidate();
            gamesList.repaint();

            if (gamesPanel != null) {
                gamesPanel.revalidate();
                gamesPanel.repaint();
            }
        }
    }

    private class GameListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String gameName = value.toString();
            String icon = getGameIcon(gameName);
            label.setText(icon + "   " + gameName);
            label.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 17));

            if (!isSelected) {
                label.setBackground(index % 2 == 0 ? CARD_DARK : new Color(45, 55, 72));
                label.setForeground(TEXT_WHITE);
            } else {
                label.setBackground(PRIMARY_BLUE);
                label.setForeground(TEXT_WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 17));
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

    private JPanel createElegantGameContainer() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(BG_DARKER);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        gameContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 20, 20);

                g2d.setColor(BG_DARK);
                g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);

                g2d.setColor(PRIMARY_BLUE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 6, getHeight() - 6, 20, 20);
            }
        };
        gameContainer.setOpaque(false);
        gameContainer.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);

        JLabel iconWelcome = new JLabel("🎮");
        iconWelcome.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
        gbc.gridy = 0;
        welcomePanel.add(iconWelcome, gbc);

        JLabel welcomeLabel = new JLabel("Selecciona un juego para comenzar");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_WHITE);
        gbc.gridy = 1;
        welcomePanel.add(welcomeLabel, gbc);

        JLabel hintLabel = new JLabel("Explora los juegos disponibles en el menú lateral");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        hintLabel.setForeground(TEXT_GRAY);
        gbc.gridy = 2;
        welcomePanel.add(hintLabel, gbc);

        gameContainer.add(welcomePanel, BorderLayout.CENTER);
        containerPanel.add(gameContainer, BorderLayout.CENTER);

        return containerPanel;
    }

    private JPanel createBeautifulScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, PRIMARY_DARK),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(BG_DARK);

        JLabel trophyIcon = new JLabel("🏆");
        trophyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel title = new JLabel("MEJORES PUNTAJES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);

        headerPanel.add(trophyIcon, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);

        scoresArea = new JTextArea(10, 15);
        scoresArea.setEditable(false);
        scoresArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        scoresArea.setBackground(CARD_DARK);
        scoresArea.setForeground(TEXT_WHITE);
        scoresArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scoresArea.setLineWrap(true);
        scoresArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(scoresArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_DARK, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBackground(CARD_DARK);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

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
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  🏆 ").append(gameName).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n");

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
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(BG_DARK);

        JLabel icon = new JLabel("🎉", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));

        JLabel congrats = new JLabel("¡Juego terminado!", SwingConstants.CENTER);
        congrats.setFont(new Font("Segoe UI", Font.BOLD, 24));
        congrats.setForeground(TEXT_WHITE);

        JLabel gameLabel = new JLabel(gameName, SwingConstants.CENTER);
        gameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        gameLabel.setForeground(TEXT_GRAY);

        JLabel scoreLabel = new JLabel("Puntaje final: " + score, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        scoreLabel.setForeground(ACCENT_GREEN);

        panel.add(icon);
        panel.add(congrats);
        panel.add(gameLabel);
        panel.add(scoreLabel);

        JOptionPane.showMessageDialog(this, panel, "Fin del Juego", JOptionPane.PLAIN_MESSAGE);
    }
}