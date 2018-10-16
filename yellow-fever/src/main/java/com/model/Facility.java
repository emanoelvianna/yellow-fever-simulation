package com.model;

import com.core.YellowFever;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Facility implements Steppable, Valuable {

  private static final long serialVersionUID = 1L;
  public static final int ORDERING = 1; // schedule after rainfall
  private int facilityID; // id
  private Building location; // location of the facility

  public void step(SimState state) {

  }

  public boolean isReachedCapacity(Building building, YellowFever yellowFever) {
    // TODO: voltar ao valor do paramtro!
    if (building.getPatientCounter() >= 1) {
      return true;
    } else
      return false;
  }

  public void setFacilityID(int id) {
    this.facilityID = id;
  }

  public int getFacilityID() {
    return facilityID;
  }

  public void setLocation(Building location) {
    this.location = location;
  }

  public Building getLocation() {
    return location;
  }

  public double doubleValue() {
    return this.getFacilityID();
  }

}
