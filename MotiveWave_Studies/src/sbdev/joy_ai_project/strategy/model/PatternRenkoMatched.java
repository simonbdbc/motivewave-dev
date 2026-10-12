package sbdev.joy_ai_project.strategy.model;

public class PatternRenkoMatched {
    public String instrumentKey;
    public String intervalScale;
    public int intervalValue;
    public double renkoSize;
    public String patternRenko; // format "GGRRRRRR"
    public String patternHilo1; // format "GGGGGGGG"
    public String patternHilo2; // format "RRRRRRRR"
    public Long timestamp;
    public String date;
    public String updated_at;
    public Bracket bracket;
    public String direction;
    public int count;
    public String lastDateTimeEntry;
    public int countTp, countSl, delta;
    public String winrate;
    public double profitFactor;
    public double score;

    @Override
    public String toString() {
        return "PatternData{" + "instrumentKey='" + instrumentKey + '\'' + ", intervalScale='" + intervalScale + '\''
                + ", intervalValue=" + intervalValue + ", renkoSize=" + renkoSize + ", timestamp=" + timestamp
                + ", date=" + date + ", updated_at=" + updated_at + ", bracket=" + bracket.toString() + ", direction='"
                + direction + '\'' + ", patternRenko='" + patternRenko + '\'' + ", patternHilo1='" + patternHilo1 + '\''
                + ", patternHilo2='" + patternHilo2 + '\'' + ", count=" + count + ", countTp=" + countTp + ", countSl="
                + countSl + ", delta=" + delta + ", winrate='" + winrate + '\'' + ", profitFactor=" + profitFactor
                + ", score=" + score + ", lastDateTimeEntry='" + lastDateTimeEntry + '\'' + '}';
    }

    // Ajoute constructeur ou builder si besoin
}
