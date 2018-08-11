package com.main;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Envelope;

import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;

public class Region {

  public static void create(YellowFever yellowFever) {
    try {
      System.out.println("[INFO] Reading the layer of buildings");
      Bag buildings = new Bag();
      // buildings.add("BUILDINGS");
      ShapeFileImporter.read(new URL("file:data/buildings.shp"), yellowFever.getBuildingsShape(), buildings);

      System.out.println("[INFO] Reading the layer of streets");
      Bag streets = new Bag();
      // streets.add("BUILDINGS");
      ShapeFileImporter.read(new URL("file:data/streets.shp"), yellowFever.getStreetsShape(), streets);

      System.out.println("[INFO] Completed readings");

      Envelope MBR = yellowFever.getBuildingsShape().getMBR();
      MBR.expandToInclude(yellowFever.getStreetsShape().getMBR());
      yellowFever.getBuildingsShape().setMBR(MBR);
      yellowFever.getStreetsShape().setMBR(MBR);
      
      System.out.println("[INFO] Completed construction");
    } catch (Exception exception) {
      Logger.getLogger(YellowFever.class.getName()).log(Level.SEVERE, null, exception);
    }
  }
}
