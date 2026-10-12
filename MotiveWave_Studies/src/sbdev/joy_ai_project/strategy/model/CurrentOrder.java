package sbdev.joy_ai_project.strategy.model;

public class CurrentOrder {
    public double entryPrice = 0.0;
    public double takeProfitPrice = 0.0;
    public double stopLossPrice = 0.0;

    public CurrentOrder(double entryPrice, double takeProfitPrice, double stopLossPrice) {
        this.entryPrice = entryPrice;
        this.takeProfitPrice = takeProfitPrice;
        this.stopLossPrice = stopLossPrice;
    }
}
