package dev;
// package study_examples.dev;

// import com.motivewave.platform.sdk.common.Instrument;
// import com.motivewave.platform.sdk.order_mgmt.OrderContext;
// import com.motivewave.platform.sdk.study.StudyHeader;

// import study_examples.RenkoOverlay;
// import study_examples.RenkoOverlay.Signals;

// /** Moving Average Cross Strategy. This is based of the SampleMACross study and adds the ability to trade. */
// @StudyHeader(
//   namespace="com.sbdev", 
//   id="STRATEGY_RENKO_FOLLOW", 
//   name="Strategy Renko Follow",
//   desc="Strategy Renko Follow.",
//   menu="Custom Strategies",
//   overlay = true,
//   signals = true,
//   strategy = true,
//   autoEntry = true,
//   manualEntry = false,
//   supportsUnrealizedPL = true,
//   supportsRealizedPL = true,
//   supportsTotalPL = true)
// public class StrategyRenkoFollow extends RenkoOverlay
// {
//   @Override
//   public void onActivate(OrderContext ctx)
//   {


//   }

//   @Override
//   public void onSignal(OrderContext ctx, Object signal)
//   {
//     Instrument instr = ctx.getInstrument();
//     float position = ctx.getPositionAsFloat();
//     float qty = (getSettings().getTradeLots() * instr.getDefaultQuantityAsFloat());

//     switch(getSettings().getPositionType()) {
//     case LONG: // Only Long Positions are allowed.
//       if (position == 0 && signal == Signals.CROSS_HIGH) {
//         ctx.buy(qty); // Open Long Position
//       }
//       if (position > 0 && signal == Signals.CROSS_LOW) {
//         ctx.sell(qty); // Close Long Position
//       }
//       break;
//     case SHORT: // Only Short Positions are allowed.
//       if (position == 0 && signal == Signals.CROSS_LOW) {
//         ctx.sell(qty); // Open Short Position
//       }
//       if (position < 0 && signal == Signals.CROSS_HIGH) {
//         ctx.buy(qty); // Close Short Position
//       }
//       break;
//     default: // Both Long and Short Positions Allowed
//       qty += Math.abs(position); // Stop and Reverse if there is an open position
//       if (position <= 0 && signal == Signals.CROSS_HIGH) {
//         ctx.buy(qty); // Open Long Position
//       }
//       if (position >= 0 && signal == Signals.CROSS_LOW) {
//         ctx.sell(qty); // Open Short Position
//       }
//     }
//   }
// }
