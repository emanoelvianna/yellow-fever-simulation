package com.ui;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import com.core.YellowFever;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Valuable;
import sim.util.geo.MasonGeometry;

public class YellowFeverWithUI extends GUIState {

  private static double RAINFALL_NMM = 4.5;

  private Display2D display;
  private JFrame displayFrame;
  private Display2D displayRainfall;
  private JFrame displayFrameRainfall;
  private FastValueGridPortrayal2D rainfallPortrayal = new FastValueGridPortrayal2D();
  private ContinuousPortrayal2D humanPortrayal = new ContinuousPortrayal2D();
  private SparseGridPortrayal2D facilPortrayal = new SparseGridPortrayal2D();
  private GeomVectorFieldPortrayal roadShapeProtrayal = new GeomVectorFieldPortrayal();
  private GeomVectorFieldPortrayal regionShapeProtrayal = new GeomVectorFieldPortrayal();

  public static void main(String[] args) {

    YellowFeverWithUI dadaabGUI = new YellowFeverWithUI();
    Console console = new Console(dadaabGUI);
    console.setVisible(true);
  }

  public YellowFeverWithUI() {
    super(new YellowFever(System.currentTimeMillis()));
  }

  public YellowFeverWithUI(SimState state) {
    super(state);
  }

  @Override
  public void start() {
    super.start();
    /* set up our portrayals */
    this.setupPortrayals();
  }

  @Override
  public void load(SimState state) {
    super.load(state);
    this.setupPortrayals();
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

  private void setupPortrayals() {
    YellowFever yellowFever = (YellowFever) state;

    rainfallPortrayal.setField(yellowFever.getRainfallGrid());
    double rng = (RAINFALL_NMM * 90 * 90 * 3);
    rainfallPortrayal.setMap(new sim.util.gui.SimpleColorMap(0, rng, Color.WHITE, Color.BLUE));

    humanPortrayal.setField(yellowFever.getAllHumans());

    /* to draw each human type with different color */
    OvalPortrayal2D hPortrayal = new OvalPortrayal2D(0.20) {
      final Color healthy = new Color(0, 128, 0);
      final Color exposed = new Color(0, 0, 255); // 0-0-255 255-255-0 184-134-11
      final Color infected = new Color(255, 0, 0);
      final Color recovered = new Color(102, 0, 102);

      public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (object != null) {
          double cType = ((Valuable) object).doubleValue();

          if (cType == 1) {
            paint = healthy;
          } else if (cType == 2) {
            paint = exposed;
          } else if (cType == 3) {
            paint = infected;
          } else {
            paint = recovered;
          }

          super.draw(object, graphics, info);
        } else {
          super.draw(object, graphics, info);
        }
      }
    };

    humanPortrayal.setPortrayalForAll(hPortrayal);
    facilPortrayal.setField(yellowFever.getFacilityGrid());

    /* to draw each facility type with different color */
    RectanglePortrayal2D facPortrayal = new RectanglePortrayal2D(1.0, false) {
      final Color borehole = new Color(0, 128, 255);
      final Color healthC = new Color(255, 0, 0);
      final Color school = new Color(0, 255, 0);
      final Color foodC = new Color(102, 0, 102);
      final Color mosq = new Color(0, 0, 102);
      final Color market = new Color(0, 102, 102);
      final Color other = new Color(255, 255, 255);

      public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (object != null) {
          double cType = ((Valuable) object).doubleValue();
          if (cType == 1) {
            paint = school;
          } else if (cType == 2) {
            paint = borehole;
          } else if (cType == 3) {
            paint = mosq;
          } else if (cType == 4) {
            paint = market;
          } else if (cType == 5) {
            paint = foodC;
          } else if (cType == 6) {
            paint = healthC;
          } else {
            paint = other;
          }
          super.draw(object, graphics, info);
        } else {
          super.draw(object, graphics, info);
        }
      }
    };

    facilPortrayal.setPortrayalForAll(facPortrayal);
    regionShapeProtrayal.setField(yellowFever.getRegionShape());

    GeomPortrayal gp = new GeomPortrayal(true) {
      final Color d = new Color(224, 255, 224);
      final Color i = new Color(255, 180, 210);
      final Color h = new Color(204, 204, 153);
      final Color o = new Color(255, 255, 255);

      public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (object != null) {
          MasonGeometry mg = (MasonGeometry) object;
          Integer cType = mg.getIntegerAttribute("CAMPID");
          if (cType == 1) {
            paint = d;
          } else if (cType == 2) {
            paint = i;
          } else if (cType == 3) {
            paint = h;
          } else {
            paint = o;
          }
          super.draw(object, graphics, info);
        } else {
          super.draw(object, graphics, info);
        }
      }

    };

    regionShapeProtrayal.setPortrayalForAll(gp);
    roadShapeProtrayal.setField(yellowFever.getRegionShape());
    roadShapeProtrayal.setPortrayalForAll(new GeomPortrayal(Color.LIGHT_GRAY, false));

    display.reset();
    display.setBackdrop(Color.white);
    display.repaint();

    displayRainfall.reset();
    displayRainfall.setBackdrop(Color.white);
    displayRainfall.repaint();
  }

  @Override
  public void init(Controller c) {
    super.init(c);

    display = new Display2D(380, 760, this);

    displayRainfall = new Display2D(380, 760, this);

    display.attach(regionShapeProtrayal, "Region Vector");
    display.attach(roadShapeProtrayal, "Road Vector");
    display.attach(humanPortrayal, "Humans");
    display.attach(facilPortrayal, "Facility");
    
    displayFrame = display.createFrame();
    c.registerFrame(displayFrame);
    displayFrame.setVisible(true);
    
    displayRainfall.attach(rainfallPortrayal, "Rainfall");
    
    displayFrameRainfall = displayRainfall.createFrame();
    c.registerFrame(displayFrameRainfall);
    displayFrameRainfall.setVisible(false);
    displayFrameRainfall.setTitle("Rainfall");
  }
}
