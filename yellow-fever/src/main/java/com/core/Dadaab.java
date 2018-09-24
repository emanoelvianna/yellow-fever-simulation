package com.core;

import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.xy.XYSeries;

import com.core.enumeration.Parameters;
import com.model.Climate;
import com.model.Facility;
import com.model.Family;
import com.model.FieldUnit;
import com.model.Human;
import com.model.Mosquito;
import com.model.enumeration.HealthStatus;

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
/**
 *
 * @author gmu
 */
import sim.util.Bag;

public class Dadaab extends SimState {

  public ObjectGrid2D allCamps; // The model environment - holds fields (
                                // parcels)
  public GeomGridField allCampGeoGrid;
  public DoubleGrid2D rainfallGrid; // mainly for rainfall vizualization
  public Continuous2D allHumans; // refugee agents
  public SparseGrid2D facilityGrid;// facilities: schools, health center,
                                   // borehol etc
  public IntGrid2D roadGrid; // road in grid- for navigation
  public GeomVectorField roadLinks;
  public GeomVectorField campShape;
  public SparseGrid2D nodes;
  public ObjectGrid2D closestNodes; // the road nodes closest to each of the //
                                    // locations
  Network roadNetwork = new Network();

  private final Parameters params;
  private int totalSusciptible;
  private int totalExposed;
  private int totalOfHumansWithMildInfection;
  private int totalOfHumansWithSevereInfected;
  private int totalOfMosquitoWithInfection;
  private int totalInfected;
  private int totalRecovered;
  private int totalSusciptibleNewly;
  private int totalExposedNewly;
  private int totalInfectedNewly;
  private int totalRecoveredNewly;
  public int[] campSuscpitable;
  public int[] campExposed;
  public int[] campInfected;
  public int[] campRecovered;
  private int[] totalActivity;
  private double totalBacterialLoad = 0;
  private int currentDay;
  private double temperature;

  /**
   * charts and graphs
   */
  // agent health status
  private static final long serialVersionUID = -5966446373681187141L;
  public XYSeries totalsusceptibleSeries = new XYSeries("Susceptible"); // shows
                                                                        // number
                                                                        // of
                                                                        // Susceptible
                                                                        // agents
  public XYSeries totalExposedSeries = new XYSeries("Exposed");
  public XYSeries totalInfectedSeries = new XYSeries(" Infected"); // shows
                                                                   // number of
                                                                   // infected
                                                                   // agents
  public XYSeries totalRecoveredSeries = new XYSeries(" Recovered"); // shows
                                                                     // number
                                                                     // of
                                                                     // recovered
                                                                     // agents

  public XYSeries rainfallSeries = new XYSeries(" Rainfall"); //
  public XYSeries totalsusceptibleSeriesNewly = new XYSeries("Newly Susceptible"); // shows
                                                                                   // number
                                                                                   // of
                                                                                   // Newly
                                                                                   // Susceptible
                                                                                   // agents
  public XYSeries totalExposedSeriesNewly = new XYSeries("Newly Exposed");
  public XYSeries totalInfectedSeriesNewly = new XYSeries("Newly Infected"); // shows
                                                                             // number
                                                                             // of
                                                                             // Newly
                                                                             // infected
                                                                             // agents
  public XYSeries totalRecoveredSeriesNewly = new XYSeries("Newly Recovered"); // shows
                                                                               // number
                                                                               // of
                                                                               // Newly
                                                                               // recovered
                                                                               // agents
  public XYSeries totalTotalPopSeries = new XYSeries(" Total"); // shows number
                                                                // of dead
                                                                // agents
  public XYSeries totalDeathSeries = new XYSeries(" Death"); // shows number of
                                                             // dead agents

  public XYSeries totalBacteriaLoadSeries = new XYSeries(" Total vibrio Cholerae /million"); // shows
                                                                                             // number
                                                                                             // of
                                                                                             // recovered
                                                                                             // agents
  // private static final long serialVersionUID = -5966446373681187141L;
  public DefaultCategoryDataset dataset = new DefaultCategoryDataset(); //
  public DefaultCategoryDataset agedataset = new DefaultCategoryDataset();// shows
                                                                          // age
                                                                          // structure
                                                                          // of
                                                                          // agents
  public DefaultCategoryDataset familydataset = new DefaultCategoryDataset(); // shows
                                                                              // family
                                                                              // size
  // timer graphics
  public DefaultValueDataset hourDialer = new DefaultValueDataset(); // shows
                                                                     // the
                                                                     // current
                                                                     // hour
  public DefaultValueDataset dayDialer = new DefaultValueDataset(); // counts
  public int totalgridWidth = 10;
  public int totalgridHeight = 10;

  public Bag allFamilies; // holding all families
  private Bag allMosquitoes;
  public Bag familyHousing;
  public Bag boreHoles; // holds borehols
  public Bag rainfallWater; // water points from rain
  public Bag allFacilities;
  public Bag schooles;
  public Bag healthCenters;
  public Bag mosques;
  public Bag market;
  public Bag foodCenter;
  public Bag other;
  // WaterContamination fm = new WaterContamination();
  Rainfall rainfall; // scheduling rainfall
  Facility fac;// schduling borehole refill
  private Climate climate;

  private TimeManager time = new TimeManager();

  public DadaabObserver dObserver;
  int[] sumActivities = { 0, 0, 0, 0, 0, 0, 0, 0 }; //
  int[] dailyRain = new int[365];

  public Dadaab(long seed, String[] args) {
    super(seed);
    params = new Parameters(args);
    rainfall = new Rainfall();
    fac = new Facility();//
    allFamilies = new Bag();
    allMosquitoes = new Bag();
    familyHousing = new Bag();
    boreHoles = new Bag();
    rainfallWater = new Bag();
    allFacilities = new Bag();
    allCampGeoGrid = new GeomGridField();
    this.climate = new Climate();
    this.currentDay = 0;
    this.temperature = 21; // TODO: Refatorar

    schooles = new Bag();
    healthCenters = new Bag();
    mosques = new Bag();
    market = new Bag();
    foodCenter = new Bag();
    other = new Bag();

    totalActivity = new int[10];
    campSuscpitable = new int[3];
    campExposed = new int[3];
    campInfected = new int[3];
    campRecovered = new int[3];
  }

  // Boolean getOutputStats = true;
  public void start() {
    super.start();
    // accessing inpt files
    CampBuilder builder = new CampBuilder();
    builder.create("data/d_camp_a.txt", "data/d_faci_a.txt", "data/d_costp_a.txt", this, this.random);

    schedule.scheduleRepeating(rainfall, rainfall.ORDERING, 1);
    schedule.scheduleRepeating(fac, fac.ORDERING, 1);

    // if (getOutputStats ==true){
    dObserver = new DadaabObserver(this);
    schedule.scheduleRepeating(dObserver, DadaabObserver.ORDERING, 1.0);
    // }
    // updating chart information
    Steppable chartUpdater = new Steppable() {

      // all graphs and charts wll be updated in each steps
      public void step(SimState state) {

        if (this.isNewDay()) {
          this.setTemperature();
          this.eggsIsReadyToHatch();
        }

        Bag humans = allHumans.getAllObjects(); // getting all refugees
        //
        int[] sumAct = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // adding each activity
                                                         // and puting the value
                                                         // in array

        int[] sumAge = { 0, 0, 0, 0, 0 }; // adding agent all agents whose age
                                          // falls in a given age-class

        int[] sumfamSiz = { 0, 0, 0, 0, 0, 0, 0 }; // adding all agent families
                                                   // based o their family size

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
          if (f.getMembers().numObjs > 6) { // aggregate all families of >6
                                            // family size
            siz = 6;
          } else {
            siz = f.getMembers().numObjs - 1;
          }
          sumfamSiz[siz] += 1;
        }
        int none = 0;
        // accessing each agent
        for (int i = 0; i < humans.numObjs; i++) {
          Human human = (Human) humans.objs[i];
          // TODO: Refatorar
          switch (human.getCurrentActivity()) {
          case STAY_HOME:
            sumAct[0] += 1;
            break;
          case SCHOOL:
            sumAct[1] += 1;
            break;
          case RELIGION_ACTIVITY:
            sumAct[2] += 1;
            break;
          case SOCIAL_VISIT:
            sumAct[6] += 1;
            break;
          }

          int age = ageClass(human.getAge()); // age class of agent i
          // int siz = 0;
          sumAge[age] += 1;

          if (human.getHome().getCampID() == 1) {
            if (human.getCurrentHealthStatus().equals(HealthStatus.SUSCEPTIBLE)) {
              totSusDag = totSusDag + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.EXPOSED)) {
              totExpDag = totExpDag + 1;
            } else if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
              totInfDag = totInfDag + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
              totRecDag = totRecDag + 1;
            } else {
              none = 0;
            }
          }

          if (human.getHome().getCampID() == 2) {

            if (human.getCurrentHealthStatus().equals(HealthStatus.SUSCEPTIBLE)) {
              totSusInfo = totSusInfo + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.EXPOSED)) {
              totExpInfo = totExpInfo + 1;
            } else if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
              totInfInfo = totInfInfo + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
              totRecInfo = totRecInfo + 1;
            } else {
              none = 0;
            }
          }

          if (human.getHome().getCampID() == 3) {
            if (human.getCurrentHealthStatus().equals(HealthStatus.SUSCEPTIBLE)) {
              totSusHag = totSusHag + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.EXPOSED)) {
              totExpHag = totExpHag + 1;
            } else if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
              totInfHag = totInfHag + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
              totRecHag = totRecHag + 1;
            } else {
              none = 0;
            }
          }

          // total health status

          if (human.getCurrentHealthStatus().equals(HealthStatus.SUSCEPTIBLE)) {
            totalSus = totalSus + 1;
          } else if (human.getCurrentHealthStatus().equals(HealthStatus.EXPOSED)) {
            totalExp = totalExp + 1;
          } else if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
            totalInf = totalInf + 1;
          } else if (human.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
            totalRec = totalRec + 1;
          } else {
            none = 0;
          }

          if (human.getCurrentHealthStatus() != human.getPreviousHealthStatus()) {
            if (human.getCurrentHealthStatus().equals(HealthStatus.SUSCEPTIBLE)) {
              totalSusNewly = totalSusNewly + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.EXPOSED)) {
              totalExpNewly = totalExpNewly + 1;
            } else if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
              totalInfNewly = totalInfNewly + 1;
            } else if (human.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
              totalRecNewly = totalRecNewly + 1;
            } else {
              none = 0;
            }
          }

          defineInfectionNumbersInHumans(human);
        }

        defineInfectionNumbersInMosquitoes();

        setNumberOfSuscipitableNewly(totalSusNewly);
        setNumberOfExposedNewly(totalExpNewly);
        setNumberOfInfectedNewly(totalInfNewly);
        setNumberOfRecoveredNewly(totalRecNewly);

        setNumberOfSuscipitable(totalSus);
        setNumberOfExposed(totalExp);
        setNumberOfInfected(totalInf);
        setNumberOfRecovered(totalRec);

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

        // percentage - agent activity by type
        for (int i = 0; i < sumAct.length; i++) {
          getDataset().setValue(sumAct[i] * 100 / allHumans.getAllObjects().numObjs, actTitle, activities[i]);
        }

        String ageTitle = "Age Group";
        String[] ageC = new String[] { "1-4", "5-11", "12-17", "18-60", "60 +" };

        // ageset
        for (int i = 0; i < sumAge.length; i++) {
          agedataset.setValue(sumAge[i] * 100 / allHumans.getAllObjects().numObjs, ageTitle, ageC[i]);
        }

        String famTitle = "Household Size";
        String[] famC = new String[] { "1", "2", "3", "4", "5", "6", "6+" };

        // family size
        for (int i = 0; i < sumAge.length; i++) {
          familydataset.setValue(sumfamSiz[i], famTitle, famC[i]);
        }

        int totDead = countDeath();

        totalTotalPopSeries.add((double) (state.schedule.time()), allHumans.getAllObjects().numObjs);
        totalDeathSeries.add((double) (state.schedule.time()), totDead);
        // health status - percentage

        totalsusceptibleSeries.add((double) (state.schedule.time()), (totalSus));
        totalExposedSeries.add((double) (state.schedule.time()), (totalExp));
        totalInfectedSeries.add((double) (state.schedule.time()), (totalInf));
        totalRecoveredSeries.add((double) (state.schedule.time()), (totalRec));

        totalsusceptibleSeriesNewly.add((double) (state.schedule.time()), (totalSusNewly));
        totalExposedSeriesNewly.add((double) (state.schedule.time()), (totalExpNewly));
        totalInfectedSeriesNewly.add((double) (state.schedule.time()), (totalInfNewly));
        totalRecoveredSeriesNewly.add((double) (state.schedule.time()), (totalRecNewly));

        totalBacteriaLoadSeries.add((double) (state.schedule.time()), (getTotalBacterialLoad() / 1000000.0));

        rainfallSeries.add((double) (state.schedule.time()), rainfall.getCurrentRain());

        int m = ((int) state.schedule.time()) % 60;

        double t = (getTime().currentHour((int) state.schedule.getTime())) + (m / 60.0);
        int h = 1 + getTime().dayCount((int) state.schedule.getTime()); //
        hourDialer.setValue(t);
        dayDialer.setValue(h);

        setTotalBacterialLoad(rainfall.getTotalBacterialLoad());

      }

      private boolean isNewDay() {
        if (time.dayCount((int) schedule.getSteps()) > currentDay) {
          currentDay = time.dayCount((int) schedule.getSteps());
          return true;
        } else {
          return false;
        }
      }

      private void setTemperature() {
        List<Double> temperatures = getClimate().getTemperature();
        if (currentDay < temperatures.size()) {
          temperature = temperatures.get(currentDay);
        } else {
          // TODO:
        }
      }

      // TODO: remover os ovos que acabaram de nascer dos ambientes
      private void eggsIsReadyToHatch() {
        for (Object object : familyHousing) {
          FieldUnit fieldUnit = (FieldUnit) object;
          if (fieldUnit.getTimeOfMaturation() == 0 && fieldUnit.containsEggs() && !fieldUnit.isMatureEggs()) {
            fieldUnit.defineTimeOfMaturation(temperature);
          } else if (fieldUnit.getTimeOfMaturation() > 0 && fieldUnit.containsEggs() && !fieldUnit.isMatureEggs()) {
            double timeOfMaturation = fieldUnit.getTimeOfMaturation();
            timeOfMaturation--;
            fieldUnit.setTimeOfMaturation(timeOfMaturation);
            if (timeOfMaturation == 0) {
              fieldUnit.setMatureEggs(true);
            }
          } else if (fieldUnit.getTimeOfMaturation() == 0 && fieldUnit.isMatureEggs()) {
            int amount = fieldUnit.getEggs().size();
            for (int i = 0; i < amount; i++) {
              if (random.nextDouble() > 0.5) { // 50% chance of female
                int daysOfLife = 30 + random.nextInt(16);
                Mosquito mosquito = new Mosquito(daysOfLife, fieldUnit);
                mosquito.setCurrentHealthStatus(HealthStatus.SUSCEPTIBLE);
                mosquito.setStoppable(schedule.scheduleRepeating(mosquito, Mosquito.ORDERING, 1.0));
                fieldUnit.addMosquito(mosquito);
                allMosquitoes.add(mosquito);
              }
            }
          }
        }
      }
    };

    schedule.scheduleRepeating(chartUpdater);
    // System.out.println("total:- "+ allRefugees.getAllObjects().numObjs);
  }

  private void defineInfectionNumbersInHumans(Human human) {
    if (HealthStatus.MILD_INFECTION.equals(human.getCurrentHealthStatus())) {
      this.totalOfHumansWithMildInfection++;
    } else if (HealthStatus.SEVERE_INFECTION.equals(human.getCurrentHealthStatus())) {
      this.totalOfHumansWithSevereInfected++;
    }
  }

  private void defineInfectionNumbersInMosquitoes() {
    for (Object housing : familyHousing) {
      FieldUnit fieldUnit = (FieldUnit) housing;
      for (Object mosquito : fieldUnit.getMosquitoes()) {
        Mosquito m = (Mosquito) mosquito;
        if (HealthStatus.INFECTED.equals(m.getCurrentHealthStatus())) {
          this.totalOfMosquitoWithInfection++;
        }
      }
    }
  }

  // TODO: Rever está informação
  // age class or ageset
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

  public void killrefugee(Human human) {
    human.getFamily().removeMembers(human);
    // remova a família quando o tamanho for zero
    if (human.getFamily().getMembers().numObjs == 0) {
      allFamilies.remove(human.getFamily());
    }
    allHumans.remove(human);
  }

  // TODO: O que deve ser considerado para remover o mosquito?
  public void killmosquito(Mosquito mosquito) {
    mosquito.getCurrentPosition().removeMosquito(mosquito);
    allMosquitoes.remove(mosquito);
  }

  int PrevPop = 0;
  int curPop = 0;

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

  public static void main(String[] args) {
    doLoop(Dadaab.class, args);
    System.exit(0);
  }

  public void finish() {
    super.finish();

    if (dObserver != null) {
      this.dObserver.finish();
    }
  }

  public void addMosquitoes(Mosquito mosquito) {
    this.allMosquitoes.add(mosquito);
  }

  public Bag getAllMosquitoes() {
    return allMosquitoes;
  }

  public void setNumberOfSuscipitable(int expo) {
    this.totalSusciptible = expo;
  }

  public int getNumberOfSuscipitable() {
    return totalSusciptible;
  }

  public void setNumberOfExposed(int expo) {
    this.totalExposed = expo;
  }

  public int getNumberOfExposed() {
    return totalExposed;
  }

  public void setNumberOfInfected(int number) {
    this.totalInfected = number;
  }

  public int getNumberOfInfected() {
    return this.totalInfected;
  }

  public void setNumberOfRecovered(int rec) {
    this.totalRecovered = rec;
  }

  public int getNumberOfRecovered() {
    return totalRecovered;
  }

  public void setNumberOfSuscipitableNewly(int expo) {
    this.totalSusciptibleNewly = expo;
  }

  public int getNumberOfSuscipitableNewly() {
    return totalSusciptibleNewly;
  }

  public void setNumberOfExposedNewly(int expo) {
    this.totalExposedNewly = expo;
  }

  public int getNumberOfExposedNewly() {
    return totalExposedNewly;
  }

  public void setNumberOfInfectedNewly(int inf) {
    this.totalInfectedNewly = inf;
  }

  public int getNumberOfInfectedNewly() {
    return totalInfectedNewly;
  }

  public void setNumberOfRecoveredNewly(int rec) {
    this.totalRecoveredNewly = rec;
  }

  public int getNumberOfRecoveredNewly() {
    return totalRecoveredNewly;
  }

  public void setTotalActivity(int[] rec) {
    this.totalActivity = rec;
  }

  public int[] getTotalActivity() {
    return totalActivity;
  }

  public void setTotalBacterialLoad(double r) {
    this.totalBacterialLoad = r;
  }

  public double getTotalBacterialLoad() {
    return totalBacterialLoad;
  }

  public Parameters getParams() {
    return params;
  }

  public DefaultCategoryDataset getDataset() {
    return this.dataset;
  }

  public void setDataset(DefaultCategoryDataset dataset) {
    this.dataset = dataset;
  }

  public TimeManager getTime() {
    return time;
  }

  public int getTotalOfHumansWithSevereInfected() {
    return totalOfHumansWithSevereInfected;
  }

  public void setTotalOfHumansWithSevereInfected(int totalOfHumansWithSevereInfected) {
    this.totalOfHumansWithSevereInfected = totalOfHumansWithSevereInfected;
  }

  public int getTotalOfHumansWithMildInfection() {
    return totalOfHumansWithMildInfection;
  }

  public void setTotalOfHumansWithMildInfection(int totalOfHumansWithMildInfection) {
    this.totalOfHumansWithMildInfection = totalOfHumansWithMildInfection;
  }

  public int getTotalOfMosquitoWithInfection() {
    return totalOfMosquitoWithInfection;
  }

  public void setTotalOfMosquitoWithInfection(int totalOfMosquitoWithInfection) {
    this.totalOfMosquitoWithInfection = totalOfMosquitoWithInfection;
  }

  public Climate getClimate() {
    return climate;
  }

  public void setClimate(Climate climate) {
    this.climate = climate;
  }

  public int getCurrentDay() {
    return currentDay;
  }

  public void setCurrentDay(int currentDay) {
    this.currentDay = currentDay;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

}
