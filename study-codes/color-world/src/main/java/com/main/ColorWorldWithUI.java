package com.main;

import java.awt.Color;
import java.net.MalformedURLException;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.gui.SimpleColorMap;

/**
 * The display GUI for the ColorWorld GeoMASON example. Much of this file is
 * similar to other MASON GUI code. The only exception is that we use our custom
 * ColorWorldPortrayal for the voting districts to handle the shading.
 *
 */
public class ColorWorldWithUI extends GUIState {
  Display2D display;
  JFrame displayFrame;

  // our data is vector format, not raster.
  GeomVectorFieldPortrayal countyPortrayal = new GeomVectorFieldPortrayal();
  GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();

  public ColorWorldWithUI(SimState state) {
    super(state);
  }

  public ColorWorldWithUI() throws MalformedURLException {
    super(new ColorWorld(System.currentTimeMillis()));
  }

  public static String getName() {
    return "GeoMASON: Color World";
  }

  @Override
  public Object getSimulationInspectedObject() {
    return state;
  }

  @Override
  public void init(Controller controller) {
    super.init(controller);

    display = new Display2D(ColorWorld.WIDTH, ColorWorld.HEIGHT, this);

    display.attach(countyPortrayal, "FFX County Politcal Boundaries");
    display.attach(agentPortrayal, "Agents");

    displayFrame = display.createFrame();
    controller.registerFrame(displayFrame);
    displayFrame.setVisible(true);
  }

  @Override
  public void quit() {
    super.quit();

    if (displayFrame != null) {
      displayFrame.dispose();
    }

    displayFrame = null;
    display = null;
  }

  @Override
  public void start() {
    super.start();
    setupPortrayals();
  }

  private void setupPortrayals() {
    ColorWorld world = (ColorWorld) state;

    agentPortrayal.setField(ColorWorld.agents);
    agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.RED, 6.0));

    // the county portrayal (ie, the voting districts) to use our custom portrayal
    countyPortrayal.setField(world.county);
    countyPortrayal.setPortrayalForAll(new ColorWorldPortrayal(new SimpleColorMap(0.0, ColorWorld.NUM_AGENTS, Color.WHITE, Color.BLUE)));

    display.reset();
    display.setBackdrop(Color.WHITE);
    display.repaint();
  }

  public static void main(String[] args) throws MalformedURLException {
    ColorWorldWithUI worldGUI = new ColorWorldWithUI();
    Console console = new Console(worldGUI);
    console.setVisible(true);
  }

}
