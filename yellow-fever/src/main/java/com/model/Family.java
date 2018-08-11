package com.model;

import sim.util.Bag;

public class Family {

  private Bag members;
  private Residence location;

  public Family(Bag members, Residence location) {
    this.members = members;
    this.location = location;
  }

  public Bag getMembers() {
    return members;
  }

  public Residence getLocation() {
    return location;
  }

}
