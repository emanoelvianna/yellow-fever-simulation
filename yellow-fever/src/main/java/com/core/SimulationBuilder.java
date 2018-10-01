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

import com.model.Facility;
import com.model.Family;
import com.model.Building;
import com.model.Human;
import com.model.Mosquito;
import com.model.enumeration.ActivityMapping;
import com.model.enumeration.HealthStatus;
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

  static int gridWidth = 0;
  static int gridHeight = 0;

  public void create(String campfile, String facilityfile, String roadfile, YellowFever dadaab,
      MersenneTwisterFast random) {
    // CampBuilder.random = random;
    try {

      // buffer reader - read ascii file
      BufferedReader camp = new BufferedReader(new FileReader(campfile));
      String line;

      // first read the dimensions
      line = camp.readLine(); // read line for width
      String[] tokens = line.split("\\s+");
      int width = Integer.parseInt(tokens[1]);
      gridWidth = width;

      line = camp.readLine();
      tokens = line.split("\\s+");
      int height = Integer.parseInt(tokens[1]);
      gridHeight = height;

      createGrids(width, height, dadaab);

      // skip the next four lines as they contain irrelevant metadata

      for (int i = 0; i < 4; ++i) {
        line = camp.readLine();
      }

      dadaab.getFamilyHousing().clear();// clear the bag

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
              dadaab.getFamilyHousing().add(fieldUnit);
            }

            if (camptype >= 10 && camptype <= 12) {
              fieldUnit.setCampID(1);
            } else if (camptype >= 20 && camptype <= 22) {
              fieldUnit.setCampID(2);
            }

            else if (camptype >= 30 && camptype <= 32) {
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
          dadaab.allCamps.field[curr_col][curr_row] = fieldUnit;

        }

      }

      // read elev and change camp locations id to elev
      InputStream inputStream = new FileInputStream(new File("data/d_camp_a.txt"));
      Importer.read(inputStream, GridDataType.INTEGER, dadaab.allCampGeoGrid);
      // overwrite the file and make 100

      // now read facility grid
      BufferedReader fac = new BufferedReader(new FileReader(facilityfile));
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

            Facility facility = new Facility();
            Building facilityField = (Building) dadaab.allCamps.get(curr_col, curr_row);
            facility.setLocation(facilityField);
            facilityField.setFacility(facility);
            dadaab.getAllFacilities().add(facilityField);

            if (facilitytype == 1) {
              facility.setFacilityID(2);
              dadaab.getWorks().add(facilityField);
            } else if (facilitytype == 2 || facilitytype == 3) {
              facility.setCapacity(dadaab.getParams().getGlobal().getResourcesInMedicalCenters());
              facility.setFacilityID(6);
              dadaab.getHealthCenters().add(facilityField);
            } else if (facilitytype == 4) {
              facility.setFacilityID(5);
              dadaab.getFoodCenter().add(facilityField);
            } else if (facilitytype > 5 && facilitytype <= 8) {
              facility.setFacilityID(1);
              dadaab.getSchooles().add(facilityField);
            } else if (facilitytype == 9) {
              facility.setFacilityID(4);
              dadaab.getMarket().add(facilityField);
            } else if (facilitytype == 10) {
              facility.setFacilityID(3);
              dadaab.getMosques().add(facilityField);
            } else {
              facility.setFacilityID(8);
              dadaab.getOther().add(facilityField);
            }

            dadaab.facilityGrid.setObjectLocation(facility, curr_col, curr_row);
          }
        }
      }

      // now read road grid

      BufferedReader road = new BufferedReader(new FileReader(roadfile));

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
            dadaab.roadGrid.set(curr_col, curr_row, roadID);

          }
        }
      }

      // now read elev file and store in bag
      BufferedReader elev = new BufferedReader(new FileReader("data/d_dem_n.txt"));

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

            Building elevationField = (Building) dadaab.allCamps.get(curr_col, curr_row);
            elevationField.setElevation(elevation);

          }
        }

      }

      // read shape file

      Bag maskedCamp = new Bag();
      maskedCamp.add("CAMPID");
      File file = new File("data/Camp_n.shp");
      URL campShapUL = file.toURL();

      ShapeFileImporter.read(campShapUL, dadaab.campShape, maskedCamp);

      Bag masked = new Bag();

      // ShapeFileImporter importer = new ShapeFileImporter();
      File file2 = new File("data/dadaab_road_f_node.shp");
      URL raodLinkUL = file2.toURL();
      ShapeFileImporter.read(raodLinkUL, dadaab.roadLinks, masked);

      extractFromRoadLinks(dadaab.roadLinks, dadaab); // construct a network of
                                                      // roads

      // set up the locations and nearest node capability

      dadaab.closestNodes = setupNearestNodes(dadaab);

    } catch (IOException ex) {
      Logger.getLogger(SimulationBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }

    this.populateRefugee(dadaab);
    this.populateMosquito(dadaab);
    // random
    int max = dadaab.getParams().getGlobal().getMaximumNumberRelative();
    int[] numberOfFamilies = new int[dadaab.getAllFamilies().numObjs];

    for (int i = 0; i < dadaab.getAllFamilies().numObjs; i++) {

      Family f = (Family) dadaab.getAllFamilies().objs[i];
      int tot = 0;
      if (dadaab.getAllFamilies().numObjs > max) {
        tot = max;
      }

      else
        tot = dadaab.getAllFamilies().numObjs;

      int numOfRel = 1 + dadaab.random.nextInt(tot - 1);

      // swap the array index
      for (int kk = 0; kk < numberOfFamilies.length; kk++) {
        int idx = dadaab.random.nextInt(numberOfFamilies.length);
        int temp = numberOfFamilies[idx];
        numberOfFamilies[idx] = numberOfFamilies[i];
        numberOfFamilies[i] = temp;
      }

      for (int jj = 0; jj < numOfRel; jj++) {
        if (f.equals((Family) dadaab.getAllFamilies().objs[numberOfFamilies[jj]]) != true) {
          Building l = ((Family) dadaab.getAllFamilies().objs[numberOfFamilies[jj]]).getCampLocation();
          f.addRelative(l);
        }
      }

    }

    // read climate file
    String line;
    String divider = ",";
    BufferedReader buffered = null;
    try {
      buffered = new BufferedReader(new FileReader("data-poa/clima-2012-2014.csv"));
      // skip the first line
      buffered.readLine();
      while ((line = buffered.readLine()) != null) {
        String[] info = line.split(divider);
        dadaab.getClimate().addDate(info[0]);
        dadaab.getClimate().addPrecipitation(Double.parseDouble(info[1]));
        dadaab.getClimate().addTemperature(Double.parseDouble(info[2]));
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

    this.generateRandomHumansInfected(dadaab);
    this.administerRandomVaccines(dadaab);

    System.out.println("---");
    int quantidadeInicialDeHumanos = dadaab.getParams().getGlobal().getInitialHumansNumber();
    System.out.println("Quantidade de humanos adicionadas: " + quantidadeInicialDeHumanos);
    int quantidadeInicialDeMosquitos = dadaab.getParams().getGlobal().getInitialMosquitoesNumber();
    System.out.println("Quantidade de mosquitos adicionadas: " + quantidadeInicialDeMosquitos);
    int quantidadeInicialDeMosquitosNasCasa = 0;
    for (Object object : dadaab.getFamilyHousing()) {
      Building fieldUnit = (Building) object;
      quantidadeInicialDeMosquitosNasCasa += fieldUnit.getMosquitoes().size();
    }
    System.out.println("Quantidade de mosquitos nas residencias: " + quantidadeInicialDeMosquitosNasCasa);

    int quantidadeInicialDeHumanosNasCasa = 0;
    for (Object object : dadaab.getFamilyHousing()) {
      Building fieldUnit = (Building) object;
      quantidadeInicialDeHumanosNasCasa += fieldUnit.getHumans().size();
    }
    System.out.println("Quantidade de humanos nas residencias: " + quantidadeInicialDeHumanosNasCasa);

    int infectados = 0;
    for (Object object : dadaab.allHumans.getAllObjects()) {
      Human human = (Human) object;
      if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
        infectados++;
      }
    }
    System.out.println("Quantidade de pessoas expostas: " + infectados);
    System.out.println("---");

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
  private void addAllRefugees(int age, int sex, Family hh, YellowFever dadaab) {
    Human human = new Human(age, sex, hh, hh.getCampLocation(), hh.getCampLocation(), dadaab.random, dadaab.allHumans);
    hh.addMembers(human);
    hh.getCampLocation().addRefugee(human);
    human.setCurrentHealthStatus(HealthStatus.SUSCEPTIBLE);
    human.setCurrentActivity(ActivityMapping.STAY_HOME);
    human.setStudent(this.isStudent(age));
    human.setWorker(this.isWorker(age));
    human.setStoppable(dadaab.schedule.scheduleRepeating(human, Human.ORDERING, 1.0));
  }

  // TODO: Rever a faixa de idade de estudante
  // TODO: Considerar uma faixa como estudante universitário
  private boolean isStudent(int age) {
    if (age < 25) {
      return true;
    } else {
      return false;
    }
  }

  // TODO: Está feixa de idade de trabalhador está no texto?
  private boolean isWorker(int age) {
    if (age >= 25 && age <= 65) {
      return true;
    } else {
      return false;
    }
  }

  // random searching of next parcel to populate houses
  public static Building nextAvailCamp(YellowFever dadaab) {
    // for now random
    int index = dadaab.random.nextInt(dadaab.getFamilyHousing().numObjs);
    while (((Building) dadaab.getFamilyHousing().objs[index]).isCampOccupied(dadaab) == true
        || dadaab.getAllFacilities().contains((Building) dadaab.getFamilyHousing().objs[index]) == true) {
      // try another spot
      index = dadaab.random.nextInt(dadaab.getFamilyHousing().numObjs);
    }
    return (Building) dadaab.getFamilyHousing().objs[index];
  }

  // create refugees - first hh
  private void populateRefugee(YellowFever dadaab) {

    // UNHCR stat
    // age distibution
    // 1-4 = 0.20; 5-11 = 0.25; 12-17 = 0.12; 18-59 = 0.40;>= 60 = 0.;

    // family size
    // 1 = 30% , 2 =12% , 3 = 11%, 4=13%, 5 =12%, 6 = 10%, >6= 12%

    // proportion of teta = families/ total population = 8481/29772 ~ 0.3

    //
    double teta = 0.3;
    int totalRef = dadaab.getParams().getGlobal().getInitialHumansNumber();
    // prprtion of hh to total size
    // System.out.println("s: " + totalRef);

    // int hhsize = (totalRef * teta /10 ) +

    double[] prop = { 0.30, 0.12, 0.11, 0.13, 0.12, 0.10, 0.06, 0.03, 0.01, 0.01, 0.01 }; // proportion
                                                                                          // of
                                                                                          // household
    int[] size = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // family size - all are
                                                      // zero
    // family size ranges from 1 to 11

    int count = 0;
    int rem = 0;// remaining
    int curTot = 0;

    for (int i = 0; i < size.length; i++) {
      double x = prop[i] * totalRef * teta;
      int hh = (int) Math.round(x);
      size[i] = hh;
      curTot = curTot + ((i + 1) * hh);

    }

    if (curTot > totalRef) {
      size[0] = size[0] - (curTot - totalRef);
    }

    if (curTot < totalRef) {
      size[0] = size[0] + (totalRef - curTot);
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

    // reshuffle position
    // Collections.shuffle(Arrays.asList(sizeDist)); // does not work

    // swaping with random posiion
    for (int i = 0; i < sizeDist.length; i++) {

      int change = i + dadaab.random.nextInt(sizeDist.length - i);
      int holder = sizeDist[i];
      sizeDist[i] = sizeDist[change];
      sizeDist[change] = holder;

      // System.out.println ("hh size: "+ sizeDist[i]);
    }

    // initialize household

    // UNHCR stat
    // age distibution
    // 1-4 = 0.20; 5-11 = 0.25; 12-17 = 0.12; 18-59 = 0.40;>= 60 = 0.03;

    for (int a = 0; a < sizeDist.length; a++) {
      int counter = 0;

      int tot = sizeDist[a];
      counter = counter + tot;
      if (tot != 0 && counter <= totalRef) {
        Building fieldUnit = nextAvailCamp(dadaab);
        Family hh = new Family(fieldUnit);
        dadaab.getAllFamilies().add(hh);
        fieldUnit.addRefugeeHH(hh);

        // TODO: Justificativa está considerando a arborização
        if (dadaab.random.nextDouble() < 0.5) { // 50% chance to contains nectar
          fieldUnit.setNectar(true);
        }
        if (dadaab.random.nextDouble() < 0.5) { // 50% chance to contains sap
          fieldUnit.setSap(true);
        }

        double rn = dadaab.random.nextDouble();
        int age = 0;
        for (int i = 0; i < tot; i++) {
          if (i == 0) {
            // a household head need to be between 18-59;
            age = 18 + dadaab.random.nextInt(42);
          } else {
            if (rn <= 0.1) {
              age = 5 + dadaab.random.nextInt(14); // 20% chance the age between
                                                   // 5-19
            } else if (rn > 0.57 && rn <= 0.97) {
              age = 20 + dadaab.random.nextInt(14); // 40% chance the age
                                                    // between 20-34
            } else if (rn > 0.1 && rn <= 0.40) {
              age = 35 + dadaab.random.nextInt(14); // 25% chance the age
                                                    // between 35-49
            } else if (rn > 0.40 && rn <= 0.57) {
              age = 50 + dadaab.random.nextInt(14); // 12% chance the age
                                                    // between 50-64
            } else {
              age = 65 + dadaab.random.nextInt(25); // 3% chance the age between
                                                    // 65-90
            }
          }
          int sex = this.defineSex(dadaab);
          addAllRefugees(age, sex, hh, dadaab);
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

  public int defineSex(YellowFever dadaab) {
    // sex 50-50 chance
    if (dadaab.random.nextDouble() > 0.5) {
      return 1;
    } else {
      return 2;
    }
  }

  /// raod network methods from haiti project
  static void extractFromRoadLinks(GeomVectorField roadLinks, YellowFever dadaab) {
    Bag geoms = roadLinks.getGeometries();
    Envelope e = roadLinks.getMBR();
    double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
    int xcols = gridWidth - 1, ycols = gridHeight - 1;

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

      if (xint >= gridWidth) {
        continue;
      } else if (yint >= gridHeight) {
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

    ObjectGrid2D closestNodes = new ObjectGrid2D(gridWidth, gridHeight);
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
