package com.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.model.Building;
import com.model.Facility;
import com.model.Family;
import com.model.Human;
import com.model.Mosquito;
import com.model.enumeration.ActivityMapping;
import com.model.enumeration.HealthStatus;
import com.model.enumeration.Sex;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import ec.util.MersenneTwisterFast;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomGridField.GridDataType;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class SimulationBuilder {

  private static final String BUILDINGS_SHAPEFILE = "data-dadaab/Camp_n.shp";
  private static final String BUILDINGS_ASCGRID = "data-dadaab/d_camp_a.txt";
  private static final String ROADS_SHAPEFILE = "data-dadaab/dadaab_road_f_node.shp";
  private static final String ROADS_ASCGRID = "data-dadaab/d_costp_a.txt";
  private static final String FACILITY_ASCGRID = "data-dadaab/d_faci_a.txt";
  private static final String ELEVATION_ASCGRID = "data-dadaab/d_dem_n.txt";
  private static final String RAINS_FILE = "data-poa/clima-2012-2014.csv";
  private static int GRID_WIDTH = 0;
  private static int GRID_HEIGHT = 0;

  public void create(YellowFever yellowFever, MersenneTwisterFast random) {
    try {
      String line;
      // buffer reader - read ascii file
      BufferedReader camp = new BufferedReader(new FileReader(BUILDINGS_ASCGRID));
      // first read the dimensions
      line = camp.readLine(); // read line for width
      String[] tokens = line.split("\\s+");
      int width = Integer.parseInt(tokens[1]);
      GRID_WIDTH = width;
      line = camp.readLine();
      tokens = line.split("\\s+");
      int height = Integer.parseInt(tokens[1]);
      GRID_HEIGHT = height;
      createGrids(width, height, yellowFever);
      // skip the next four lines as they contain irrelevant metadata
      for (int i = 0; i < 4; ++i) {
        line = camp.readLine();
      }
      yellowFever.getFamilyHousing().clear();// clear the bag
      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = camp.readLine();
        tokens = line.split("\\s+");
        for (int curr_col = 0; curr_col < width; ++curr_col) {
          int camptype = Integer.parseInt(tokens[curr_col]);
          Building fieldUnit = null;
          fieldUnit = new Building();
          if (camptype > 0) {
            fieldUnit.setFieldID(camptype);
            if (camptype == 11 || camptype == 21 || camptype == 31) {
              yellowFever.getFamilyHousing().add(fieldUnit);
            }

            if (camptype >= 10 && camptype <= 12) {
              fieldUnit.setCampID(1);
            } else if (camptype >= 20 && camptype <= 22) {
              fieldUnit.setCampID(2);
            } else if (camptype >= 30 && camptype <= 32) {
              fieldUnit.setCampID(3);
            } else {
              fieldUnit.setCampID(0);
            }
          } else {
            fieldUnit.setFieldID(0);
          }
          fieldUnit.setLocationX(curr_col);
          fieldUnit.setLocationY(curr_row);
          fieldUnit.setWater(0);
          // dadaab.allFields.add(fieldUnit);
          yellowFever.allCamps.field[curr_col][curr_row] = fieldUnit;
        }
      }
      // read elev and change camp locations id to elev
      InputStream inputStream = new FileInputStream(new File(BUILDINGS_ASCGRID));
      Importer.read(inputStream, GridDataType.INTEGER, yellowFever.allCampGeoGrid);
      // overwrite the file and make 100

      this.readFacilityAscGridFile(yellowFever, tokens, height, width);
      this.readRoadsAscGridFile(yellowFever, tokens, height, width);
      this.readElevationAscGridFile(yellowFever, tokens, height, width);
      this.readShapefiles(yellowFever);
      this.readRainFile(yellowFever);

    } catch (IOException ex) {
      Logger.getLogger(SimulationBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }

    // add agents to the environment
    this.populateHuman(yellowFever);
    this.populateMosquito(yellowFever);

    this.defineFamilies(yellowFever);
    this.defineInitialTemperature(yellowFever);
    this.defineInitialPrecipitation(yellowFever);

    this.generateRandomHumansInfected(yellowFever);
    this.administerRandomVaccines(yellowFever);
  }

  private void readFacilityAscGridFile(YellowFever yellowFever, String[] tokens, int height, int width) {
    try {
      String line;
      // now read facility grid
      BufferedReader fac = new BufferedReader(new FileReader(FACILITY_ASCGRID));
      // skip the irrelevant metadata
      for (int i = 0; i < 6; i++) {
        fac.readLine();
      }

      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = fac.readLine();
        tokens = line.split("\\s+");
        for (int curr_col = 0; curr_col < width; ++curr_col) {
          int facilitytype = Integer.parseInt(tokens[curr_col]);
          if (facilitytype > 0 && facilitytype < 11) {
            int capacity = yellowFever.getParams().getGlobal().getHeaalthFacilityCapacity();
            Facility facility = new Facility(capacity);
            Building facilityField = (Building) yellowFever.allCamps.get(curr_col, curr_row);
            facility.setLocation(facilityField);
            facilityField.setFacility(facility);
            yellowFever.getAllFacilities().add(facilityField);

            if (facilitytype == 1) {
              facility.setFacilityID(2);
              yellowFever.getWorks().add(facilityField);
            } else if (facilitytype == 2 || facilitytype == 3) {
              facility.setCapacity(yellowFever.getParams().getGlobal().getResourcesInMedicalCenters());
              facility.setFacilityID(6);
              yellowFever.getHealthCenters().add(facilityField);
            } else if (facilitytype == 4) {
              facility.setFacilityID(5);
              yellowFever.getFoodCenter().add(facilityField);
            } else if (facilitytype > 5 && facilitytype <= 8) {
              facility.setFacilityID(1);
              yellowFever.getSchooles().add(facilityField);
            } else if (facilitytype == 9) {
              facility.setFacilityID(4);
              yellowFever.getMarket().add(facilityField);
            } else if (facilitytype == 10) {
              facility.setFacilityID(3);
              yellowFever.getMosques().add(facilityField);
            } else {
              facility.setFacilityID(8);
              yellowFever.getOther().add(facilityField);
            }
            yellowFever.facilityGrid.setObjectLocation(facility, curr_col, curr_row);
          }
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(SimulationBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void readRoadsAscGridFile(YellowFever yellowFever, String[] tokens, int height, int width) {
    try {
      String line;
      // now read road grid
      BufferedReader road = new BufferedReader(new FileReader(ROADS_ASCGRID));
      // skip the irrelevant metadata
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
            yellowFever.roadGrid.set(curr_col, curr_row, roadID);

          }
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(SimulationBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void readElevationAscGridFile(YellowFever yellowFever, String[] tokens, int height, int width) {
    try {
      String line;
      // now read elev file and store in bag
      BufferedReader elev = new BufferedReader(new FileReader(ELEVATION_ASCGRID));
      // skip the irrelevant metadata
      for (int i = 0; i < 6; i++) {
        elev.readLine();
      }
      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = elev.readLine();
        tokens = line.split("\\s+");
        for (int curr_col = 0; curr_col < width; ++curr_col) {
          double elevation = Double.parseDouble(tokens[curr_col]);
          if (elevation > 0) {
            Building elevationField = (Building) yellowFever.allCamps.get(curr_col, curr_row);
            elevationField.setElevation(elevation);
          }
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(SimulationBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void readShapefiles(YellowFever yellowFever) {
    try {
      Bag maskedCamp = new Bag();
      maskedCamp.add("CAMPID");
      File file = new File(BUILDINGS_SHAPEFILE);
      URL campShapUL = file.toURL();
      ShapeFileImporter.read(campShapUL, yellowFever.campShape, maskedCamp);
      Bag masked = new Bag();
      // ShapeFileImporter importer = new ShapeFileImporter();
      File file2 = new File(ROADS_SHAPEFILE);
      URL raodLinkUL = file2.toURL();
      ShapeFileImporter.read(raodLinkUL, yellowFever.roadLinks, masked);
      // construct a network of
      extractFromRoadLinks(yellowFever.roadLinks, yellowFever);
      // set up the locations and nearest node capability
      yellowFever.closestNodes = setupNearestNodes(yellowFever);
    } catch (IOException ex) {
      Logger.getLogger(SimulationBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void defineFamilies(YellowFever yellowFever) {
    int max = yellowFever.getParams().getGlobal().getMaximumNumberRelative();
    int[] numberOfFamilies = new int[yellowFever.getAllFamilies().numObjs];

    for (int i = 0; i < yellowFever.getAllFamilies().numObjs; i++) {
      Family f = (Family) yellowFever.getAllFamilies().objs[i];
      int tot = 0;
      if (yellowFever.getAllFamilies().numObjs > max) {
        tot = max;
      } else {
        tot = yellowFever.getAllFamilies().numObjs;
      }

      int numOfRel = 1 + yellowFever.random.nextInt(tot - 1);
      // swap the array index
      for (int kk = 0; kk < numberOfFamilies.length; kk++) {
        int idx = yellowFever.random.nextInt(numberOfFamilies.length);
        int temp = numberOfFamilies[idx];
        numberOfFamilies[idx] = numberOfFamilies[i];
        numberOfFamilies[i] = temp;
      }

      for (int jj = 0; jj < numOfRel; jj++) {
        if (f.equals((Family) yellowFever.getAllFamilies().objs[numberOfFamilies[jj]]) != true) {
          Building l = ((Family) yellowFever.getAllFamilies().objs[numberOfFamilies[jj]]).getLocation();
          f.addRelative(l);
        }
      }
    }
  }

  private void readRainFile(YellowFever yellowFever) {
    String line;
    String divider = ",";
    BufferedReader buffered = null;
    try {
      buffered = new BufferedReader(new FileReader(RAINS_FILE));
      // skip the first line
      buffered.readLine();
      while ((line = buffered.readLine()) != null) {
        String[] info = line.split(divider);
        yellowFever.getClimate().addDate(info[0]);
        yellowFever.getClimate().addPrecipitation(Double.parseDouble(info[1]));
        double average = Double.parseDouble((info[2] + info[3])) / 2;
        yellowFever.getClimate().addTemperature(average);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      if (buffered != null) {
        try {
          buffered.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void defineInitialTemperature(YellowFever yellowFever) {
    Double initial = yellowFever.getClimate().getTemperature().get(0);
    yellowFever.setInitialTemperature(initial);
    for (Object object : yellowFever.getAllMosquitoes()) {
      Mosquito mosquito = (Mosquito) object;
      mosquito.setInitialTemperature(initial);
    }
  }

  private void defineInitialPrecipitation(YellowFever yellowFever) {
    Double initial = yellowFever.getClimate().getPrecipitation().get(0);
    yellowFever.setInitialPrecipitation(initial);
  }

  private void administerRandomVaccines(YellowFever dadaab) {
    int amount = dadaab.getParams().getGlobal().getQuantityOfVaccinesApplied();
    int index = 0;
    while (amount > 0) {
      index = dadaab.random.nextInt(dadaab.getParams().getGlobal().getInitialHumansNumber());
      Human human = (Human) dadaab.allHumans.getAllObjects().get(index);
      if (HealthStatus.SUSCEPTIBLE.equals(human.getCurrentHealthStatus())) {
        human.applyVaccine();
        amount--;
      }
    }
  }

  private void generateRandomHumansInfected(YellowFever yellowFever) {
    int amount = yellowFever.getParams().getGlobal().getInitialHumansNumberInfected();
    int index = 0;
    while (amount > 0) {
      index = yellowFever.random.nextInt(yellowFever.getParams().getGlobal().getInitialHumansNumber());
      Human human = (Human) yellowFever.allHumans.getAllObjects().get(index);
      human.setIncubationPeriod(3 + yellowFever.random.nextInt(4));
      human.setCurrentHealthStatus(HealthStatus.EXPOSED);
      amount--;
    }
  }

  private static void createGrids(int width, int height, YellowFever dadaab) {
    dadaab.allCamps = new ObjectGrid2D(width, height);
    dadaab.rainfallGrid = new DoubleGrid2D(width, height, 0);
    dadaab.allHumans = new Continuous2D(0.1, width, height);
    dadaab.facilityGrid = new SparseGrid2D(width, height);
    dadaab.roadGrid = new IntGrid2D(width, height);
    dadaab.nodes = new SparseGrid2D(width, height);
    dadaab.closestNodes = new ObjectGrid2D(width, height);
    dadaab.roadLinks = new GeomVectorField(width, height);
    dadaab.campShape = new GeomVectorField(width, height);
    dadaab.allCampGeoGrid = new GeomGridField();
  }

  //// add households
  private void addAllHumans(int age, Sex sex, Family hh, YellowFever dadaab) {
    Human human = new Human(age, sex, hh, hh.getLocation(), hh.getLocation(), dadaab.random, dadaab.allHumans);
    hh.addMembers(human);
    hh.getLocation().addRefugee(human);
    human.setCurrentHealthStatus(HealthStatus.SUSCEPTIBLE);
    human.setCurrentActivity(ActivityMapping.STAY_HOME);
    human.setStudent(this.isStudent(age));
    human.setWorker(this.isWorker(age, sex));
    human.setStoppable(dadaab.schedule.scheduleRepeating(human, Human.ORDERING, 1.0));
  }

  private boolean isStudent(int age) {
    if (age < 20) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isWorker(int age, Sex sex) {
    if (Sex.F.equals(sex) && (age >= 20 && age <= 60)) {
      return true;
    } else if (Sex.M.equals(sex) && (age >= 20 && age <= 65)) {
      return true;
    }
    return false;
  }

  // random searching of next parcel to populate houses
  public static Building nextAvailCamp(YellowFever yellowFever) {
    // for now random
    int index = yellowFever.random.nextInt(yellowFever.getFamilyHousing().numObjs);
    while (((Building) yellowFever.getFamilyHousing().objs[index]).isCampOccupied(yellowFever) == true
        || yellowFever.getAllFacilities().contains((Building) yellowFever.getFamilyHousing().objs[index]) == true) {
      // try another spot
      index = yellowFever.random.nextInt(yellowFever.getFamilyHousing().numObjs);
    }
    return (Building) yellowFever.getFamilyHousing().objs[index];
  }

  // create humans - first family
  private void populateHuman(YellowFever yellowFever) {
    // 1 = 15% , 2 =30% , 3 = 27%, 4 = 18%, 5 = 8%, 6 >= 2%
    // proportion of teta = families/ total population = 8481/29772 ~ 0.3
    double teta = 0.3;
    int totalHumans = yellowFever.getParams().getGlobal().getInitialHumansNumber();
    // proportion of household
    double[] prop = { 0.15, 0.30, 0.27, 0.18, 0.08, 0.02, 0.01, 0.01 };
    // family size - all are zero
    int[] size = { 0, 0, 0, 0, 0, 0, 0, 0 };
    // family size ranges from 1 to 11

    int count = 0;
    int rem = 0;// remaining
    int curTot = 0;

    for (int i = 0; i < size.length; i++) {
      double x = prop[i] * totalHumans * teta;
      int hh = (int) Math.round(x);
      size[i] = hh;
      curTot = curTot + ((i + 1) * hh);
    }

    if (curTot > totalHumans) {
      size[0] = size[0] - (curTot - totalHumans);
    }

    if (curTot < totalHumans) {
      size[0] = size[0] + (totalHumans - curTot);
    }

    /// creating aray of each family size ( disaggregate) and distibute randomly

    // calculate total hh size
    int ts = 0;
    for (int i = 0; i < size.length; i++) {
      ts = ts + size[i];
    }
    // initalize array based on hh size
    int[] sizeDist = new int[ts];

    // add each hh size
    int c = 0;
    int k = 0;
    for (int t = 0; t < size.length; t++) {
      int sum = size[t];
      c = c + sum;
      for (int j = k; j < c; j++) {
        sizeDist[j] = t + 1;
      }
      k = c;
    }

    // swaping with random posiion
    for (int i = 0; i < sizeDist.length; i++) {
      int change = i + yellowFever.random.nextInt(sizeDist.length - i);
      int holder = sizeDist[i];
      sizeDist[i] = sizeDist[change];
      sizeDist[change] = holder;
    }

    // initialize household
    for (int a = 0; a < sizeDist.length; a++) {
      int counter = 0;

      int tot = sizeDist[a];
      counter = counter + tot;
      if (tot != 0 && counter <= totalHumans) {
        Building fieldUnit = nextAvailCamp(yellowFever);
        Family hh = new Family(fieldUnit);
        yellowFever.getAllFamilies().add(hh);
        fieldUnit.addRefugeeHH(hh);

        // 50% chance to contains nectar
        if (yellowFever.random.nextDouble() < 0.5) {
          fieldUnit.setNectar(true);
        }
        // 50% chance to contains sap
        if (yellowFever.random.nextDouble() < 0.5) {
          fieldUnit.setSap(true);
        }

        int random = yellowFever.random.nextInt(101);
        int age = 0;
        for (int i = 0; i < tot; i++) {
          if (i == 0) {
            // a household head need to be between 18-59;
            age = 18 + yellowFever.random.nextInt(42);
          } else {
            if (random <= 25) {
              // 25% chance the age between 5-19
              age = 5 + yellowFever.random.nextInt(14);
            } else if (random <= 45) {
              // 40% chance the age between 20-34
              age = 20 + yellowFever.random.nextInt(14);
            } else if (random <= 70) {
              // 25% chance the age between 35-49
              age = 35 + yellowFever.random.nextInt(14);
            } else if (random <= 90) {
              // 20% chance the age between 50-64
              age = 50 + yellowFever.random.nextInt(14);
            } else {
              // 10% chance the age between 65-90
              age = 65 + yellowFever.random.nextInt(25);
            }
          }
          Sex sex = this.defineSex(yellowFever);
          addAllHumans(age, sex, hh, yellowFever);
        }
      }
    }
  }

  public void populateMosquito(YellowFever dadaab) {
    int initialMosquitoesNumber = dadaab.getParams().getGlobal().getInitialMosquitoesNumber();

    while (initialMosquitoesNumber > 0) {
      int index = dadaab.random.nextInt(dadaab.getFamilyHousing().numObjs);
      Building housing = (Building) dadaab.getFamilyHousing().objs[index];
      if (housing.containsMosquitoes()) {
        if (dadaab.random.nextDouble() > 0.5) { // 50% chance of has more
                                                // mosquitoes
          Mosquito mosquito = new Mosquito(housing);
          mosquito.setStoppable(dadaab.schedule.scheduleRepeating(mosquito, Mosquito.ORDERING, 1.0));
          housing.addMosquito(mosquito);
          dadaab.addMosquitoes(mosquito);
          initialMosquitoesNumber--;
        }
      } else {
        Mosquito mosquito = new Mosquito(housing);
        mosquito.setStoppable(dadaab.schedule.scheduleRepeating(mosquito, Mosquito.ORDERING, 1.0));
        housing.addMosquito(mosquito);
        dadaab.addMosquitoes(mosquito);
        initialMosquitoesNumber--;
      }
    }
  }

  public Sex defineSex(YellowFever dadaab) {
    // sex 50-50 chance
    if (dadaab.random.nextDouble() > 0.5) {
      return Sex.M;
    } else {
      return Sex.F;
    }
  }

  /// raod network methods from haiti project
  static void extractFromRoadLinks(GeomVectorField roadLinks, YellowFever dadaab) {
    Bag geoms = roadLinks.getGeometries();
    Envelope e = roadLinks.getMBR();
    double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
    int xcols = GRID_WIDTH - 1, ycols = GRID_HEIGHT - 1;

    // extract each edge
    for (Object o : geoms) {

      MasonGeometry gm = (MasonGeometry) o;
      if (gm.getGeometry() instanceof LineString) {
        readLineString((LineString) gm.getGeometry(), xcols, ycols, xmin, ymin, xmax, ymax, dadaab);
      } else if (gm.getGeometry() instanceof MultiLineString) {
        MultiLineString mls = (MultiLineString) gm.getGeometry();
        for (int i = 0; i < mls.getNumGeometries(); i++) {
          readLineString((LineString) mls.getGeometryN(i), xcols, ycols, xmin, ymin, xmax, ymax, dadaab);
        }
      }
    }
  }

  /**
   * Converts an individual linestring into a series of links and nodes in the
   * network int width, int height, Dadaab dadaab
   * 
   * @param geometry
   * @param xcols
   *          - number of columns in the field
   * @param ycols
   *          - number of rows in the field
   * @param xmin
   *          - minimum x value in shapefile
   * @param ymin
   *          - minimum y value in shapefile
   * @param xmax
   *          - maximum x value in shapefile
   * @param ymax
   *          - maximum y value in shapefile
   */
  static void readLineString(LineString geometry, int xcols, int ycols, double xmin, double ymin, double xmax,
      double ymax, YellowFever dadaab) {

    CoordinateSequence cs = geometry.getCoordinateSequence();

    // iterate over each pair of coordinates and establish a link between
    // them
    Node oldNode = null; // used to keep track of the last node referenced
    for (int i = 0; i < cs.size(); i++) {

      // calculate the location of the node in question
      double x = cs.getX(i), y = cs.getY(i);
      int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)),
          yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin))); // REMEMBER
                                                                                 // TO
                                                                                 // FLIP
                                                                                 // THE
                                                                                 // Y
                                                                                 // VALUE

      if (xint >= GRID_WIDTH) {
        continue;
      } else if (yint >= GRID_HEIGHT) {
        continue;
      }

      // find that node or establish it if it doesn't yet exist
      Bag ns = dadaab.nodes.getObjectsAtLocation(xint, yint);
      Node n;
      if (ns == null) {
        n = new Node(new Building(xint, yint));
        dadaab.nodes.setObjectLocation(n, xint, yint);
      } else {
        n = (Node) ns.get(0);
      }

      if (oldNode == n) // don't link a node to itself
      {
        continue;
      }

      // attach the node to the previous node in the chain (or continue if
      // this is the first node in the chain of links)

      if (i == 0) { // can't connect previous link to anything
        oldNode = n; // save this node for reference in the next link
        continue;
      }

      int weight = (int) n.getLocation().distanceTo(oldNode.getLocation()); // weight
                                                                            // is
                                                                            // just
      // distance

      // create the new link and save it
      Edge e = new Edge(oldNode, n, weight);
      dadaab.roadNetwork.addEdge(e);
      oldNode.getLinks().add(e);
      n.getLinks().add(e);

      oldNode = n; // save this node for reference in the next link
    }
  }

  /**
   * Used to find the nearest node for each space
   * 
   */
  static class Crawler {

    Node node;
    Building location;

    public Crawler(Node n, Building l) {
      node = n;
      location = l;
    }
  }

  /**
   * Calculate the nodes nearest to each location and store the information
   * 
   * @param closestNodes
   *          - the field to populate
   */
  static ObjectGrid2D setupNearestNodes(YellowFever dadaab) {

    ObjectGrid2D closestNodes = new ObjectGrid2D(GRID_WIDTH, GRID_HEIGHT);
    ArrayList<Crawler> crawlers = new ArrayList<Crawler>();

    for (Object o : dadaab.roadNetwork.allNodes) {
      Node n = (Node) o;
      Crawler c = new Crawler(n, n.getLocation());
      crawlers.add(c);
    }

    // while there is unexplored space, continue!
    while (crawlers.size() > 0) {
      ArrayList<Crawler> nextGeneration = new ArrayList<Crawler>();

      // randomize the order in which cralwers are considered
      int size = crawlers.size();

      for (int i = 0; i < size; i++) {

        // randomly pick a remaining crawler
        int index = dadaab.random.nextInt(crawlers.size());
        Crawler c = crawlers.remove(index);

        // check if the location has already been claimed
        Node n = (Node) closestNodes.get(c.location.getLocationX(), c.location.getLocationY());

        if (n == null) { // found something new! Mark it and reproduce

          // set it
          closestNodes.set(c.location.getLocationX(), c.location.getLocationY(), c.node);

          // reproduce
          Bag neighbors = new Bag();

          dadaab.allCamps.getNeighborsHamiltonianDistance(c.location.getLocationX(), c.location.getLocationY(), 1,
              false, neighbors, null, null);

          for (Object o : neighbors) {
            Building l = (Building) o;
            // Location l = (Location) o;
            if (l == c.location) {
              continue;
            }
            Crawler newc = new Crawler(c.node, l);
            nextGeneration.add(newc);
          }
        }
        // otherwise just die
      }
      crawlers = nextGeneration;
    }
    return closestNodes;
  }

}
