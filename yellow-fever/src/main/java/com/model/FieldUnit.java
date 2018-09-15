package com.model;

import java.io.Serializable;

import com.core.Dadaab;

import sim.util.Bag;
import sim.util.Valuable;

public class FieldUnit implements Valuable, Serializable {

  private int fieldID; // identify the type pf the field
  private int campID; // holds id of the three camps
  private double water; // hold water amount
  private boolean sap;
  private boolean nectar;
  private double elevation; // elevation
  private double vibrioCholerae = 0; // contamination level
  private int patientCounter = 0;
  private Facility facility;
  private int locationX;
  private int locationY;
  private Bag refugeeHH; // camp location for household
  private Bag refugee; // who are on the field right now
  private Bag mosquito;
  // TODO: quem sabe ainda não está muito claro, ou mesmo errado
  private boolean presentHumans;

  // getter and setter
  public FieldUnit() {
    super();
    refugeeHH = new Bag();
    refugee = new Bag();
    this.setMosquito(new Bag());
  }

  public FieldUnit(int x, int y) {
    this.setLocationX(x);
    this.setLocationY(y);
  }

  // check how many familes can occupied in a field
  public boolean isCampOccupied(Dadaab dadaab) {
    if (this.getRefugeeHH().size() >= dadaab.getParams().getGlobal().getMaximumHHOccumpancyPerField()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean equals(FieldUnit b) {
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
  public double distanceTo(FieldUnit b) {
    return Math.sqrt(
        Math.pow(b.getLocationX() - this.getLocationX(), 2) + Math.pow(b.getLocationY() - this.getLocationY(), 2));
  }

  public double distanceTo(int xCoord, int yCoord) {
    return Math.sqrt(Math.pow(xCoord - this.getLocationX(), 2) + Math.pow(yCoord - this.getLocationY(), 2));
  }

  public FieldUnit copy() {
    FieldUnit fieldUnit = new FieldUnit(this.getLocationX(), this.getLocationY());
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

  public void setRefugee(Bag refugeeMoving) {
    this.refugee = refugeeMoving;
  }

  public Bag getRefugee() {
    return refugee;
  }

  public void addRefugee(Human r) {
    this.refugee.add(r);
  }

  public void removeRefugee(Human r) {
    this.refugee.remove(r);
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

  public Bag getMosquito() {
    return mosquito;
  }

  public void setMosquito(Bag mosquito) {
    this.mosquito = mosquito;
  }

  public boolean containsPresentHumans() {
    return !refugee.isEmpty();
  }

}
