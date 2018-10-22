package com.model;

import com.core.YellowFever;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Facility implements Steppable, Valuable {

  private static final long serialVersionUID = 1L;
  public static final int ORDERING = 1; // schedule after rainfall
  private Building location; // location of the facility
  private int facilityID; // id

  public void step(SimState state) {

  }

  // used to the statistics
  public boolean isReachedCapacity(Building building, YellowFever yellowFever) {
    if (building.getPatientCounter() >= yellowFever.getParams().getGlobal().getHeaalthFacilityCapacity()) {
      yellowFever.setMaximumCapacityInDay(true);
      return true;
    }
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
