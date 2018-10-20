package com.model;

import com.core.YellowFever;
import com.core.algorithms.TimeManager;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Facility implements Steppable, Valuable {

  private static final long serialVersionUID = 1L;
  public static final int ORDERING = 1; // schedule after rainfall
  private YellowFever yellowFever;
  private Building location; // location of the facility
  private TimeManager time;
  private int facilityID; // id
  private int currentStep;
  private int currentDay;

  public Facility() {
    this.time = new TimeManager();
    this.currentStep = 0;
    this.currentDay = 0;
  }

  public void step(SimState state) {
    this.yellowFever = (YellowFever) state;
    this.currentStep = (int) yellowFever.schedule.getSteps();
    if (this.isNewDay()) {
      this.yellowFever.setMaximumCapacity(false);
    }
  }

  private boolean isNewDay() {
    if (this.time.dayCount(currentStep) > this.currentDay) {
      this.currentDay = this.time.dayCount(currentStep);
      return true;
    } else {
      return false;
    }
  }

  // used to the statistics
  public boolean isReachedCapacity(Building building, YellowFever yellowFever) {
    if (building.getPatientCounter() >= yellowFever.getParams().getGlobal().getHeaalthFacilityCapacity()) {
      this.yellowFever.setMaximumCapacity(true);
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
