package com.model;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Egg implements Steppable, Valuable {

  private static final long serialVersionUID = 1L;
  private Building currentPosition;
  private double timeOfMaturation;
  private int amount;

  public Egg(Building currentPosition, double timeOfMaturation, int amount) {
    this.currentPosition = currentPosition;
    this.timeOfMaturation = timeOfMaturation;
    this.amount = amount;
  }

  public void step(SimState state) {

  }

  public Building getCurrentPosition() {
    return currentPosition;
  }

  public void setCurrentPosition(Building currentPosition) {
    this.currentPosition = currentPosition;
  }

  public double getTimeOfMaturation() {
    return timeOfMaturation;
  }

  public void setTimeOfMaturation(double timeOfMaturation) {
    this.timeOfMaturation = timeOfMaturation;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public double doubleValue() {
    return 0;
  }
}
