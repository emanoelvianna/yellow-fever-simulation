package com.core;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.util.geo.GeomPlanarGraph;

public class YellowFever extends SimState {

  private static final int NUMBERS_OF_AGENTS = 1000;
  private static final int WIDTH = 700;
  private static final int HEIGHT = 600;
  private static final long serialVersionUID = 8374233360244345303L;

  private GeomVectorField buildingsShape;
  private GeomVectorField streetsShape;
  private GeomVectorField agents;
  private GeomPlanarGraph network;
  private GeomVectorField junctions;

  public YellowFever(long seed) {
    super(seed);
    this.buildingsShape = new GeomVectorField(WIDTH, HEIGHT);
    this.streetsShape = new GeomVectorField(WIDTH, HEIGHT);
    this.agents = new GeomVectorField(WIDTH, HEIGHT);
    this.network = new GeomPlanarGraph();
    this.junctions = new GeomVectorField(WIDTH, HEIGHT);
  }

  public static void main(String[] args) {
    doLoop(YellowFever.class, args);
    System.exit(0);
  }

  @Override
  public void start() {
    new BuildRegion().create(this);
    super.start();
    this.agents.clear();
    System.out.println("[INFO] Adding agents");
    addAgents();
    System.out.println("[INFO] Agents added");
    this.agents.setMBR(this.buildingsShape.getMBR());
  }

  public void addAgents() {
    // TODO:
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

  public GeomPlanarGraph getNetwork() {
    return network;
  }

  public GeomVectorField getJunctions() {
    return junctions;
  }

  public GeomVectorField getAgents() {
    return agents;
  }

}
