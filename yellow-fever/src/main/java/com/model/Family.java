package com.model;

import com.model.enumeration.HealthStatus;

import sim.util.Bag;

public class Family {
  // location
  // food requirement
  //
  // private double foodReq;

  private int rationDate; // food ration date;
  Bag relatives; // hold relative location
  FieldUnit location; // location of the house
  Bag members; // holds the family members
  private boolean hasLaterine = false;

  public Family(FieldUnit loc) {
    this.setCampLocation(loc);
    members = new Bag();
    relatives = new Bag();
  }

  public void setHasLaterine(boolean laterine) {
    this.hasLaterine = laterine;
  }

  public boolean getHasLaterine() {
    return hasLaterine;
  }

  // location of house
  final public void setCampLocation(FieldUnit location) {
    this.location = location;
  }

  final public FieldUnit getCampLocation() {
    return location;
  }

  // holds memebers of the family
  public void setMembers(Bag refugees) {
    this.members = refugees;
  }

  public Bag getMembers() {
    return members;
  }

  public void addMembers(Refugee r) {
    this.members.add(r);
  }

  public void removeMembers(Refugee r) {
    this.members.remove(r);
  }

  // when the family get food from food center
  public void setRationDate(int ration) {
    this.rationDate = ration;
  }

  public int getRationDate() {
    return rationDate;
  }

  // location of the relative
  public void setRelativesLocation(Bag r) {
    this.relatives = r;
  }

  public Bag getRelativesLocation() {
    return relatives;
  }

  public void addRelative(FieldUnit relative) {
    relatives.add(relative);
  }

  public void removeFriend(FieldUnit relative) {
    relatives.remove(relative);
  }

  public int numberOfInfectedFamilyMembers() {
    int count = 0;
    for (Object f : this.getMembers()) {
      Refugee ref = (Refugee) f;
      if (ref.getCurrentHealthStatus() == HealthStatus.MILD_INFECTION) {
        count = count + 1;
      } else if (ref.getCurrentHealthStatus() == HealthStatus.SEVERE_INFECTION) {
        count = count + 1;
      } else if (ref.getCurrentHealthStatus() == HealthStatus.TOXIC_INFECTION) {
        count = count + 1;
      }
    }
    return count;
  }

}
