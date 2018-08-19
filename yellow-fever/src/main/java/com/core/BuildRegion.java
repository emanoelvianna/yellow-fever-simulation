package com.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildRegion {

  private static final String BUILDINGS_SHAPEFILE = "data-dadaab/Camp_n.shp";
  private static final String BUILDINGS_ASCGRID = "data-dadaab/d_camp_a.txt";
  private static final String ROADS_SHAPEFILE = "data-dadaab/dadaab_road_f_node.shp";
  private static final String ROADS_ASCGRID = "data-dadaab/d_costp_a.txt";
  private static final String FACILITY_ASCGRID = "data-dadaab/d_faci_a.txt";
  private YellowFever yellowFever;

  public void create(YellowFever yellowFever) {
    this.yellowFever = yellowFever;
    this.loadingData();
  }

  private void loadingData() {
    try {
      BufferedReader buildingsAscGrid = new BufferedReader(new FileReader(BUILDINGS_ASCGRID));
    } catch (IOException ex) {
      Logger.getLogger(BuildRegion.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void buildFamily() {
    BuildFamily buildFamily = new BuildFamily(this.yellowFever);
  }

}
