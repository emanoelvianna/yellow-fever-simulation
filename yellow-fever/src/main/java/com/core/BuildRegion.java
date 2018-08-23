package com.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.model.Building;
import com.model.Facility;

import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;

public class BuildRegion {
  private static final String BUILDINGS_SHAPEFILE = "data-dadaab/Camp_n.shp";
  private static final String BUILDINGS_ASCGRID = "data-dadaab/d_camp_a.txt";
  private static final String ROADS_SHAPEFILE = "data-dadaab/dadaab_road_f_node.shp";
  private static final String ROADS_ASCGRID = "data-dadaab/d_costp_a.txt";
  private static final String FACILITY_ASCGRID = "data-dadaab/d_faci_a.txt";
  private static final String RAINS_FILE = "data-dadaab/dadaabDailyRain.csv";
  private static final int NUMBERS_OF_AGENTS = 1000;

  private static int GRID_HEIGTH = 0;
  private static int GRID_WIDTH = 0;

  private YellowFever yellowFever;

  public void create(YellowFever yellowFever) {
    this.yellowFever = yellowFever;
    this.loadingData();
  }

  private void loadingData() {
    try {
      BufferedReader regionAscGrid = new BufferedReader(new FileReader(BUILDINGS_ASCGRID));
      String line;
      /* first read the dimensions */
      line = regionAscGrid.readLine(); // read line for width
      String[] tokens = line.split("\\s+");
      int width = Integer.parseInt(tokens[1]);
      GRID_HEIGTH = width;

      line = regionAscGrid.readLine();
      tokens = line.split("\\s+");
      int height = Integer.parseInt(tokens[1]);
      GRID_WIDTH = height;
      /* create grids to attributes */
      createGrids();
      /* skip the next four lines */
      for (int i = 0; i < 4; ++i) {
        line = regionAscGrid.readLine();
      }

      this.yellowFever.getRegionSites().clear();

      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = regionAscGrid.readLine();
        tokens = line.split("\\s+");
        for (int curr_col = 0; curr_col < width; ++curr_col) {
          int regionType = Integer.parseInt(tokens[curr_col]);
          Building building = new Building();
          this.defineBuildingTypesInRegion(regionType, building, curr_col, curr_row);
          // TODO: Quais os tipos de atributos iram existir?
          // fieldUnit.setWater(0);
          this.yellowFever.getRegion().field[curr_col][curr_row] = building;
        }
      }

      /* reading region locations */
      InputStream inputStream = new FileInputStream(new File(BUILDINGS_ASCGRID));
      ArcInfoASCGridImporter.read(inputStream, GridDataType.INTEGER, this.yellowFever.getRegionGeoGrid());

      /* read facility grid */
      BufferedReader facilityAscGrid = new BufferedReader(new FileReader(FACILITY_ASCGRID));
      /* skip the irrelevant metadata */
      for (int i = 0; i < 6; i++) {
        facilityAscGrid.readLine();
      }

      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = facilityAscGrid.readLine();
        tokens = line.split("\\s+");
        for (int curr_col = 0; curr_col < width; ++curr_col) {
          int facilityType = Integer.parseInt(tokens[curr_col]);
          this.defineFacilitiesTypesInRegion(facilityType, curr_col, curr_row);
        }
      }

      /* read road grid */
      BufferedReader road = new BufferedReader(new FileReader(ROADS_ASCGRID));
      /* skip the irrelevant metadata */
      for (int i = 0; i < 6; i++) {
        road.readLine();
      }

      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = road.readLine();
        tokens = line.split("\\s+");
        for (int curr_col = 0; curr_col < width; ++curr_col) {
          double r = Double.parseDouble(tokens[curr_col]); // no need
          int roadID = (int) r * 1000;
          if (roadID >= 0) {
            this.yellowFever.getRoadGrid().set(curr_col, curr_row, roadID);
          }
        }
      }

      this.loadingDailyRainfall(height);

    } catch (IOException ex) {
      Logger.getLogger(BuildRegion.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void loadingDailyRainfall(int height) {
    try {
      String line;
      String[] tokens;
      BufferedReader dailyRainfall = new BufferedReader(new FileReader(RAINS_FILE));
      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = dailyRainfall.readLine();
        tokens = line.split("\\s+");
        int rain = Integer.parseInt(tokens[0]);
        this.yellowFever.getDailyRain()[curr_row] = rain;
      }
    } catch (IOException ex) {
      Logger.getLogger(BuildRegion.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void defineBuildingTypesInRegion(int regionType, Building building, int curr_col, int curr_row) {
    if (regionType > 0) {
      building.setType(regionType);
      if (regionType == 11 || regionType == 21 || regionType == 31) {
        this.yellowFever.getRegionSites().add(building);
      }
      if (regionType >= 10 && regionType <= 12) {
        building.setType(1);
      } else if (regionType >= 20 && regionType <= 22) {
        building.setType(2);
      } else if (regionType >= 30 && regionType <= 32) {
        building.setType(3);
      } else {
        building.setType(0);
      }
    } else {
      building.setType(0);
    }
    building.setX(curr_col);
    building.setY(curr_row);
  }

  private void defineFacilitiesTypesInRegion(int facilityType, int curr_col, int curr_row) {
    if (facilityType > 0 && facilityType < 11) {
      Facility facility = new Facility();
      Building facilityBuilding = (Building) this.yellowFever.getRegion().get(curr_col, curr_row);
      facility.setLocation(facilityBuilding);
      facilityBuilding.setFacility(facility);
      this.yellowFever.getAllFacilities().add(facilityBuilding);
      // facility.setCapacity(0);
      if (facilityType == 1) {
        // facility.setInfectionLevel(0);
        // facilityBuilding.setVibrioCholerae(0);
        facility.setType(2);
        // facilityBuilding.setWater(dadaab.params.global.getBoreholeWaterSupplyPerDay());
        // dadaab.boreHoles.add(facilityBuilding);
      } else if (facilityType == 2 || facilityType == 3) {
        facility.setType(6);
        this.yellowFever.getHealthCenters().add(facilityBuilding);
      } else if (facilityType == 4) {
        facility.setType(5);
        this.yellowFever.getFoodCenter().add(facilityBuilding);
      } else if (facilityType > 5 && facilityType <= 8) {
        facility.setType(1);
        this.yellowFever.getSchooles().add(facilityBuilding);
      } else if (facilityType == 9) {
        facility.setType(4);
        this.yellowFever.getMarket().add(facilityBuilding);
      } else if (facilityType == 10) {
        facility.setType(3);
        this.yellowFever.getMosques().add(facilityBuilding);
      } else {
        facility.setType(8);
        this.yellowFever.getOther().add(facilityBuilding);
      }
      this.yellowFever.getFacilityGrid().setObjectLocation(facility, curr_col, curr_row);
    }
  }

  private void createGrids() {
    this.yellowFever.setRegion(new ObjectGrid2D(GRID_WIDTH, GRID_HEIGTH));
    this.yellowFever.setRainfallGrid(new DoubleGrid2D(GRID_WIDTH, GRID_HEIGTH, 0));
    this.yellowFever.setAllHumans(new Continuous2D(0.1, GRID_WIDTH, GRID_HEIGTH));
    this.yellowFever.setFacilityGrid(new SparseGrid2D(GRID_WIDTH, GRID_HEIGTH));
    this.yellowFever.setRoadGrid(new IntGrid2D(GRID_WIDTH, GRID_HEIGTH));
    this.yellowFever.setNodes(new SparseGrid2D(GRID_WIDTH, GRID_HEIGTH));
    this.yellowFever.setClosestNodes(new ObjectGrid2D(GRID_WIDTH, GRID_HEIGTH));
    this.yellowFever.setRoadShape(new GeomVectorField(GRID_WIDTH, GRID_HEIGTH));
    this.yellowFever.setRegioShape(new GeomVectorField(GRID_WIDTH, GRID_HEIGTH));

    this.yellowFever.setRegionGeoGrid(new GeomGridField());
  }

}
