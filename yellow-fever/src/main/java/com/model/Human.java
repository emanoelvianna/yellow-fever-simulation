package com.model;

import com.core.YellowFever;
import com.model.enumeration.HealthStatus;
import com.model.enumeration.Sex;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Human implements Steppable {
  private static final long serialVersionUID = 1L;
  private int age;
  private Sex sex;
  private Residence home;
  private HealthStatus healthStatus;
  private Family family;

  public Human(int age, Sex sex, Residence home, HealthStatus healthStatus, Family family) {
    this.age = age;
    this.sex = sex;
    this.home = home;
    this.healthStatus = healthStatus;
    this.family = family;
  }

  public Human(YellowFever yellowFever) {

  }

  public void step(SimState state) {
    YellowFever yellowFever = (YellowFever) state;
  }
}
