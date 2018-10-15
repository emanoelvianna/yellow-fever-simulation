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
    YellowFever yellowFever = (YellowFever) state;
    this.resetPatientNumber(yellowFever);
  }

  // TODO: Garantir que está funcionando!
  public boolean isReachedCapacity(Building building, YellowFever yellowFever) {
    if (building.getPatientCounter() >= yellowFever.getParams().getGlobal().getHeaalthFacilityCapacity()) {
      return true;
    } else
      return false;
  }

  public void resetPatientNumber(YellowFever yellowFever) {
    for (Object object : yellowFever.getHealthCenters()) {
      Building building = (Building) object;
      int amount = building.getPatientCounter();
      int capacity = yellowFever.getParams().getGlobal().getHeaalthFacilityCapacity();
      building.setPatientCounter(capacity - amount);
    }
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
