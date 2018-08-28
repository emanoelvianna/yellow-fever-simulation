package com.core;

import java.util.ArrayList;

import com.model.FieldUnit;

import sim.field.network.Edge;

public class Node {

  private FieldUnit location;
  private ArrayList<Edge> links;

  public Node(FieldUnit l) {
    setLocation(l);
    setLinks(new ArrayList<Edge>());
  }

  public FieldUnit getLocation() {
    return location;
  }

  public void setLocation(FieldUnit location) {
    this.location = location;
  }

  public ArrayList<Edge> getLinks() {
    return links;
  }

  public void setLinks(ArrayList<Edge> links) {
    this.links = links;
  }

}