package com.model;

import com.core.YellowFever;

import sim.util.Bag;

public class Building {

  private static final int MAXIMUM_OCCUPANCY = 1000;
  private int type;
  private int xLocation;
  private int yLocation;
  private double water;
  private double elevation;
  private Facility facility;

  private Bag humansHH; // camp location for household
  private Bag humans; // who are on the field right now

  public Building() {
    super();
    this.humansHH = new Bag();
    this.humans = new Bag();
  }

  public Building(int x, int y) {
    this.setX(x);
    this.setY(y);
  }

  /* calaculate distance */
  public double distanceTo(Building building) {
    return Math.sqrt(Math.pow(building.getX() - this.getX(), 2) + Math.pow(building.getY() - this.getY(), 2));
  }

  public boolean isCampOccupied(YellowFever yellowFever) {
    if (this.humansHH.size() >= MAXIMUM_OCCUPANCY) {
      return true;
    } else {
      return false;
    }
  }

  public void addHuman(Human human) {
    this.humans.add(human);
  }

  public void addHumanHH(Family family) {
    this.humansHH.add(family);
  }

  public int getX() {
    return this.xLocation;
  }

  public int setX(int x) {
    return this.xLocation = x;
  }

  public int getY() {
    return this.yLocation;
  }

  public int setY(int y) {
    return this.yLocation = y;
  }

  public int getType() {
    return this.type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public double getWater() {
    return water;
  }

  public void setWater(double water) {
    this.water = water;
  }

  public double getElevation() {
    return elevation;
  }

  public void setElevation(double elevation) {
    this.elevation = elevation;
  }

}
