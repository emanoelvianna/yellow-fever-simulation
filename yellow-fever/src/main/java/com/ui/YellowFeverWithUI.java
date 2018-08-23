package com.ui;

import java.net.MalformedURLException;

import com.core.YellowFever;

import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;

public class YellowFeverWithUI extends GUIState {

  public YellowFeverWithUI() throws MalformedURLException {
    super(new YellowFever(System.currentTimeMillis()));
  }

  public YellowFeverWithUI(SimState state) {
    super(state);
  }

  @Override
  public void init(Controller controller) {
    super.init(controller);
  }

  @Override
  public Object getSimulationInspectedObject() {
    return state;
  }

  @Override
  public void start() {
    super.start();
    YellowFever yellowFever = (YellowFever) state;
  }

  public static void main(String[] args) throws MalformedURLException {
    YellowFeverWithUI gui = new YellowFeverWithUI();
    Console console = new Console(gui);
    console.setVisible(true);
  }

}
