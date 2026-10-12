package sbdev.joy_ai_project.strategy_v8o1;

import java.util.ArrayList;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.TimeFrame;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.TifDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.order_mgmt.Order;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;

@StudyHeader(namespace = "com.sbdev", id = "STRATEGY_TRADE_V8", name = "Strategy Trade V8", label = "Strategy Trade V8", desc = "Strategy Trade Manager", menu = "SB Dev", overlay = true, signals = true, strategy = true, autoEntry = true, manualEntry = false, supportsStopPL = true, supportsUnrealizedPL = true, supportsRealizedPL = true, supportsTotalPL = true, supportsTargetPL = true, supportsRiskRatio = true, showTradeOptions = true, requiresBarUpdates = true)
public class StrategyTradeV8 extends StrategySignalV8 {

  public final static String STEP = "step";
  // Entry inputs
  public final static String TRADE_LOTS = "tradeLots", TIME_IN_FORCE = "tif";
  // Exit inputs
  public final static String EXIT = "exit";
  // Stop Loss
  public final static String BREAK_EVEN_ENABLED = "breakEvenEnabled",
      BREAK_EVEN = "breakEvenPips", COVER = "coverPips",
      MAX_LOSS = "maxLoss", ATR_PERIOD = "atrPeriod", ATR_MULTIPLIER = "atrMultiplier", ATR_ENABLED = "atrEnabled";
  // Risk Session
  public final static String SESSION_MAX_WIN = "sessionMaxWin", SESSION_MAX_LOSS = "sessionMaxLoss",
      MAX_SUCCESSIVE_LOSS_ENABLED = "maxSuccessiveLossEnabled", MAX_SUCCESSIVE_LOSS = "maxSuccessiveLoss";

  public final static long ENTRY_DELAY = 2000;

  // Step Types
  // final static String PIPS = "PIPS", TICKS = "TICKS";

  final static String INPUT = "input";

  protected int successiveLoss = 0;
  protected Boolean pnlMax = false;
  protected Double pnlSession = 0.00;
  protected Boolean startSession = false;

  @Override
  public void destroy() {
    super.destroy();
    clearOrders();
  }

  @Override
  public void initialize(Defaults defaults) {
    super.initialize(defaults);

    // Study Settings
    SettingsDescriptor sd = getSettingsDescriptor();

    sd.addTab(getExitStrategyTab());
    sd.addTab(getRiskTab());

    sd.addDependency(
        new EnabledDependency(BREAK_EVEN_ENABLED, BREAK_EVEN, COVER, ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
    sd.addDependency(new EnabledDependency(ATR_ENABLED, ATR_MULTIPLIER, ATR_PERIOD));
    sd.addDependency(new EnabledDependency(MAX_SUCCESSIVE_LOSS_ENABLED, MAX_SUCCESSIVE_LOSS));
    setSettingsDescriptor(sd);

  }

  protected SettingTab getExitStrategyTab() {

    var tab = new SettingTab("Exit Strategy");

    var grp = tab.addGroup("Entry");
    grp.addRow(new IntegerDescriptor(TRADE_LOTS, "Trade Lots", 1, 1, 9999, 1),
        new TifDescriptor(TIME_IN_FORCE, "Time in Force", Enums.TIF.GTC));

    grp = tab.addGroup("Bracket");
    grp.addRow(new IntegerDescriptor(EXIT, "Take Profit", 60, 1, 9999, 1));
    grp.addRow(new IntegerDescriptor(MAX_LOSS, "Stop Loss", 60, 0, 999, 1));

    grp = tab.addGroup("Break Even");
    grp.addRow(new IntegerDescriptor(BREAK_EVEN, "Break Even", 80, 0, 999, 1),
        new IntegerDescriptor(COVER, "Cover", 0, 0, 99, 1),
        new BooleanDescriptor(BREAK_EVEN_ENABLED, "Break Even Enabled", false));

    grp = tab.addGroup("ATR Stop");
    grp.addRow(new BooleanDescriptor(ATR_ENABLED, "ATR Enabled", false));
    grp.addRow(new IntegerDescriptor(ATR_PERIOD, "ATR Period", 40, 1, 999, 1),
        new DoubleDescriptor(ATR_MULTIPLIER, "ATR Multiplier", 5.0, 0.1, 999, 0.1));

    return tab;
  }

  protected SettingTab getRiskTab() {
    var tab = new SettingTab("Risk Session");
    var grp = tab.addGroup("Win");
    grp.addRow(new IntegerDescriptor(SESSION_MAX_WIN, "Max Win Session", 700, 0, 9999, 1));

    grp = tab.addGroup("Loss");
    grp.addRow(new IntegerDescriptor(SESSION_MAX_LOSS, "Max Loss Session", 600, 0, 9999, 1));

    grp = tab.addGroup("Successive Loss");
    grp.addRow(new BooleanDescriptor(MAX_SUCCESSIVE_LOSS_ENABLED, "Max Successive Loss Enabled", false));
    grp.addRow(new IntegerDescriptor(MAX_SUCCESSIVE_LOSS, "Max Successive Loss", 3, 1, 99, 1));
    return tab;
  }

  @Override
  public void onActivate(OrderContext ctx) {
    debug("onActivate");
    if (getSettings().isEnterOnActivate()) {
      debug("isEnterOnActivate");
    }

    Double pnlTotal = ctx.getTotalRealizedPnL();
    pnlSession = 0.00;
    int max = getSettings().getInteger(SESSION_MAX_WIN);
    int min = getSettings().getInteger(SESSION_MAX_LOSS) * -1;
    debug("Risk Management -> check PnL Total : " + pnlTotal + ", Min: " + min + ", Max: " + max);

  }

  @Override
  public void onSignal(OrderContext ctx, Object signal) {
    float position = ctx.getPositionAsFloat();

    Double pnl = ctx.getTotalRealizedPnL();
    Double lastPnl = startSession ? 0.00 : ctx.getRealizedPnL();
    int max = getSettings().getInteger(SESSION_MAX_WIN);
    int min = getSettings().getInteger(SESSION_MAX_LOSS) * -1;
    Boolean successiveLossEnabled = getSettings().getBoolean(MAX_SUCCESSIVE_LOSS_ENABLED);
    int maxSuccessiveLoss = getSettings().getInteger(MAX_SUCCESSIVE_LOSS);

    if (min < pnlSession && pnlSession < max) {
      pnlSession += lastPnl;
      if (min < pnlSession && pnlSession < max) {
        pnlMax = false;
      } else {
        pnlMax = true;
      }
    } else {
      pnlMax = true;
    }
    // var series = ctx.getDataContext().getDataSeries();

    debug("Risk Management -> check PnL Total : " + pnl + ", PnL Session: " + pnlSession + ", Min: " + min + ", Max: "
        + max);

    if (successiveLossEnabled) {
      if (lastPnl > 0) {
        successiveLoss = 0;
      }
      if (lastPnl < 0) {
        successiveLoss += 1;
        // if (pnlMax == true) {
        // successiveLoss = 99;
        // debug("One Loss after max PnL");
        // }
      }
      debug("Successive Loss: " + successiveLoss);
    } else {
      successiveLoss = 0;
    }

    // debug("avgEntryPrice : " + series.getClose());
    // debug("hiLoPrice1History : " + hiLoPrice1History[9]);
    // debug("hiLoPrice2History : " + hiLoPrice2History[9]);
    // debug("hiLoColor1History : " + hiLoColor1History[9]);
    // debug("hiLoColor2History : " + hiLoColor2History[9]);

    // Boolean okLong = hiLoColor1History[0] == GREEN && hiLoColor2History[0] ==
    // GREEN && hiLoPrice1History[0] < series.getClose()
    // && hiLoPrice2History[0] < series.getClose();
    // Boolean okShort = hiLoColor1History[0] == RED && hiLoColor2History[0] == RED
    // && hiLoPrice1History[0] > series.getClose()
    // && hiLoPrice2History[0] > series.getClose();

    // debug("okLong : " + okLong);
    // debug("okShort : " + okShort);

    // getExportedValue(Values.MA_HIGH_LOW_1);
    // debug("mA1 : " + mA1);
    // debug("mA2 : " + mA2);

    // Boolean pnlMax = false;
    // if (pnl > max) {
    // pnlMax = true;
    // }

    // if (successiveLoss < maxSuccessiveLoss && min < pnlSession && pnlSession <
    // max) {
    if (successiveLoss < maxSuccessiveLoss && !pnlMax) {
      // if (position == 0 && signal == Signals.CROSS_HIGH && okLong) {

      startSession = false;
      // pnlSession += lastPnl;

      if (position == 0 && signal == Signals.CROSS_HIGH) {
        debug("isLong : " + isLong() + " / signal : " + signal);
        setEntryState(Enums.EntryState.NONE);
        setLong(true);
        onEnterNow(ctx);
      }
      // if (position == 0 && signal == Signals.CROSS_LOW && okShort) {
      if (position == 0 && signal == Signals.CROSS_LOW) {
        debug("isLong : " + isLong() + " / signal : " + signal);
        setEntryState(Enums.EntryState.NONE);
        setLong(false);
        onEnterNow(ctx);
      }

      // Instrument instr = ctx.getInstrument();

      // float qty = (getSettings().getTradeLots() *
      // instr.getDefaultQuantityAsFloat());

      // qty += Math.abs(position); // Stop and Reverse if there is an open position
      // if (position <= 0 && signal == Signals.CROSS_HIGH) {
      // ctx.buy(qty); // Open Long Position
      // }
      // if (position >= 0 && signal == Signals.CROSS_LOW) {
      // ctx.sell(qty); // Open Short Position
      // }

    } else {
      debug("Stop Pnl Session : " + pnlSession);
      if (successiveLossEnabled) {
        debug("Successive Loss: " + successiveLoss);
      }
      // onDeactivate(ctx);
    }
  }

  public void onSessionStarted(OrderContext ctx, TimeFrame session) {
    debug("onSessionStarted");
    startSession = true;
  }

  @Override
  public void onSessionEnded(OrderContext ctx, TimeFrame session) {
    super.onSessionEnded(ctx, session);
    debug("onSessionEnded");
    successiveLoss = 0;
    pnlMax = false;
    pnlSession = 0.00;
    startSession = false;
  }

  @Override
  public void onDeactivate(OrderContext ctx) {
    super.onDeactivate(ctx);
    createdExitOrders = false;
    clearOrders();
  }

  @Override
  public void onReset(OrderContext ctx) {
    ctx.cancelOrders();
    super.onReset(ctx);
    createdExitOrders = false;
    successiveLoss = 0;
    pnlMax = false;
    pnlSession = 0.00;
    clearOrders();
  }

  @Override
  public void onBarUpdate(OrderContext ctx) {
    // System.out.println("onBarUpdate(): " +
    // com.motivewave.common.Util.formatDateMMMDDHHSS(ServiceInstance.getCurrentTime()));
    if (getEntryState() != Enums.EntryState.OPEN)
      return;
    // Check to see if we have hit the break even state, if so adjust the trail
    // orders.
    Instrument instr = ctx.getInstrument();
    float step = (float) (instr.getTickSize());

    float breakEven = getSettings().getInteger(BREAK_EVEN) * step;
    if (isLong()) {
      if (instr.round(instr.getSellPrice() - ctx.getAvgEntryPrice()) >= instr.round(breakEven)) {
        doBreakEven(ctx);
      }
    } else {
      if (instr.round(ctx.getAvgEntryPrice() - instr.getBuyPrice()) >= instr.round(breakEven)) {
        doBreakEven(ctx);
      }
    }
  }

  @Override
  public void onBarClose(OrderContext ctx) {
    var series = ctx.getDataContext().getDataSeries();
    switch (getEntryState()) {
      case NONE:
        break;
      case PRE_ENTRY:
        if ((isLong() && series.getClose() > series.getOpen()) || (isShort() &&
            series.getClose() < series.getOpen())) {
          info("StrategyRenkoScalping::onBarClose() entering at market: " +
              ctx.getInstrument().getSymbol());
          onEnterNow(ctx);
        }
        break;
      case OPEN:
        moveATROrders(ctx);
        break;
      default:
        break;
    }
  }

  @Override
  public void onOrderFilled(OrderContext ctx, Order order) {

    if (order == entryOrder) {
      info("StrategyRenkoScalping::onOrderFilled() Entry Filled, creating exit orders "
          + ctx.getInstrument().getSymbol());
      setEntryState(Enums.EntryState.OPEN);
      createExitOrders(ctx);
      clearOrder(order);
      return;
    } else if (order == entryOrder1 || order == entryOrder2) {
      // wait for the third order to be filled
      return;
    }

    if (Util.in(order, trailOrder, trailOrder1))
      trailFilled(ctx, order);
    else if (order == firstOrder)
      firstExitFilled(ctx);

    clearOrder(order);
  }

  @Override
  public void onOrderCancelled(OrderContext ctx, Order order) {
    if (isCancelInProgress())
      return;
    if (Util.in(order, entryOrder, entryOrder1, entryOrder2, entryOrder3)) {
      ctx.cancelOrders();
    }
    clearOrder(order);
  }

  @Override
  public void onEnterNow(OrderContext ctx) {
    if (getEntryState() != Enums.EntryState.NONE)
      return;

    setEntryState(Enums.EntryState.WAITING_ENTRY);

    // Special Case: If hedging is enabled we need to create separate position
    // orders for each exit target
    var instr = ctx.getInstrument();
    // if (ctx.supportsHedging() &&
    //     getSettings().getExitPoint(EXIT).isEnabled()) {
    if (ctx.supportsHedging()) {
      var orders = new ArrayList<Order>();
      // var ep = getSettings().getExitPoint(EXIT);
      entryOrder1 = createMktEntry(ctx, isLong(), getSettings().getInteger(TRADE_LOTS) *
          instr.getDefaultQuantityAsFloat());
      orders.add(entryOrder1);
      ctx.submitOrders(orders);
    } else {
      entryOrder = createMktEntry(ctx, isLong(),
          getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat());
      ctx.submitOrders(entryOrder);
    }
  }

  @Override
  public void onPositionClosed(OrderContext ctx) {
    ctx.cancelOrders();
    createdExitOrders = false;
    clearOrders();
    // setState(Enums.StrategyState.INACTIVE);
  }

  private Order createMktEntry(OrderContext ctx, boolean buy, float qty) {
    if (buy)
      return ctx.createMarketOrder(Enums.OrderAction.BUY, qty);
    return ctx.createMarketOrder(Enums.OrderAction.SELL, qty);
  }

  private void clearOrders() {
    firstOrder = secondOrder = thirdOrder = null;
    entryOrder = entryOrder1 = entryOrder2 = entryOrder3 = null;
    clearTrailOrders();
  }

  private void clearTrailOrders() {
    // trailOrder = trailOrder1 = trailOrder2 = trailOrder3 = null;
    trailOrder = trailOrder1 = trailOrder2 = null;
  }

  private void doBreakEven(OrderContext ctx) {
    if (getEntryState() != Enums.EntryState.OPEN)
      return;
    if (!getSettings().getBoolean(BREAK_EVEN_ENABLED))
      return;
    // if (!getSettings().getBoolean(ATR_ENABLED) )
    // return;

    atrActive = getSettings().getBoolean(ATR_ENABLED);
    breakEvenTrail(ctx, trailOrder);
    breakEvenTrail(ctx, trailOrder1);
    breakEvenTrail(ctx, trailOrder2);
    // breakEvenTrail(ctx, trailOrder3);
  }

  private void breakEvenTrail(OrderContext ctx, Order trail) {
    if (trail == null || trail.isFilled() || !trail.exists())
      return;
    var instr = ctx.getInstrument();
    float step = (float) (instr.getTickSize());

    // Double check the we are not already at break even
    float coverOffset = (float) (getSettings().getDouble(COVER) * step);
    float breakEvenPrice = ctx.getAvgEntryPrice();
    if (coverOffset > 0) {
      if (isLong())
        breakEvenPrice += coverOffset;
      else
        breakEvenPrice -= coverOffset;
    }

    boolean buy = trail.isBuy();
    if (buy) {
      float min = instr.round(instr.getAskPrice() + 2.0f * step);
      if (breakEvenPrice < min) {
        warning(
            "StrategyRenkoScalping::breakEvenTrail() too close to ask price setting 2 pts away: " + instr.getSymbol()
                + " breakEven: " + min);
        breakEvenPrice = min;
      }
    } else {
      float max = instr.round(instr.getBidPrice() - 2.0f * step);
      if (breakEvenPrice > max) {
        warning(
            "StrategyRenkoScalping::breakEvenTrail() too close to bid price setting 2 pts away: " + instr.getSymbol()
                + " breakEven: " + max);
        breakEvenPrice = max;
      }
    }

    if ((isLong() && trail.getStopPrice() >= breakEvenPrice) ||
        (!isLong() && trail.getStopPrice() <= breakEvenPrice))
      return;

    trail.setAdjStopPrice(breakEvenPrice);
    ctx.submitOrders(trail);
  }

  private void createExitOrders(OrderContext ctx) {
    if (createdExitOrders) {
      warning("StrategyRenkoScalping::createExitOrders() orders already created!");
      return;
    }
    var instr = ctx.getInstrument();
    float step = (float) (instr.getTickSize());

    createdExitOrders = true;

    // Calculate the stop price
    float offset = (float) (getSettings().getDouble(MAX_LOSS) * step);
    float basePrice = ctx.getAvgEntryPrice();
    if (isLong())
      basePrice -= offset;
    else
      basePrice += offset;

    // Make sure this is at least 1 pip away from the current bid/ask
    if (isLong()) {
      float maxPrice = instr.round(instr.getSellPrice() - step);
      if (basePrice > maxPrice)
        basePrice = maxPrice;
    } else {
      float minPrice = instr.round(instr.getBuyPrice() + step);
      if (basePrice < minPrice)
        basePrice = minPrice;
    }

    var action = isLong() ? Enums.OrderAction.SELL : Enums.OrderAction.BUY;

    var orders = new ArrayList<Order>();
    var tif = getSettings().getTIF(TIME_IN_FORCE);
    var entry = entryOrder;

    // Create Target Orders
    // var ep = getSettings().getExitPoint(EXIT);
    float qty = getSettings().getInteger(TRADE_LOTS) * instr.getDefaultQuantityAsFloat();
    float firstExit = 0;
    if (isLong())
      firstExit = instr.round(ctx.getAvgEntryPrice() + getSettings().getInteger(EXIT) * step);
    else
      firstExit = instr.round(ctx.getAvgEntryPrice() - getSettings().getInteger(EXIT) * step);

    Object ref = entryOrder1 != null ? entryOrder1.getReferenceID() : null;
    firstOrder = ctx.createLimitOrder(instr, ref, action, tif, qty, firstExit);

    orders.add(firstOrder);

    // Create the stop order(s)
    atrActive = false;
    if (entry != null) {
      // Non-hedged, just create one order
      qty = getSettings().getInteger(TRADE_LOTS) *
          instr.getDefaultQuantityAsFloat();
      trailOrder = ctx.createStopOrder(instr, action, tif, qty, basePrice);
      orders.add(trailOrder);
    } else {
      qty = getSettings().getInteger(TRADE_LOTS) *
          instr.getDefaultQuantityAsFloat();
      trailOrder1 = ctx.createStopOrder(instr, entryOrder1.getReferenceID(),
          action, tif, qty, basePrice);
      orders.add(trailOrder1);

    }

    // To make this faster, submit these orders together
    ctx.submitOrders(orders);
  }

  private void moveATROrders(OrderContext ctx) {
    if (!atrActive) {
      return;
    }

    if (!isValidOrder(trailOrder) && !isValidOrder(trailOrder1) &&
        !isValidOrder(trailOrder2))
      return;

    // Calculate the ATR offset
    var dc = ctx.getDataContext();
    var series = dc.getDataSeries();
    var instr = dc.getInstrument();
    int latest = series.size() - 1;
    int atrPeriod = getSettings().getInteger(ATR_PERIOD);
    double mult = getSettings().getDouble(ATR_MULTIPLIER);
    Double atr = series.atr(latest - 1, atrPeriod);
    if (atr == null)
      return;
    float offset = (float) instr.round(atr * mult);
    float step = (float) (instr.getTickSize());

    boolean buy = true;
    if (isValidOrder(trailOrder))
      buy = trailOrder.isBuy();
    if (isValidOrder(trailOrder1))
      buy = trailOrder1.isBuy();

    float lastPrice = series.getClose(latest - 1);
    float stop = 0;

    if (buy) {
      stop = instr.round(lastPrice + offset);
      float min = instr.round(instr.getAskPrice() + 2.0f * step);
      if (stop < min)
        stop = min;
    } else {
      stop = instr.round(lastPrice - offset);
      float max = instr.round(instr.getBidPrice() - 2.0f * step);
      if (stop > max)
        stop = max;
    }

    double stopPrice = 0;
    if (isValidOrder(trailOrder))
      stopPrice = trailOrder.getStopPrice();
    else if (isValidOrder(trailOrder1))
      stopPrice = trailOrder1.getStopPrice();
    else if (isValidOrder(trailOrder2))
      stopPrice = trailOrder2.getStopPrice();

    if ((buy && stop >= stopPrice) || (!buy && stop <= stopPrice)) {
      // No change in price
      return;
    }

    if (isValidOrder(trailOrder)) {
      trailOrder.setAdjStopPrice(stop);
      ctx.submitOrders(trailOrder);
    } else {
      var orders = new ArrayList<Order>();
      if (isValidOrder(trailOrder1)) {
        trailOrder1.setAdjStopPrice(stop);
        orders.add(trailOrder1);
      }
      if (isValidOrder(trailOrder2)) {
        trailOrder2.setAdjStopPrice(stop);
        orders.add(trailOrder2);
      }
      ctx.submitOrders(orders);
    }
  }

  private void trailFilled(OrderContext ctx, Order order) {
    var cancelList = new ArrayList<Order>();
    if (isValidOrder(firstOrder) && Util.isEmpty(firstOrder.getReferenceID()))
      cancelList.add(firstOrder);
    if (isValidOrder(secondOrder) && Util.isEmpty(secondOrder.getReferenceID()))
      cancelList.add(secondOrder);
    if (isValidOrder(thirdOrder) && Util.isEmpty(thirdOrder.getReferenceID()))
      cancelList.add(thirdOrder);
    firstOrder = secondOrder = thirdOrder = null;
    ctx.cancelOrders(cancelList);
  }

  private void firstExitFilled(OrderContext ctx) {
    try {
      if (trailOrder != null) {
        float qty = firstOrder.getFilledAsFloat();
        if (qty == 0) {
          // Grab the value from the config
          warning("StrategyRenkoScalping::firstExitFilled() filled qty is 0!");
          qty = getSettings().getInteger(TRADE_LOTS) *
              ctx.getInstrument().getDefaultQuantityAsFloat();
        }
        if (qty >= trailOrder.getQuantityAsFloat()) {
          var cancelList = new ArrayList<Order>();
          if (Util.isEmpty(trailOrder.getReferenceID()))
            cancelList.add(trailOrder);
          if (secondOrder != null)
            cancelList.add(secondOrder);
          if (thirdOrder != null)
            cancelList.add(thirdOrder);
          ctx.cancelOrders(cancelList);
        } else {
          trailOrder.setAdjQuantity(trailOrder.getQuantityAsFloat() - qty);
          ctx.submitOrders(trailOrder);
        }
      } else if (trailOrder1 != null) {
        if (Util.isEmpty(trailOrder1.getReferenceID()))
          ctx.cancelOrders(trailOrder1);
      }
    } finally {
      firstOrder = null;
    }
  }

  private void clearOrder(Order order) {
    if (order == entryOrder)
      entryOrder = null;
    if (order == entryOrder1)
      entryOrder1 = null;
    if (order == entryOrder2)
      entryOrder2 = null;
    if (order == entryOrder3)
      entryOrder3 = null;
    if (order == firstOrder)
      firstOrder = null;
    if (order == secondOrder)
      secondOrder = null;
    if (order == thirdOrder)
      thirdOrder = null;
    if (order == trailOrder)
      trailOrder = null;
    if (order == trailOrder1)
      trailOrder1 = null;
    if (order == trailOrder2)
      trailOrder2 = null;
  }

  private boolean isValidOrder(Order order) {
    if (order == null)
      return false;
    if (!order.exists())
      return false;
    if (order.isCancelled() || order.isFilled())
      return false;
    return true;
  }

  private Order entryOrder, entryOrder1, entryOrder2, entryOrder3;

  private Order trailOrder, trailOrder1, trailOrder2;
  private boolean atrActive = false;

  private Order firstOrder, secondOrder, thirdOrder;
  private boolean createdExitOrders = false;

}
