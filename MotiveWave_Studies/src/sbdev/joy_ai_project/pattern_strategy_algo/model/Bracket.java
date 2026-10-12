package sbdev.joy_ai_project.pattern_strategy_algo.model;

import java.util.Objects;

public class Bracket {
    public final int tp;
    public final int sl;

    public Bracket(int tp, int sl) {
        this.tp = tp;
        this.sl = sl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Bracket))
            return false;
        Bracket b = (Bracket) o;
        return tp == b.tp && sl == b.sl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tp, sl);
    }

    @Override
    public String toString() {
        return tp + "/" + sl;
    }
}
