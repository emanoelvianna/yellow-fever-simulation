package com.model;

import sim.util.Bag;

public class Family {

  private Building location;
  private Bag members;
  private Bag otherMembers;
  private Bag relatives;

  public Family(Bag members, Building location) {
    this.members = members;
    this.setLocation(location);
  }

  public Family(Building location) {
    this.setLocation(location);
    members = new Bag();
    setRelatives(new Bag());
  }

  public void addMember(Human human) {
    this.members.add(human);
  }

  public void removeMember(Human human) {
    this.members.remove(human);
  }

  public Bag getMembers() {
    return this.members;
  }

  public Building getLocation() {
    return this.location;
  }

  public void setLocation(Building location) {
    this.location = location;
  }

  public Bag getRelatives() {
    return relatives;
  }

  public void setRelatives(Bag relatives) {
    this.relatives = relatives;
  }

  public Bag getOtherMembers() {
    return otherMembers;
  }

  public void setOtherMembers(Bag otherMembers) {
    this.otherMembers = otherMembers;
  }

}
