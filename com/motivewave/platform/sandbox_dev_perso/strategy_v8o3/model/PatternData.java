package sbdev.joy_ai_project.strategy_v8o3.model;

public class PatternData {
    public String instrumentKey;
    public String intervalScale;
    public int intervalValue;
    public int tp, sl;
    public Bracket bracket;
    public String patternRenko; // format "GGRRRRRR"
    public String patternHilo1; // format "GGGGGGGG"
    public String patternHilo2; // format "RRRRRRRR"
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
                + ", intervalValue=" + intervalValue + ", bracket=" + (bracket != null ? bracket : (tp + "/" + sl))
                + ", direction='" + direction + '\'' + ", patternRenko='" + patternRenko + '\'' + ", patternHilo1='"
                + patternHilo1 + '\'' + ", patternHilo2='" + patternHilo2 + '\'' + ", count=" + count + ", countTp="
                + countTp + ", countSl=" + countSl + ", delta=" + delta + ", winrate='" + winrate + '\''
                + ", profitFactor=" + profitFactor + ", score=" + score + ", lastDateTimeEntry='" + lastDateTimeEntry
                + '\'' + '}';
    }

    // Ajoute constructeur ou builder si besoin
}
