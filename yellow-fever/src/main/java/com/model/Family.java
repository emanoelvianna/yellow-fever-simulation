package com.model;

import sim.util.Bag;

public class Family {

  private Bag members;
  private Residence location;

  public Family(Bag members, Residence location) {
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

  public Residence getLocation() {
    return this.location;
  }

}
