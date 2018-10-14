package com.model;

import com.core.YellowFever;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Facility implements Steppable, Valuable {

  private static final long serialVersionUID = 1L;
  private int capacity;
  private int facilityID; // id
  private Building location; // location of the facility
  public static final int ORDERING = 1; // schedule after rainfall

  public Facility(int capacity) {
    this.capacity = capacity;
  }

  public void step(SimState state) {
    YellowFever yellowFever = (YellowFever) state;
    this.countResources(yellowFever);
  }

  public synchronized boolean isReachedCapacity(Building building, YellowFever yellowFever) {
    if (building.getPatientCounter() >= yellowFever.getParams().getGlobal().getHeaalthFacilityCapacity()) {
      return true;
    } else
      return false;
  }

  public void countResources(YellowFever yellowFever) {
    synchronized (yellowFever.getHealthCenters()) {
      for (Object obj : yellowFever.getHealthCenters()) {
        Building building = (Building) obj;
        building.setPatientCounter(this.capacity - building.getPatientCounter());
      }
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
