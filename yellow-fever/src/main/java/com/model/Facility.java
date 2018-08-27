package com.model;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Facility implements Steppable {

  private static final long serialVersionUID = 1L;
  private static final int ORDERING = 1; // schedule after rainfall
  private int type;
  private Building location;

  public void step(SimState arg0) {
    // TODO:
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Building getLocation() {
    return location;
  }

  public void setLocation(Building location) {
    this.location = location;
  }

  public static int getOrdering() {
    return ORDERING;
  }
}
