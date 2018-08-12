package com.main;

import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class Region {

  public void create(YellowFever yellowFever) {
    try {
      System.out.println("[INFO] Reading the layer of buildings");
      Bag buildings = new Bag();
      // buildings.add("BUILDINGS");
      ShapeFileImporter.read(new URL("file:data/saidabuildings.shp"), yellowFever.getBuildingsShape(), buildings);

      System.out.println("[INFO] Reading the layer of streets");
      Bag streets = new Bag();
      // streets.add("BUILDINGS");
      ShapeFileImporter.read(new URL("file:data/saidaroads.shp"), yellowFever.getStreetsShape(), streets);

      System.out.println("[INFO] Completed readings");

      Envelope MBR = yellowFever.getBuildingsShape().getMBR();
      MBR.expandToInclude(yellowFever.getStreetsShape().getMBR());
      yellowFever.getBuildingsShape().setMBR(MBR);
      yellowFever.getStreetsShape().setMBR(MBR);

      yellowFever.getNetwork().createFromGeomField(yellowFever.getBuildingsShape());

      addIntersectionNodes(yellowFever, yellowFever.getNetwork().nodeIterator(), yellowFever.getJunctions());

      System.out.println("[INFO] Completed construction");
    } catch (Exception exception) {
      Logger.getLogger(YellowFever.class.getName()).log(Level.SEVERE, null, exception);
    }
  }

  private void addIntersectionNodes(YellowFever yellowFever, Iterator nodeIterator, GeomVectorField intersections) {
    GeometryFactory fact = new GeometryFactory();
    Coordinate coord = null;
    Point point = null;
    int counter = 0;

    while (nodeIterator.hasNext()) {
      Node node = (Node) nodeIterator.next();
      coord = node.getCoordinate();
      point = fact.createPoint(coord);

      yellowFever.getJunctions().addGeometry(new MasonGeometry(point));
      counter++;
    }
  }
}
