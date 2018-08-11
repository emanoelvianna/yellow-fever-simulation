package com.main;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;

public class YellowFever extends SimState {

  private static final int WIDTH = 300;
  private static final int HEIGHT = 300;
  private static final long serialVersionUID = 8374233360244345303L;

  private GeomVectorField buildingsShape;
  private GeomVectorField streetsShape;

  public YellowFever(long seed) {
    super(seed);
    this.buildingsShape = new GeomVectorField(WIDTH, HEIGHT);
    this.streetsShape = new GeomVectorField(WIDTH, HEIGHT);
  }

  @Override
  public void start() {
    Region.create(this);
  }

  public static void main(String[] args) {
    doLoop(YellowFever.class, args);
    System.exit(0);
  }

  public static int getWidth() {
    return WIDTH;
  }

  public static int getHeight() {
    return HEIGHT;
  }

  public GeomVectorField getBuildingsShape() {
    return buildingsShape;
  }

  public GeomVectorField getStreetsShape() {
    return streetsShape;
  }

}
