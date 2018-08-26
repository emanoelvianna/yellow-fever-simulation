package com.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import sim.io.geo.ArcInfoASCGridImporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

public class BuildRegion {
  private static final String BUILDINGS_SHAPEFILE = "data-dadaab/Camp_n.shp";
  private static final String BUILDINGS_ASCGRID = "data-dadaab/d_camp_a.txt";
  private static final String ROADS_SHAPEFILE = "data-dadaab/dadaab_road_f_node.shp";
  private static final String ROADS_ASCGRID = "data-dadaab/d_costp_a.txt";
  private static final String FACILITY_ASCGRID = "data-dadaab/d_faci_a.txt";
  private static final String RAINS_FILE = "data-dadaab/dadaabDailyRain.csv";
  private static final String ELEVATION_ASCGRID = "data-dadaab/d_dem_n.txt";
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
          // TODO: Quais os tipos de atributos iram existir sobre as construções?
          // fieldUnit.setWater(0);
          this.yellowFever.getRegion().field[curr_col][curr_row] = building;
        }
      }

      /* read region locations */
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

      /* read rain file */
      this.loadingDailyRainfall(height);

      /* read region elevation file */
      this.loadingRegionElevation(line, tokens, height, width);

      /* read shapeFile */
      this.loadingShapeFiles();

    } catch (IOException ex) {
      Logger.getLogger(BuildRegion.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void loadingShapeFiles() {
    try {

      /* read buildings */
      Bag maskedCamp = new Bag();
      maskedCamp.add("CAMPID");
      File file = new File("data-dadaab/Camp_n.shp");
      URL campShapUL = file.toURL();
      ShapeFileImporter.read(campShapUL, this.yellowFever.getRegionShape(), maskedCamp);

      /* read roads */
      Bag masked = new Bag();
      File file2 = new File("data-dadaab/dadaab_road_f_node.shp");
      URL raodLinkUL = file2.toURL();
      ShapeFileImporter.read(raodLinkUL, this.yellowFever.getRoadShape(), masked);
      /* construct a network of roads */
      extractFromRoadLinks(this.yellowFever.getRoadShape(), this.yellowFever);

      // set up the locations and nearest node capability
      this.yellowFever.setClosestNodes(setupNearestNodes(this.yellowFever));
    } catch (IOException ex) {
      Logger.getLogger(BuildRegion.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  private void loadingRegionElevation(String line, String[] tokens, int height, int width) {
    try {

      BufferedReader elev = new BufferedReader(new FileReader(ELEVATION_ASCGRID));
      /* skip the irrelevant metadata */
      for (int i = 0; i < 6; i++) {
        elev.readLine();
      }
      for (int curr_row = 0; curr_row < height; ++curr_row) {
        line = elev.readLine();
        tokens = line.split("\\s+");
        for (int curr_col = 0; curr_col < width; ++curr_col) {
          double elevation = Double.parseDouble(tokens[curr_col]);
          if (elevation > 0) {
            Building building = (Building) yellowFever.getRegion().get(curr_col, curr_row);
            building.setElevation(elevation);
          }
        }
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
      // TODO: Utilizado para limitar a capacidade de instalação
      // facility.setCapacity(0);
      if (facilityType == 1) {
        // TODO: Dentro da instalação, irá existir infecção?
        // facility.setInfectionLevel(0);
        // facilityBuilding.setVibrioCholerae(0);
        facility.setType(2);
        // TODO: Dentro da instalação, irá existir nível de água?
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

  static void extractFromRoadLinks(GeomVectorField roadLinks, YellowFever yellowFever) {
    Bag geoms = roadLinks.getGeometries();
    Envelope e = roadLinks.getMBR();
    double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
    int xcols = GRID_WIDTH - 1, ycols = GRID_HEIGTH - 1;

    // extract each edge
    for (Object o : geoms) {

      MasonGeometry gm = (MasonGeometry) o;
      if (gm.getGeometry() instanceof LineString) {
        readLineString((LineString) gm.getGeometry(), xcols, ycols, xmin, ymin, xmax, ymax, yellowFever);
      } else if (gm.getGeometry() instanceof MultiLineString) {
        MultiLineString mls = (MultiLineString) gm.getGeometry();
        for (int i = 0; i < mls.getNumGeometries(); i++) {
          readLineString((LineString) mls.getGeometryN(i), xcols, ycols, xmin, ymin, xmax, ymax, yellowFever);
        }
      }
    }
  }

  static void readLineString(LineString geometry, int xcols, int ycols, double xmin, double ymin, double xmax,
      double ymax, YellowFever yellowFever) {

    CoordinateSequence cs = geometry.getCoordinateSequence();
    // iterate over each pair of coordinates and establish a link between
    // them
    Node oldNode = null; // used to keep track of the last node referenced
    for (int i = 0; i < cs.size(); i++) {
      // calculate the location of the node in question
      double x = cs.getX(i), y = cs.getY(i);
      int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)),
          yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin))); // REMEMBER TO FLIP THE Y VALUE
      if (xint >= GRID_WIDTH) {
        continue;
      } else if (yint >= GRID_HEIGTH) {
        continue;
      }
      // find that node or establish it if it doesn't yet exist
      Bag ns = yellowFever.getNodes().getObjectsAtLocation(xint, yint);
      Node node;
      if (ns == null) {
        node = new Node(new Building(xint, yint));
        yellowFever.getNodes().setObjectLocation(node, xint, yint);
      } else {
        node = (Node) ns.get(0);
      }
      if (oldNode == node) // don't link a node to itself
        continue;
      // attach the node to the previous node in the chain (or continue if
      // this is the first node in the chain of links)
      if (i == 0) { // can't connect previous link to anything
        oldNode = node; // save this node for reference in the next link
        continue;
      }
      int weight = (int) node.building.distanceTo(oldNode.building); // weight is just
      // distance
      // create the new link and save it
      Edge e = new Edge(oldNode, node, weight);
      yellowFever.getRoadNetwork().addEdge(e);
      oldNode.links.add(e);
      node.links.add(e);
      oldNode = node; // save this node for reference in the next link
    }
  }

  public ObjectGrid2D setupNearestNodes(YellowFever yellowFever) {

    ObjectGrid2D closestNodes = new ObjectGrid2D(GRID_WIDTH, GRID_HEIGTH);
    ArrayList<Crawler> crawlers = new ArrayList<Crawler>();

    for (Object o : yellowFever.getRoadNetwork().allNodes) {
      Node n = (Node) o;
      Crawler c = new Crawler(n, n.building);
      crawlers.add(c);
    }

    // while there is unexplored space, continue!
    while (crawlers.size() > 0) {
      ArrayList<Crawler> nextGeneration = new ArrayList<Crawler>();
      // randomize the order in which cralwers are considered
      int size = crawlers.size();
      for (int i = 0; i < size; i++) {
        // randomly pick a remaining crawler
        int index = yellowFever.random.nextInt(crawlers.size());
        Crawler c = crawlers.remove(index);
        // check if the location has already been claimed
        Node n = (Node) closestNodes.get(c.building.getX(), c.building.getY());
        if (n == null) { // found something new! Mark it and reproduce
          // set it
          closestNodes.set(c.building.getX(), c.building.getY(), c.node);
          // reproduce
          Bag neighbors = new Bag();
          yellowFever.getRegion().getNeighborsHamiltonianDistance(c.building.getX(), c.building.getY(), 1, false,
              neighbors, null, null);

          for (Object o : neighbors) {
            Building building = (Building) o;
            // Location l = (Location) o;
            if (building == c.building) {
              continue;
            }
            Crawler newc = new Crawler(c.node, building);
            nextGeneration.add(newc);
          }
        }
        // otherwise just die
      }
      crawlers = nextGeneration;
    }
    return closestNodes;
  }

  private void populateRefugee(MersenneTwisterFast random, YellowFever yellowFever) {
    double teta = 0.3;
    int totalAgents = NUMBERS_OF_AGENTS;
    double[] prop = { 0.30, 0.12, 0.11, 0.13, 0.12, 0.10, 0.06, 0.03, 0.01, 0.01, 0.01 }; // proportion of household
    int[] size = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // family size - all are zero
    int count = 0;
    int remaining = 0;
    int curTot = 0;

    for (int i = 0; i < size.length; i++) {
      double x = prop[i] * totalAgents * teta;
      int hh = (int) Math.round(x);
      size[i] = hh;
      curTot = curTot + ((i + 1) * hh);
    }

    if (curTot > totalAgents) {
      size[0] = size[0] - (curTot - totalAgents);
    }

    if (curTot < totalAgents) {
      size[0] = size[0] + (totalAgents - curTot);
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
    for (int a = 0; a < sizeDist.length; a++) {
      int counter = 0;
      int tot = sizeDist[a];
      counter = counter + tot;
      if (tot != 0 && counter <= totalAgents) {
        /* random searching of next parcel to populate houses */
        Building building = nextAvailCamp(yellowFever);
        Family hh = new Family(building);
        yellowFever.getAllFamilies().add(hh);
        // TODO: Verifica a necessidade deste atributo sobre o agente
        // hh.setWaterAtHome(tot * dadaab.params.global.getMaximumWaterRequirement() +
        // (1.5 * dadaab.params.global.getMaximumWaterRequirement() *
        // dadaab.random.nextDouble()));

        // TODO: Verifica a necessidade deste atributo sobre o agente
        // hh.setRationDate(1 + a % 9);
        // if (dadaab.random.nextDouble() > dadaab.params.global.getLaterineCoverage())
        // {
        // hh.setHasLaterine(true);
        // }

        building.addHumanHH(hh);

        int age = this.defineAge(tot, yellowFever);

        int sex = this.defineSex(yellowFever);

        addAllRefugees(age, sex, hh, random, yellowFever);
      }
    }
  }

  private int defineSex(YellowFever yellowFever2) {
    int sex = 0; // sex 50-50 chanceF
    if (yellowFever.random.nextDouble() > 0.5) {
      sex = 1;
    } else {
      sex = 2;
    }
    return sex;
  }

  public int defineAge(int tot, YellowFever yellowFever) {
    double rn = yellowFever.random.nextDouble();
    int age = 0;
    for (int i = 0; i < tot; i++) {
      // a household head need to be between 18-59;
      if (i == 0) {
        age = 18 + yellowFever.random.nextInt(42); // 18-59
      } else {
        if (rn <= 0.1) {
          age = 1 + yellowFever.random.nextInt(4); // 1-4 age
        } else if (rn > 0.1 && rn <= 0.40) {
          age = 5 + yellowFever.random.nextInt(7); // 5=11

        } else if (rn > 0.40 && rn <= 0.57) {
          age = 12 + yellowFever.random.nextInt(6); // 11-17
        } else if (rn > 0.57 && rn <= 0.97) {
          age = 18 + yellowFever.random.nextInt(42); // 18-59
        } else {
          age = 60 + yellowFever.random.nextInt(40); // 60 +
        }
      }
    }
    return age;
  }

  private static void addAllRefugees(int age, int sex, Family hh, MersenneTwisterFast random, YellowFever yellowFever) {
    Human human = new Human(age, sex, hh, hh.getLocation(), hh.getLocation(), random, yellowFever.getAllHumans());
    hh.addMember(human);
    hh.getLocation().addHuman(human);
    // TODO: Quais outros atribuitos iram existir?
    // newRefugee.setBodyResistance(1);
    // newRefugee.setHealthStatus(1);
    // newRefugee.setCurrentActivity(0);
    // human.setWaterLevel(2 * dadaab.params.global.getMinimumWaterRequirement()
    // + dadaab.params.global.getMaximumWaterRequirement() * random.nextDouble());

    // TODO: Quais outros atribuitos iram existir?
    // double ratioInfected = (dadaab.params.global.getPercentageOfAsymptomatic() /
    // (100));
    // double ageEffect = 0.5 + 0.5 * (Math.pow(human.getAge(), 2) / (Math.pow(60,
    // 2.5)));

    // TODO: Quais outros atribuitos iram existir?
    // if (dadaab.random.nextDouble() > ratioInfected * ageEffect) {
    // newRefugee.setSymtomaticType(1);// symtotic
    // } else {
    // newRefugee.setSymtomaticType(2); // asymptotic
    // }
    int study = 0;
    if (age >= 5 && age < 15) {
      if (yellowFever.random.nextDouble() > 0.56) {
        study = 1;
      } else
        study = 0;
    } else {
      study = 0;
    }
    // TODO: Quais outros atribuitos iram existir?
    // human.setStudyID(study);
    human.setStopper(yellowFever.schedule.scheduleRepeating(human, Human.getOrdering(), 1.0));
  }

  public static Building nextAvailCamp(YellowFever yellowFever) {
    int x = yellowFever.random.nextInt(yellowFever.getRegionSites().numObjs);
    while (((Building) yellowFever.getRegionSites().objs[x]).isCampOccupied(yellowFever) == true
        || yellowFever.getAllFacilities().contains((Building) yellowFever.getRegionSites().objs[x]) == true) {
      x = yellowFever.random.nextInt(yellowFever.getRegionSites().numObjs);
    }
    return (Building) yellowFever.getRegionSites().objs[x];

  }

  static class Node {

    Building building;
    ArrayList<Edge> links;

    public Node(Building b) {
      building = b;
      links = new ArrayList<Edge>();
    }
  }

  static class Crawler {

    Node node;
    Building building;

    public Crawler(Node n, Building b) {
      node = n;
      building = b;
    }
  }

}
