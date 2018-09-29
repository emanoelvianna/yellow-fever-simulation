package com.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.core.Dadaab;
import com.model.enumeration.HealthStatus;

import sim.util.Bag;
import sim.util.Valuable;

public class Building implements Valuable, Serializable {

  private int fieldID; // identify the type pf the field
  private int campID; // holds id of the three camps
  private double water; // hold water amount
  private boolean sap;
  private boolean nectar;
  private double elevation; // elevation
  private double vibrioCholerae = 0; // contamination level
  private double timeOfMaturation;
  private int patientCounter = 0;
  private Facility facility;
  private int locationX;
  private int locationY;
  private Bag refugeeHH; // camp location for household
  private Bag humans; // who are on the field right now
  private Bag mosquitoes;
  private int eggs;

  // getter and setter
  public Building() {
    super();
    this.refugeeHH = new Bag();
    this.humans = new Bag();
    this.mosquitoes = new Bag();
    this.eggs = 0;
    this.timeOfMaturation = 0;
  }

  public Building(int x, int y) {
    this.humans = new Bag();
    this.mosquitoes = new Bag();
    this.refugeeHH = new Bag();
    this.eggs = 0;
    this.timeOfMaturation = 0;
    this.locationX = x;
    this.locationY = y;
  }

  public boolean containsHumansInfected() {
    for (Object object : humans) {
      Human human = (Human) object;
      if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
        return true;
      }
    }
    return false;
  }

  public void defineTheMaturationTimeOfTheEggs(double temperature) {
    this.timeOfMaturation = 8 + Math.abs(temperature - 25);
  }

  // check how many familes can occupied in a field
  public boolean isCampOccupied(Dadaab dadaab) {
    if (this.getRefugeeHH().size() >= dadaab.getParams().getGlobal().getMaximumHHOccumpancyPerField()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean equals(Building b) {
    if (b.getLocationX() == this.getLocationX() && b.getLocationY() == this.getLocationY()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean equals(int x, int y) {
    if (x == this.getLocationX() && y == this.getLocationY()) {
      return true;
    }
    return false;
  }

  // calaculate distance
  public double distanceTo(Building b) {
    return Math.sqrt(
        Math.pow(b.getLocationX() - this.getLocationX(), 2) + Math.pow(b.getLocationY() - this.getLocationY(), 2));
  }

  public double distanceTo(int xCoord, int yCoord) {
    return Math.sqrt(Math.pow(xCoord - this.getLocationX(), 2) + Math.pow(yCoord - this.getLocationY(), 2));
  }

  public Building copy() {
    Building fieldUnit = new Building(this.getLocationX(), this.getLocationY());
    return fieldUnit;
  }

  public void setRefugeeHH(Bag refugees) {
    this.refugeeHH = refugees;
  }

  public Bag getRefugeeHH() {
    return refugeeHH;
  }

  public void addRefugeeHH(Family r) {
    this.refugeeHH.add(r);
  }

  public void removeRefugeeHH(Family r) {
    this.refugeeHH.remove(r);
  }

  public void setRefugee(Bag humans) {
    this.humans = humans;
  }

  public Bag getHumans() {
    return humans;
  }

  public void addRefugee(Human r) {
    this.humans.add(r);
  }

  public void removeRefugee(Human r) {
    this.humans.remove(r);
  }

  public void addMosquito(Mosquito mosquito) {
    this.mosquitoes.add(mosquito);
  }

  public void removeMosquito(Mosquito mosquito) {
    this.mosquitoes.remove(mosquito);
  }

  public Bag getMosquitoes() {
    return mosquitoes;
  }

  public void setFieldID(int id) {
    this.fieldID = id;
  }

  public int getFieldID() {
    return fieldID;
  }

  public void setCampID(int id) {
    this.campID = id;
  }

  public int getCampID() {
    return campID;
  }

  public void setFacility(Facility f) {
    this.facility = f;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setVibrioCholerae(double vc) {
    this.vibrioCholerae = vc;
  }

  public double getVibrioCholerae() {
    return vibrioCholerae;
  }

  // water - either from borehole or rainfall
  public void setWater(double flow) {
    this.water = flow;
  }

  public double getWater() {
    return water;
  }

  public void addWater(double water) {
    this.water = this.water + water;
  }

  public void waterAbsorption(double absorption) {
    this.water = this.water - absorption;
  }

  public void setPatientCounter(int c) {
    this.patientCounter = c;
  }

  public int getPatientCounter() {
    return patientCounter;
  }

  public void setElevation(double elev) {
    this.elevation = elev;
  }

  public double getElevation() {
    return elevation;
  }

  final public int getLocationX() {
    return locationX;
  }

  final public void setLocationX(int x) {
    this.locationX = x;
  }

  final public int getLocationY() {
    return locationY;
  }

  final public void setLocationY(int y) {
    this.locationY = y;
  }

  public double doubleValue() {
    return getCampID();
  }

  public boolean containsSap() {
    return sap;
  }

  public void setSap(boolean sap) {
    this.sap = sap;
  }

  public boolean containsNectar() {
    return nectar;
  }

  public void setNectar(boolean nectar) {
    this.nectar = nectar;
  }

  // TODO:
  public boolean containsWater() {
    return water != 0;
  }

  public boolean containsHumans() {
    return !this.humans.isEmpty();
  }

  public boolean containsEggs() {
    return this.eggs > 0;
  }

  public void addEgg(int eggs) {
    this.eggs += eggs;
  }

  public void removeEgg() {
    this.eggs--;
  }

  public int getEggs() {
    return this.eggs;
  }

  public boolean containsMosquitoes() {
    return !this.mosquitoes.isEmpty();
  }

  public double getTimeOfMaturation() {
    return timeOfMaturation;
  }

  public void setTimeOfMaturation(double timeOfMaturation) {
    this.timeOfMaturation = timeOfMaturation;
  }

}
