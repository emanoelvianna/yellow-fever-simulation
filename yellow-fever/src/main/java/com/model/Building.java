package com.model;

public class Building {

  private int type;
  private int xLocation;
  private int yLocation;
  private double water;
  private Facility facility;

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

}
