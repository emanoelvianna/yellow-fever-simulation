package com.core;

import com.model.Facility;
import com.model.Family;
import com.model.Human;
import com.model.Rainfall;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;
import sim.util.Bag;

public class YellowFever extends SimState {

  private static final long serialVersionUID = 8374233360244345303L;

  private ObjectGrid2D region;
  private GeomGridField regionGeoGrid;
  private DoubleGrid2D rainfallGrid;
  private Continuous2D allHumans;
  private SparseGrid2D facilityGrid;
  private IntGrid2D roadGrid;
  private GeomVectorField regioShape;
  private GeomVectorField roadShape;
  private SparseGrid2D nodes;
  private ObjectGrid2D closestNodes;

  private Network roadNetwork = new Network();
  private Rainfall rainfall; // scheduling rainfall
  private Facility facility;// schduling borehole refill

  private Bag allFamilies;
  private Bag regionSites;
  private Bag rainfallWater;
  private Bag allFacilities;
  private Bag schooles;
  private Bag healthCenters;
  private Bag mosques;
  private Bag market;
  private Bag foodCenter;
  private Bag other;

  private int[] dailyRain = new int[365];
  private int[] totalActivity;
  private int[] campSuscpitable;
  private int[] campExposed;
  private int[] campInfected;
  private int[] campRecovered;
  private int PrevPop = 0;
  private int curPop = 0;

  public YellowFever(long seed) {
    super(seed);
    rainfall = new Rainfall();
    facility = new Facility();//
    allFamilies = new Bag();
    regionSites = new Bag();
    // TODO: Verifica a necessidade deste atributo
    // boreHoles = new Bag();
    rainfallWater = new Bag();
    allFacilities = new Bag();
    regionGeoGrid = new GeomGridField();

    schooles = new Bag();
    healthCenters = new Bag();
    mosques = new Bag();
    market = new Bag();
    foodCenter = new Bag();
    other = new Bag();

    setTotalActivity(new int[10]);
    campSuscpitable = new int[3];
    campExposed = new int[3];
    campInfected = new int[3];
    campRecovered = new int[3];
  }

  public static void main(String[] args) {
    doLoop(YellowFever.class, args);
    System.exit(0);
  }

  @Override
  public void start() {
    BuildRegion buildRegion = new BuildRegion();
    buildRegion.create(this);
    
    schedule.scheduleRepeating(rainfall, Rainfall.getOrdering(), 1);
    schedule.scheduleRepeating(facility, Facility.getOrdering(), 1);

    Steppable chartUpdater = new Steppable() {
      // all graphs and charts wll be updated in each steps
      public void step(SimState state) {

        Bag ref = allHumans.getAllObjects(); // getting all refugees
        //
        int[] sumAct = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // adding each activity and puting the value in array

        int[] sumAge = { 0, 0, 0, 0, 0 }; // adding agent all agents whose age falls in a given age-class

        int[] sumfamSiz = { 0, 0, 0, 0, 0, 0, 0 }; // adding all agent families based o their family size

        int totalSus = 0; // total suscibtible
        int totalExp = 0;
        int totalInf = 0; // total infected
        int totalRec = 0; // total recovered

        int totalSusNewly = 0; // total suscibtible
        int totalExpNewly = 0;
        int totalInfNewly = 0; // total infected
        int totalRecNewly = 0; // total recovered

        // by camp
        int totSusDag = 0;
        int totExpDag = 0;
        int totInfDag = 0;
        int totRecDag = 0;

        int totSusInfo = 0;
        int totExpInfo = 0;
        int totInfInfo = 0;
        int totRecInfo = 0;

        int totSusHag = 0;
        int totExpHag = 0;
        int totInfHag = 0;
        int totRecHag = 0;

        // accessing all families and chatagorize them based on their size
        for (int i = 0; i < allFamilies.numObjs; i++) {
          Family f = (Family) allFamilies.objs[i];
          // killrefugee(f);

          int siz = 0;
          if (f.getMembers().numObjs > 6) { // aggregate all families of >6 family size
            siz = 6;
          } else {
            siz = f.getMembers().numObjs - 1;

          }
          sumfamSiz[siz] += 1;
        }
        int none = 0;
        // accessing each agent
        for (int i = 0; i < ref.numObjs; i++) {
          Human human = (Human) ref.objs[i];
          sumAct[human.getCurrentActivity()] += 1; // current activity
          int age = ageClass(human.getAge()); // age class of agent i
          // int siz = 0;
          sumAge[age] += 1;

          if (human.getHome().getType() == 1) {
            if (human.getHealthStatus() == 1) {
              totSusDag = totSusDag + 1;
            } else if (human.getHealthStatus() == 2) {
              totExpDag = totExpDag + 1;
            } else if (human.getHealthStatus() == 3) {
              totInfDag = totInfDag + 1;
            } else if (human.getHealthStatus() == 4) {
              totRecDag = totRecDag + 1;
            } else {
              none = 0;
            }
          }

          if (human.getHome().getType() == 2) {
            if (human.getHealthStatus() == 1) {
              totSusInfo = totSusInfo + 1;
            } else if (human.getHealthStatus() == 2) {
              totExpInfo = totExpInfo + 1;
            } else if (human.getHealthStatus() == 3) {
              totInfInfo = totInfInfo + 1;
            } else if (human.getHealthStatus() == 4) {
              totRecInfo = totRecInfo + 1;
            } else {
              none = 0;
            }

          }

          if (human.getHome().getType() == 3) {
            if (human.getHealthStatus() == 1) {
              totSusHag = totSusHag + 1;
            } else if (human.getHealthStatus() == 2) {
              totExpHag = totExpHag + 1;

            } else if (human.getHealthStatus() == 3) {
              totInfHag = totInfHag + 1;
            } else if (human.getHealthStatus() == 4) {
              totRecHag = totRecHag + 1;
              ;
            } else {
              none = 0;
            }
          }

          // total health status
          if (human.getHealthStatus() == 1) {
            totalSus = totalSus + 1;
          } else if (human.getHealthStatus() == 2) {
            totalExp = totalExp + 1;
          } else if (human.getHealthStatus() == 3) {
            totalInf = totalInf + 1;
          } else if (human.getHealthStatus() == 4) {
            totalRec = totalRec + 1;
          } else {
            none = 0;
          }

          if (human.getHealthStatus() != human.getPrevHealthStatus()) {
            if (human.getHealthStatus() == 1) {
              totalSusNewly = totalSusNewly + 1;
            } else if (human.getHealthStatus() == 2) {
              totalExpNewly = totalExpNewly + 1;
            } else if (human.getHealthStatus() == 3) {
              totalInfNewly = totalInfNewly + 1;
            } else if (human.getHealthStatus() == 4) {
              totalRecNewly = totalRecNewly + 1;
            } else {
              none = 0;
            }
          }

        }

        campSuscpitable[0] = totSusDag;
        campSuscpitable[1] = totSusInfo;
        campSuscpitable[2] = totSusHag;

        campExposed[0] = totExpDag;
        campExposed[1] = totExpInfo;
        campExposed[2] = totExpHag;

        campInfected[0] = totInfDag;
        campInfected[1] = totInfInfo;
        campInfected[2] = totInfHag;

        campRecovered[0] = totRecDag;
        campRecovered[1] = totRecInfo;
        campRecovered[2] = totRecHag;

        setTotalActivity(sumAct); // set activity array output

        String actTitle = "Activity"; // row key - activity
        String[] activities = new String[] { "At Home", "School", "Water", "Mosque", "Market", "Food C.", "Health C.",
            "Visit R.", "Social", "Hygiene" };

        String ageTitle = "Age Group";
        String[] ageC = new String[] { "1-4", "5-11", "12-17", "18-60", "60 +" };

        String famTitle = "Household Size";
        String[] famC = new String[] { "1", "2", "3", "4", "5", "6", "6+" };

        int totDead = countDeath();
        int m = ((int) state.schedule.time()) % 60;
      }
    };

    schedule.scheduleRepeating(chartUpdater);
    // System.out.println("total:- "+ allRefugees.getAllObjects().numObjs);
  }

  /* age class or ageset */
  private int ageClass(int age) {
    int a = 0;
    if (age < 5) {
      a = 0;
    } else if (age >= 5 && age < 12) {
      a = 1;
    } else if (age >= 12 && age < 18) {
      a = 2;
    } else if (age >= 18 && age < 60) {
      a = 3;
    } else {
      a = 4;
    }
    return a;
  }

  public int countDeath() {
    int death = 0;

    int current = allHumans.getAllObjects().numObjs;
    PrevPop = curPop;
    death = PrevPop - current;
    curPop = current;
    if (death < 0) {
      death = 0;
    }
    return death;
  }

  public ObjectGrid2D getRegion() {
    return region;
  }

  public void setRegion(ObjectGrid2D region) {
    this.region = region;
  }

  public DoubleGrid2D getRainfallGrid() {
    return rainfallGrid;
  }

  public void setRainfallGrid(DoubleGrid2D rainfallGrid) {
    this.rainfallGrid = rainfallGrid;
  }

  public Continuous2D getAllHumans() {
    return allHumans;
  }

  public void setAllHumans(Continuous2D allHumans) {
    this.allHumans = allHumans;
  }

  public SparseGrid2D getFacilityGrid() {
    return facilityGrid;
  }

  public void setFacilityGrid(SparseGrid2D facilityGrid) {
    this.facilityGrid = facilityGrid;
  }

  public IntGrid2D getRoadGrid() {
    return roadGrid;
  }

  public void setRoadGrid(IntGrid2D roadGrid) {
    this.roadGrid = roadGrid;
  }

  public GeomVectorField getRoadShape() {
    return roadShape;
  }

  public void setRoadShape(GeomVectorField roadShape) {
    this.roadShape = roadShape;
  }

  public GeomVectorField getRegionShape() {
    return regioShape;
  }

  public void setRegioShape(GeomVectorField regioShape) {
    this.regioShape = regioShape;
  }

  public SparseGrid2D getNodes() {
    return nodes;
  }

  public void setNodes(SparseGrid2D nodes) {
    this.nodes = nodes;
  }

  public ObjectGrid2D getClosestNodes() {
    return closestNodes;
  }

  public void setClosestNodes(ObjectGrid2D closestNodes) {
    this.closestNodes = closestNodes;
  }

  public GeomGridField getRegionGeoGrid() {
    return regionGeoGrid;
  }

  public void setRegionGeoGrid(GeomGridField regionGeoGrid) {
    this.regionGeoGrid = regionGeoGrid;
  }

  public Bag getRegionSites() {
    return regionSites;
  }

  public void setRegionSites(Bag regionSites) {
    this.regionSites = regionSites;
  }

  public Bag getRainfallWater() {
    return rainfallWater;
  }

  public void setRainfallWater(Bag rainfallWater) {
    this.rainfallWater = rainfallWater;
  }

  public Bag getAllFacilities() {
    return allFacilities;
  }

  public void setAllFacilities(Bag allFacilities) {
    this.allFacilities = allFacilities;
  }

  public Bag getSchooles() {
    return schooles;
  }

  public void setSchooles(Bag schooles) {
    this.schooles = schooles;
  }

  public Bag getHealthCenters() {
    return healthCenters;
  }

  public void setHealthCenters(Bag healthCenters) {
    this.healthCenters = healthCenters;
  }

  public Bag getMosques() {
    return mosques;
  }

  public void setMosques(Bag mosques) {
    this.mosques = mosques;
  }

  public Bag getMarket() {
    return market;
  }

  public void setMarket(Bag market) {
    this.market = market;
  }

  public Bag getFoodCenter() {
    return foodCenter;
  }

  public void setFoodCenter(Bag foodCenter) {
    this.foodCenter = foodCenter;
  }

  public Bag getOther() {
    return other;
  }

  public void setOther(Bag other) {
    this.other = other;
  }

  public int[] getDailyRain() {
    return dailyRain;
  }

  public void setDailyRain(int[] dailyRain) {
    this.dailyRain = dailyRain;
  }

  public Network getRoadNetwork() {
    return roadNetwork;
  }

  public void setRoadNetwork(Network roadNetwork) {
    this.roadNetwork = roadNetwork;
  }

  public Bag getAllFamilies() {
    return allFamilies;
  }

  public void setAllFamilies(Bag allFamilies) {
    this.allFamilies = allFamilies;
  }

  public int[] getTotalActivity() {
    return totalActivity;
  }

  public void setTotalActivity(int[] totalActivity) {
    this.totalActivity = totalActivity;
  }

}
