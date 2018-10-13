package com.model;

import com.model.enumeration.HealthStatus;

import sim.util.Bag;

public class Family {

  private Bag relatives; // hold relative location
  private Building location; // location of the house
  private Bag members; // holds the family members

  public Family(Building loc) {
    this.setLocation(loc);
    members = new Bag();
    relatives = new Bag();
  }

  public int numberOfInfectedFamilyMembers() {
    int count = 0;
    for (Object f : this.getMembers()) {
      Human ref = (Human) f;
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

  // location of house
  final public void setLocation(Building location) {
    this.location = location;
  }

  final public Building getLocation() {
    return location;
  }

  // holds memebers of the family
  public void setMembers(Bag members) {
    this.members = members;
  }

  public Bag getMembers() {
    return members;
  }

  public void addMembers(Human human) {
    this.members.add(human);
  }

  public void removeMembers(Human human) {
    this.members.remove(human);
  }

  // location of the relative
  public void setRelativesLocation(Bag relatives) {
    this.relatives = relatives;
  }

  public Bag getRelativesLocation() {
    return relatives;
  }

  public void addRelative(Building relative) {
    relatives.add(relative);
  }

  public void removeFriend(Building relative) {
    relatives.remove(relative);
  }

}
