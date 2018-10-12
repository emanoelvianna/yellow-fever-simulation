package com.core;

import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.xy.XYSeries;

import com.core.algorithms.TimeManager;
import com.core.report.YellowFeverReport;
import com.model.Building;
import com.model.Climate;
import com.model.Facility;
import com.model.Family;
import com.model.Human;
import com.model.Mosquito;
import com.model.enumeration.DayOfWeek;
import com.model.enumeration.HealthStatus;

import sim.engine.Schedule;
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

  private static final long serialVersionUID = -5966446373681187141L;
  // The model environment - holds fields (parcels)
  public ObjectGrid2D allCamps;
  public GeomGridField allCampGeoGrid;
  public DoubleGrid2D rainfallGrid; // TODO: Isto aqui ainda faz sentido?
  public Continuous2D allHumans;
  public SparseGrid2D facilityGrid;
  public IntGrid2D roadGrid;
  public GeomVectorField roadLinks;
  public GeomVectorField campShape;
  public SparseGrid2D nodes;
  // the road nodes closest to each of the locations
  public ObjectGrid2D closestNodes;
  Network roadNetwork = new Network();
  private final Parameters params;
  private int currentDay;
  private double temperature;
  private int totalSusceptible;
  private int totalExposed;
  private int totalOfMildInfected;
  private int totalOfSevereInfected;
  private int totalOfToxicInfected;
  private int totalOfRecovered;
  private int totalDeadMosquitoes;
  private int totalDeadHumans;

  // charts and graphs
  public XYSeries rainfallSeries = new XYSeries(" Rainfall"); //
  public XYSeries totalsusceptibleSeries = new XYSeries("Susceptible");
  public XYSeries totalExposedSeries = new XYSeries("Exposed");
  public XYSeries totalMildInfectedSeries = new XYSeries("Mild Infected");
  public XYSeries totalSevereInfectedSeries = new XYSeries("Severe Infected");
  public XYSeries totalToxicInfectedSeries = new XYSeries("Toxic Infected");
  public XYSeries totalRecoveredSeries = new XYSeries("Recovered");
  public XYSeries totalDeathSeries = new XYSeries(" Death");
  public XYSeries totalTotalPopSeries = new XYSeries(" Total");

  public DefaultCategoryDataset dataset = new DefaultCategoryDataset();
  // shows age structure of agents
  public DefaultCategoryDataset agedataset = new DefaultCategoryDataset();
  // shows family size
  public DefaultCategoryDataset familydataset = new DefaultCategoryDataset();
  public DefaultValueDataset hourDialer = new DefaultValueDataset();
  public DefaultValueDataset dayDialer = new DefaultValueDataset();

  public int totalgridWidth = 10;
  public int totalgridHeight = 10;
  private Bag allFamilies; // holding all families
  private Bag allMosquitoes;
  private Bag familyHousing;
  private Bag allFacilities;
  private Bag works;
  private Bag schooles;
  private Bag healthCenters;
  private Bag mosques;
  private Bag market;
  private Bag foodCenter;
  private Bag other;
  // WaterContamination fm = new WaterContamination();
  Facility facility;// schduling borehole refill
  private Climate climate;

  private TimeManager time = new TimeManager();

  public YellowFeverReport report;
  int[] sumActivities = { 0, 0, 0, 0, 0, 0, 0, 0 }; //

  public YellowFever(long seed, String[] args) {
    super(seed);
    this.params = new Parameters(args);
    this.facility = new Facility(this.getParams().getGlobal().getHeaalthFacilityCapacity());
    this.setAllFamilies(new Bag());
    this.allMosquitoes = new Bag();
    this.familyHousing = new Bag();
    this.works = new Bag();
    this.allFacilities = new Bag();
    this.allCampGeoGrid = new GeomGridField();
    this.climate = new Climate();
    this.currentDay = 0;
    this.temperature = 0;
    this.schooles = new Bag();
    this.healthCenters = new Bag();
    this.mosques = new Bag();
    this.market = new Bag();
    this.foodCenter = new Bag();
    this.other = new Bag();
    this.totalSusceptible = 0;
    this.totalExposed = 0;
    this.totalOfMildInfected = 0;
    this.totalOfSevereInfected = 0;
    this.totalOfToxicInfected = 0;
    this.totalOfRecovered = 0;
    this.totalDeadMosquitoes = 0;
    this.totalDeadHumans = 0;
  }

  public void start() {
    super.start();
    SimulationBuilder builder = new SimulationBuilder();
    builder.create(this, this.random);

    schedule.scheduleRepeating(facility, Facility.ORDERING, 1);

    this.outputStats(schedule);

    Steppable updater = new Steppable() {
      // all graphs and charts wll be updated in each steps
      public void step(SimState state) {
        if (isNewDay()) {
          setTemperature();
          setPrecipitation();
          probabilityOfEggsDying();
          probabilityOfEggsHatching();
          probabilityOfEggsAppearInHouses();
        }

        // getting all humans
        Bag humans = allHumans.getAllObjects();

        // adding all agent families based o their family size
        int[] sumfamSiz = { 0, 0, 0, 0, 0, 0, 0 };

        int totalOfSusceptible = 0;
        int totalOfExposed = 0;
        int totalOfMildInfected = 0;
        int totalOfSevereInfected = 0;
        int totalOfToxicInfected = 0;
        int totalOfRecovered = 0;

        // accessing all families and chatagorize them based on their size
        for (int i = 0; i < getAllFamilies().numObjs; i++) {
          Family f = (Family) getAllFamilies().objs[i];
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
        // accessing each agent
        for (int i = 0; i < humans.numObjs; i++) {
          Human human = (Human) humans.objs[i];

          if (human.getCurrentHealthStatus().equals(HealthStatus.SUSCEPTIBLE)) {
            totalOfSusceptible++;
          } else if (HealthStatus.EXPOSED.equals(human.getCurrentHealthStatus())) {
            totalOfExposed++;
          } else if (HealthStatus.MILD_INFECTION.equals(human.getCurrentHealthStatus())) {
            totalOfMildInfected++;
          } else if (HealthStatus.SEVERE_INFECTION.equals(human.getCurrentHealthStatus())) {
            totalOfSevereInfected++;
          } else if (HealthStatus.TOXIC_INFECTION.equals(human.getCurrentHealthStatus())) {
            totalOfToxicInfected++;
          } else if (human.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
            totalOfRecovered++;
          }
        }

        setTotalSusceptible(totalOfSusceptible);
        setTotalExposed(totalOfExposed);
        setTotalOfMildInfected(totalOfMildInfected);
        setTotalOfSevereInfected(totalOfSevereInfected);
        setTotalOfToxicInfected(totalOfToxicInfected);
        setTotalOfRecovered(totalOfRecovered);

        totalTotalPopSeries.add((double) (state.schedule.getTime()), allHumans.getAllObjects().numObjs);
        totalDeathSeries.add((double) (state.schedule.getTime()), getNumberDeadHumans());
        totalsusceptibleSeries.add((double) (state.schedule.getTime()), (totalOfSusceptible));
        totalExposedSeries.add((double) (state.schedule.getTime()), (totalOfExposed));
        totalMildInfectedSeries.add((double) (state.schedule.getTime()), (totalOfMildInfected));
        totalSevereInfectedSeries.add((double) (state.schedule.getTime()), (totalOfSevereInfected));
        totalToxicInfectedSeries.add((double) (state.schedule.getTime()), (totalOfToxicInfected));
        totalRecoveredSeries.add((double) (state.schedule.getTime()), (totalOfRecovered));

        int m = ((int) state.schedule.getTime()) % 60;

        double t = (getTime().currentHour((int) state.schedule.getTime())) + (m / 60.0);
        int h = getTime().dayCount((int) state.schedule.getTime());
        hourDialer.setValue(t);
        dayDialer.setValue(h);
      }
    };
    schedule.scheduleRepeating(updater);
  }

  private boolean isNewDay() {
    if (time.dayCount((int) schedule.getSteps()) > currentDay) {
      currentDay = time.dayCount((int) schedule.getSteps());
      return true;
    } else {
      return false;
    }
  }

  private void outputStats(Schedule schedule) {
    report = new YellowFeverReport(this);
    schedule.scheduleRepeating(report, YellowFeverReport.ORDERING, 1.0);
  }

  public String getCurrentDayOfWeek() {
    int day = this.time.dayCount((int) schedule.getSteps());
    return String.valueOf(DayOfWeek.getDayOfWeek(day));
  }

  public double setTemperature() {
    List<Double> temperatures = climate.getTemperature();
    if (currentDay < temperatures.size()) {
      temperature = temperatures.get(currentDay);
    }
    return this.temperature;
  }

  public void setInitialTemperature(double initial) {
    this.temperature = initial;
  }

  private void setPrecipitation() {
    List<Double> rainfall = climate.getPrecipitation();
    double mm = params.getGlobal().getWaterAbsorption();
    for (Object object : getFamilyHousing()) {
      Building housing = (Building) object;
      if (random.nextDouble() <= 0.5) { // 50% chance
        housing.waterAbsorption(mm);
        housing.addWater(rainfall.get(currentDay));
      }
    }
  }

  public void setInitialPrecipitation(double initial) {
    double mm = params.getGlobal().getWaterAbsorption();
    for (Object object : getFamilyHousing()) {
      Building housing = (Building) object;
      if (random.nextDouble() <= 0.5) { // 50% chance
        housing.waterAbsorption(mm);
        housing.addWater(initial);
      }
    }
  }

  private void probabilityOfEggsDying() {
    for (Object object : getFamilyHousing()) {
      Building housing = (Building) object;
      if (housing.containsEggs()) {
        int amount = housing.getEggs();
        for (int i = 0; i < amount; i++) {
          if (random.nextDouble() <= 0.05) // 5% chance
            housing.removeEgg();
        }
      }
    }
  }

  private void probabilityOfEggsHatching() {
    for (Object object : getFamilyHousing()) {
      Building housing = (Building) object;
      if (housing.getTimeOfMaturation() > 0 && housing.containsEggs()) {
        double timeOfMaturation = housing.getTimeOfMaturation();
        housing.setTimeOfMaturation(--timeOfMaturation);
      } else if (housing.getTimeOfMaturation() <= 0 && housing.containsEggs()) {
        int amount = housing.getEggs();
        for (int i = 0; i < amount; i++) {
          if (random.nextDouble() >= 0.5) { // 50% chance of female
            Mosquito mosquito = new Mosquito(housing);
            mosquito.setStoppable(schedule.scheduleRepeating(mosquito, Mosquito.ORDERING, 1.0));
            housing.addMosquito(mosquito);
            housing.removeEgg();
            allMosquitoes.add(mosquito);
          } else {
            housing.removeEgg();
          }
        }
      }
    }
  }

  private void probabilityOfEggsAppearInHouses() {
    int probability = params.getGlobal().getProbabilityOfEggsAppearInHouses();
    for (Object object : getFamilyHousing()) {
      Building housing = (Building) object;
      if (random.nextInt(101) <= probability) {
        housing.addEgg(random.nextInt(101));
      }
    }
  }

  public void killrefugee(Human human) {
    human.getFamily().removeMembers(human);
    if (human.getFamily().getMembers().numObjs == 0) {
      getAllFamilies().remove(human.getFamily());
    }
    allHumans.remove(human);
    this.totalDeadHumans++;
  }

  public void killmosquito(Mosquito mosquito) {
    mosquito.getCurrentPosition().removeMosquito(mosquito);
    allMosquitoes.remove(mosquito);
    this.totalDeadMosquitoes++;
  }

  public static void main(String[] args) {
    doLoop(YellowFever.class, args);
    System.exit(0);
  }

  public void finish() {
    super.finish();
    if (report != null) {
      this.report.finish();
    }
  }

  public void addMosquitoes(Mosquito mosquito) {
    this.allMosquitoes.add(mosquito);
  }

  public Bag getAllMosquitoes() {
    return allMosquitoes;
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

  public Bag getAllFamilies() {
    return allFamilies;
  }

  public void setAllFamilies(Bag allFamilies) {
    this.allFamilies = allFamilies;
  }

  public Bag getWorks() {
    return works;
  }

  public void setWorks(Bag works) {
    this.works = works;
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

  public Bag getFamilyHousing() {
    return familyHousing;
  }

  public void setFamilyHousing(Bag familyHousing) {
    this.familyHousing = familyHousing;
  }

  public Bag getMarket() {
    return market;
  }

  public void setMarket(Bag market) {
    this.market = market;
  }

  public int getNumberOfSusceptible() {
    return totalSusceptible;
  }

  public void setTotalSusceptible(int totalSusceptible) {
    this.totalSusceptible = totalSusceptible;
  }

  public int getNumberOfExposed() {
    return totalExposed;
  }

  public void setTotalExposed(int totalExposed) {
    this.totalExposed = totalExposed;
  }

  public int getNumberOfMildInfected() {
    return totalOfMildInfected;
  }

  public void setTotalOfMildInfected(int totalOfMildInfected) {
    this.totalOfMildInfected = totalOfMildInfected;
  }

  public int getNumberOfSevereInfected() {
    return totalOfSevereInfected;
  }

  public void setTotalOfSevereInfected(int totalOfSevereInfected) {
    this.totalOfSevereInfected = totalOfSevereInfected;
  }

  public int getNumberOfToxicInfected() {
    return totalOfToxicInfected;
  }

  public void setTotalOfToxicInfected(int totalOfToxicInfected) {
    this.totalOfToxicInfected = totalOfToxicInfected;
  }

  public int getNumberOfRecovered() {
    return totalOfRecovered;
  }

  public void setTotalOfRecovered(int totalOfRecovered) {
    this.totalOfRecovered = totalOfRecovered;
  }

  public int getTotalDeadMosquitoes() {
    return totalDeadMosquitoes;
  }

  public void setTotalDeadMosquitoes(int totalDeadMosquitoes) {
    this.totalDeadMosquitoes = totalDeadMosquitoes;
  }

  public int getNumberDeadHumans() {
    return totalDeadHumans;
  }

  public void setTotalDeadHumans(int totalDeadHumans) {
    this.totalDeadHumans = totalDeadHumans;
  }

}
