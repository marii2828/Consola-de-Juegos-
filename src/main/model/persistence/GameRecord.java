package model.persistence;

import java.util.Date;
import java.text.SimpleDateFormat;

public class GameRecord {
    private String gameName;
    private int score;
    private Date date;
    
    public GameRecord() {}
    
    public GameRecord(String gameName, int score, Date date) {
        this.gameName = gameName;
        this.score = score;
        this.date = date;
    }
    
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public String getFormattedDate() {
        if (date == null) return "Fecha desconocida";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(date);
    }
    
    @Override
    public String toString() {
        return String.format("GameRecord[%s: %d pts on %s]", 
            gameName, score, getFormattedDate());
    }
}