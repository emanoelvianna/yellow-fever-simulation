package com.model;

import sim.util.Bag;

public class Family {

  private Bag members;
  private Bag otherMembers;
  private Building location;

  public Family(Bag members, Building location) {
    this.members = members;
    this.location = location;
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

}
