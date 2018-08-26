package com.model;

import com.core.TimeManager;
import com.core.YellowFever;
import com.model.enumeration.HealthStatus;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

public class Human implements Steppable {
  private static final long serialVersionUID = 1L;
  private static final int ORDERING = 2;
  private Stoppable stopper;
  private int age;
  private int sex;
  private HealthStatus healthStatus;
  /* monitors health status of agent - capture the change in each day */
  private int prevHealthStatus;
  private Building home;
  private Building goal; /* location of the goal */
  private Building position; // current position
  private Family family;
  private double jitterX;
  private double jitterY;
  private TimeManager time;// time contorler-identify the hour, day, week
  private MersenneTwisterFast random;

  public Human(int age, int sex, Family hh, Building home, Building position, MersenneTwisterFast random,
      Continuous2D allHumans) {
    this.setAge(age);
    this.setSex(sex);
    this.setFamily(hh);
    this.setHome(home);
    this.setGoal(home);
    this.setJitterX(random.nextDouble());
    this.setJitterY(random.nextDouble());
    this.setPosition(position);
    // TODO: Quais outros atribuitos iram existir?
    // this.setLaterineUse(0);
    this.setPrevHealthStatus(1);
    this.setTime(new TimeManager());
    // TODO: Quais outros atribuitos iram existir?
    // latUse = 1;
    // d = null;
    // cStep = 0;
    // infectionPerdiod = 0;
    // recoveryPeriod = 0;
    // minuteInDay = 0;
    this.setRandom(random);
    allHumans.setObjectLocation(this,
        new Double2D(hh.getLocation().getX() + getJitterX(), hh.getLocation().getY() + getJitterY()));
  }

  public Building getGoal() {
    return this.goal;
  }

  public void setGoal(Building position) {
    this.goal = position;
  }

  public void step(SimState state) {
    YellowFever yellowFever = (YellowFever) state;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public int getSex() {
    return sex;
  }

  public void setSex(int sex) {
    this.sex = sex;
  }

  public Building getHome() {
    return home;
  }

  public void setHome(Building home) {
    this.home = home;
  }

  public HealthStatus getHealthStatus() {
    return healthStatus;
  }

  public void setHealthStatus(HealthStatus healthStatus) {
    this.healthStatus = healthStatus;
  }

  public Family getFamily() {
    return family;
  }

  public void setFamily(Family family) {
    this.family = family;
  }

  public double getJitterX() {
    return jitterX;
  }

  public void setJitterX(double jitterX) {
    this.jitterX = jitterX;
  }

  public double getJitterY() {
    return jitterY;
  }

  public void setJitterY(double jitterY) {
    this.jitterY = jitterY;
  }

  public Building getPosition() {
    return position;
  }

  public void setPosition(Building position) {
    this.position = position;
  }

  public TimeManager getTime() {
    return time;
  }

  public void setTime(TimeManager time) {
    this.time = time;
  }

  public int getPrevHealthStatus() {
    return prevHealthStatus;
  }

  public void setPrevHealthStatus(int prevHealthStatus) {
    this.prevHealthStatus = prevHealthStatus;
  }

  public MersenneTwisterFast getRandom() {
    return random;
  }

  public void setRandom(MersenneTwisterFast random) {
    this.random = random;
  }

  public static int getOrdering() {
    return ORDERING;
  }

  public Stoppable getStopper() {
    return stopper;
  }

  public void setStopper(Stoppable stopper) {
    this.stopper = stopper;
  }

}
