package com.model;

import java.io.Serializable;

import com.core.YellowFever;

import sim.util.Bag;
import sim.util.Valuable;

public class Building implements Valuable, Serializable {

  private static final long serialVersionUID = 1L;
  private int fieldID; // identify the type pf the field
  private int campID; // holds id of the three camps
  private double water; // hold water amount
  private boolean sap;
  private boolean nectar;
  private double elevation; // elevation
  private double timeOfMaturation;
  private int amountOfResources;
  private int quantityOfVaccines;
  private int patientCounter;
  private Facility facility;
  private int locationX;
  private int locationY;
  private Bag refugeeHH; // camp location for household
  private Bag humans; // who are on the field right now
  private Bag mosquitoes;
  private Bag eggs;

  public Building() {
    super();
    this.refugeeHH = new Bag();
    this.humans = new Bag();
    this.mosquitoes = new Bag();
    this.eggs = new Bag();
    this.timeOfMaturation = 0;
    this.amountOfResources = 0;
    this.patientCounter = 0;
    this.quantityOfVaccines = 0;
    this.water = 0;
  }

  public Building(int x, int y) {
    this.humans = new Bag();
    this.mosquitoes = new Bag();
    this.refugeeHH = new Bag();
    this.eggs = new Bag();
    this.locationX = x;
    this.locationY = y;
    this.timeOfMaturation = 0;
    this.amountOfResources = 0;
    this.patientCounter = 0;
    this.quantityOfVaccines = 0;
    this.water = 0;
  }

  // check how many familes can occupied in a field
  public synchronized boolean isCampOccupied(YellowFever dadaab) {
    if (this.getRefugeeHH().size() >= dadaab.getParams().getGlobal().getMaximumFamilyOccumpancyPerBuilding()) {
      return true;
    } else {
      return false;
    }
  }

  public synchronized boolean equals(Building b) {
    if (b.getLocationX() == this.getLocationX() && b.getLocationY() == this.getLocationY()) {
      return true;
    } else {
      return false;
    }
  }

  public synchronized boolean equals(int x, int y) {
    if (x == this.getLocationX() && y == this.getLocationY()) {
      return true;
    }
    return false;
  }

  // calaculate distance
  public synchronized double distanceTo(Building b) {
    return Math.sqrt(
        Math.pow(b.getLocationX() - this.getLocationX(), 2) + Math.pow(b.getLocationY() - this.getLocationY(), 2));
  }

  public synchronized double distanceTo(int xCoord, int yCoord) {
    return Math.sqrt(Math.pow(xCoord - this.getLocationX(), 2) + Math.pow(yCoord - this.getLocationY(), 2));
  }

  public synchronized Building copy() {
    Building fieldUnit = new Building(this.getLocationX(), this.getLocationY());
    return fieldUnit;
  }

  public void setRefugeeHH(Bag refugees) {
    this.refugeeHH = refugees;
  }

  public synchronized Bag getRefugeeHH() {
    return refugeeHH;
  }

  public synchronized void addRefugeeHH(Family r) {
    this.refugeeHH.add(r);
  }

  public synchronized void removeRefugeeHH(Family r) {
    this.refugeeHH.remove(r);
  }

  public void setRefugee(Bag humans) {
    this.humans = humans;
  }

  public synchronized Bag getHumans() {
    return humans;
  }

  public synchronized void addRefugee(Human r) {
    this.humans.add(r);
  }

  public synchronized void removeRefugee(Human r) {
    this.humans.remove(r);
  }

  public synchronized void addMosquito(Mosquito mosquito) {
    this.mosquitoes.add(mosquito);
  }

  public synchronized void removeMosquito(Mosquito mosquito) {
    this.mosquitoes.remove(mosquito);
  }

  public synchronized Bag getMosquitoes() {
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

  // water - either from borehole or rainfall
  public void setWater(double flow) {
    this.water = flow;
  }

  public synchronized double getWater() {
    return water;
  }

  public synchronized void addWater(double water) {
    this.water = this.water + water;
  }

  public synchronized void waterAbsorption(double absorption) {
    this.water = this.water - absorption;
  }

  public synchronized void addPatient() {
    this.patientCounter++;
  }

  public synchronized void removePatient() {
    this.patientCounter--;
  }

  public void setPatientCounter(int count) {
    this.patientCounter = count;
  }

  public synchronized int getPatientCounter() {
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

  public synchronized boolean containsSap() {
    return this.sap;
  }

  public void setSap(boolean sap) {
    this.sap = sap;
  }

  public synchronized boolean containsNectar() {
    return this.nectar;
  }

  public void setNectar(boolean nectar) {
    this.nectar = nectar;
  }

  public synchronized boolean containsWater() {
    return this.water != 0;
  }

  public synchronized boolean containsHumans() {
    return this.humans.size() > 0;
  }

  public synchronized boolean containsEggs() {
    return !this.eggs.isEmpty();
  }

  public synchronized void addEgg(Egg eggs) {
    this.eggs.add(eggs);
  }

  public synchronized Bag getEggs() {
    return this.eggs;
  }

  public synchronized boolean containsMosquitoes() {
    return !this.mosquitoes.isEmpty();
  }

  public synchronized double getTimeOfMaturation() {
    return timeOfMaturation;
  }

  public void setTimeOfMaturation(double timeOfMaturation) {
    this.timeOfMaturation = timeOfMaturation;
  }

  public synchronized int getAmountOfResources() {
    return amountOfResources;
  }

  public void setAmountOfResources(int amountOfResources) {
    this.amountOfResources = amountOfResources;
  }

  public synchronized int getQuantityOfVaccines() {
    return quantityOfVaccines;
  }

  public void setQuantityOfVaccines(int quantityOfVaccines) {
    this.quantityOfVaccines = quantityOfVaccines;
  }

}
