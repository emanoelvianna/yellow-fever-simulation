package com.model;

import com.core.YellowFever;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Facility implements Steppable, Valuable {

  private int capacity;
  private int facilityID; // id
  private Building location; // location of the facility
  public static final int ORDERING = 1; // schedule after rainfall

  public Facility() {
    this.capacity = 0;
  }

  public void step(SimState state) {
    YellowFever dadaab = (YellowFever) state;
    if (dadaab.isNewDay()) {
      this.countResources(dadaab);
    }
  }

  public boolean isReachedCapacity(Building f, YellowFever d) {
    if (f.getPatientCounter() >= d.getParams().getGlobal().getHeaalthFacilityCapacity()) {
      return true;
    } else
      return false;
  }

  public void countResources(YellowFever d) {
    for (Object obj : d.getHealthCenters()) {
      Building building = (Building) obj;
      building.setPatientCounter(building.getPatientCounter() - this.capacity);
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

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

}
