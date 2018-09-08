package com.model;

import java.io.Serializable;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Mosquito implements Steppable, Valuable, Serializable {

  private double speed;
  private boolean activity;
  private boolean hungry;
  private int sensoryAmplitude;

  public Mosquito() {
    this.speed = 1.0;
    this.activity = false;
    this.hungry = false;
    this.sensoryAmplitude = 3;
  }

  public void toBite(Refugee refugee) {
    // TODO: Com probabilidade x infecção agente humano
    refugee.infected();
  }

  public void ThereIsFood() {

  }

  public void step(SimState arg0) {

  }

  public double doubleValue() {
    return 0;
  }

  public double getSpeed() {
    return speed;
  }

  public void setSpeed(double speed) {
    this.speed = speed;
  }

  public boolean isActivity() {
    return activity;
  }

  public void setActivity(boolean activity) {
    this.activity = activity;
  }

  public boolean isHungry() {
    return hungry;
  }

  public void setHungry(boolean hungry) {
    this.hungry = hungry;
  }

  public int getSensoryAmplitude() {
    return sensoryAmplitude;
  }

  public void setSensoryAmplitude(int sensoryAmplitude) {
    this.sensoryAmplitude = sensoryAmplitude;
  }

}
