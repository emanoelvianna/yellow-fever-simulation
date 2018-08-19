package com.model;

import com.core.YellowFever;
import com.model.enumeration.Activity;
import com.model.enumeration.HealthStatus;
import com.model.enumeration.Sex;

import sim.engine.SimState;
import sim.engine.Steppable;

public class Human implements Steppable {
  private static final long serialVersionUID = 1L;
  private YellowFever yellowFever;
  private int age;
  private Sex sex;
  private Building home;
  private HealthStatus healthStatus;

  public Human(YellowFever yellowFeve, Building home, HealthStatus healthStatus) {
    this.yellowFever = yellowFeve;
    this.home = home;
    this.healthStatus = healthStatus;
    this.defineAge();
    this.defineSex();
  }

  public void step(SimState state) {
    YellowFever yellowFever = (YellowFever) state;
  }

  public Activity doActivity(Activity activity, Building building) {
    if (this.age < 18) {
      return Activity.SCHOOL;
    } else if (this.age > 18) {
      return Activity.WORK;
    } else {
      // TODO: desenvolver criterio para final de semana
      return Activity.VISIT_SOCIAL;
    }
  }

  private void defineAge() {
    this.age = 10 + this.yellowFever.random.nextInt(55);
  }

  private void defineSex() {
    if (this.yellowFever.random.nextDouble() > 0.5) {
      this.sex = Sex.M;
    } else {
      this.sex = Sex.F;
    }
  }

  public int getAge() {
    return age;
  }

  public Sex getSex() {
    return sex;
  }

  public Building getHome() {
    return home;
  }

  public HealthStatus getHealthStatus() {
    return healthStatus;
  }
}
