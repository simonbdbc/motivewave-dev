package dev;
// package study_examples.dev;

// import com.motivewave.platform.sdk.order_mgmt.OrderContext;
// import com.motivewave.platform.sdk.study.StudyHeader;

// import study_examples.RenkoOverlay;
// import study_examples.RenkoOverlay.Signals;

// /** Moving Average Cross Strategy. This is based of the SampleMACross study and adds the ability to trade. */
// @StudyHeader(
//   namespace="com.sbdev", 
//   id="STRATEGY_TEST", 
//   name="Strategy Test",
//   desc="Sample Test Strategy.",
//   menu="Custom Strategies",
//   overlay = true,
//   signals = true,
//   strategy = true,
//   autoEntry = true,
//   manualEntry = false,
//   supportsUnrealizedPL = true,
//   supportsRealizedPL = true,
//   supportsTotalPL = true)
// public class StrategyTest extends RenkoOverlay
// {
//   @Override
//   public void onActivate(OrderContext ctx)
//   {


//   }

//   @Override
//   public void onSignal(OrderContext ctx, Object signal)
//   {
//     var instr = ctx.getInstrument();
//     int position = ctx.getPosition();
//     int qty = (getSettings().getTradeLots() * instr.getDefaultQuantity());

//     qty += Math.abs(position); // Stop and Reverse if there is an open position
//     if (position <= 0 && signal == Signals.CROSS_HIGH) {
//       ctx.buy(qty); // Open Long Position
//     }
//     if (position >= 0 && signal == Signals.CROSS_LOW) {
//       ctx.sell(qty); // Open Short Position
//     }
//   }
// }
