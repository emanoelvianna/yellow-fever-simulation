package com.ui;

import java.awt.Color;
import java.net.MalformedURLException;

import javax.swing.JFrame;

import com.main.YellowFever;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;

public class YellowFeverWithUI extends GUIState {
  private Display2D display;
  private JFrame displayFrame;

  private GeomVectorFieldPortrayal buildingsPortrayal = new GeomVectorFieldPortrayal();
  private GeomVectorFieldPortrayal streetsPortrayal = new GeomVectorFieldPortrayal();

  public YellowFeverWithUI() throws MalformedURLException {
    super(new YellowFever(System.currentTimeMillis()));
  }

  public YellowFeverWithUI(SimState state) {
    super(state);
  }

  @Override
  public void init(Controller controller) {
    super.init(controller);

    display = new Display2D(YellowFever.getWidth(), YellowFever.getHeight(), this);

    display.attach(buildingsPortrayal, "Buildings", true);
    display.attach(streetsPortrayal, "Streets", true);

    displayFrame = display.createFrame();
    controller.registerFrame(displayFrame);
    displayFrame.setVisible(true);
  }

  @Override
  public Object getSimulationInspectedObject() {
    return state;
  }

  @Override
  public void start() {
    super.start();
    YellowFever yellowFever = (YellowFever) state;

    streetsPortrayal.setField(yellowFever.getBuildingsShape());
    streetsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.CYAN, true));

    buildingsPortrayal.setField(yellowFever.getStreetsShape());
    buildingsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY, true));

    display.reset();
    display.setBackdrop(Color.WHITE);

    display.repaint();
  }

  public static void main(String[] args) throws MalformedURLException {
    YellowFeverWithUI gui = new YellowFeverWithUI();
    Console console = new Console(gui);
    console.setVisible(true);
  }

}
