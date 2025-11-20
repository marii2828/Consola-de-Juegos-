package model.persistence;

import java.util.*;
import java.io.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ScoreManager {
    private static ScoreManager instance;
    private Map<String, List<GameRecord>> gameRecords;
    private final String DATA_FILE = "data/scores.json";
    private final Gson gson;

    private ScoreManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.gameRecords = new HashMap<>();
        loadScores();
    }

    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }

    public void updateScore(String gameName, int score) {
        try {
            List<GameRecord> records = gameRecords.getOrDefault(gameName, new ArrayList<>());

            GameRecord newRecord = new GameRecord(gameName, score, new Date());
            records.add(newRecord);
            records.sort((r1, r2) -> Integer.compare(r2.getScore(), r1.getScore()));

            if (records.size() > 3) {
                records = new ArrayList<>(records.subList(0, 3));
            }

            gameRecords.put(gameName, records);

            saveScores();

            System.out.println(" Score actualizado: " + gameName + " - " + score);

        } catch (Exception e) {
            System.err.println(" Error actualizando score: " + e.getMessage());
        }
    }

    public List<GameRecord> getTopScores(String gameName) {
        return gameRecords.getOrDefault(gameName, new ArrayList<>());
    }


    public List<String> getTopScoresAsStrings(String gameName) {
        List<GameRecord> records = getTopScores(gameName);
        List<String> result = new ArrayList<>();

        if (records.isEmpty()) {
            result.add("No hay records aún");
            return result;
        }

        for (int i = 0; i < records.size(); i++) {
            GameRecord record = records.get(i);
            result.add((i + 1) + ". " + record.getScore() + " pts - " + record.getFormattedDate());
        }

        return result;
    }

    private void loadScores() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                System.out.println("Archivo de scores no encontrado, creando uno nuevo");
                ensureDataDirectory();
                return;
            }

            FileReader reader = new FileReader(file);
            ScoreData data = gson.fromJson(reader, ScoreData.class);

            if (data != null && data.records != null) {
                this.gameRecords = data.records;
                System.out.println(" Scores cargados: " + gameRecords.size() + " juegos");
            }

            reader.close();

        } catch (Exception e) {
            System.err.println(" Error cargando scores: " + e.getMessage());
            this.gameRecords = new HashMap<>();
        }
    }

    private void saveScores() {
        try {
            ensureDataDirectory();

            FileWriter writer = new FileWriter(DATA_FILE);
            ScoreData data = new ScoreData(gameRecords);
            gson.toJson(data, writer);
            writer.flush();
            writer.close();

            System.out.println(" Scores guardados en: " + DATA_FILE);

        } catch (Exception e) {
            System.err.println(" Error guardando scores: " + e.getMessage());
            throw new RuntimeException("No se pudo guardar los scores", e);
        }
    }

    private void ensureDataDirectory() {
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println(" Error creando directorio data: " + e.getMessage());
        }
    }

    private static class ScoreData {
        Map<String, List<GameRecord>> records;

        ScoreData(Map<String, List<GameRecord>> records) {
            this.records = records;
        }
    }

    public void printAllScores() {
        System.out.println("=== TODOS LOS SCORES ===");
        for (String gameName : gameRecords.keySet()) {
            System.out.println("🎮 " + gameName + ":");
            List<GameRecord> records = gameRecords.get(gameName);
            for (GameRecord record : records) {
                System.out.println("   - " + record.getScore() + " pts (" + record.getFormattedDate() + ")");
            }
        }
    }
}