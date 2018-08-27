package com.model;

import com.core.TimeManager;
import com.core.YellowFever;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;

public class Rainfall implements Steppable {

  private static double MAXIMUM_WATER_REQUIREMENT = 15; // 15 liter per day - for all uses
  private static double ABSORPTION_RATE_PER_MINUTE = 4.33;// mm/minute evaporation - taking median 1750 =
  private static final int ORDERING = 0; // rainfall schedules first
  private final double areaUsed = 0.6; // 60% of area of the cell used for calculating volume
  private final double coversionFactor = areaUsed * 90 * 90 * 1000.0; // area * litre
  private double currentRain;
  private final double MAXVIBROFLOWWAtER = 50; // mm of rain in a field. if it is greater than this, all virbo will
                                               // flood to next cell
  private int rainDay = 0;
  private double totalBacterialLoad = 0;
  TimeManager tm = new TimeManager();
  int rainDuration = 20;
  int rainMinute = 0;

  public void recieveRain(YellowFever yellowFever) {
    double rain_liter = 0;
    double currentRain = 0;
    int startmonth = 181; // number start from 0, ( 182-1) // start at july =182
    // start from x days
    if ((int) yellowFever.schedule.getTime() % 1440 > rainMinute
        && (int) yellowFever.schedule.getTime() % 1440 <= (rainMinute + rainDuration)) {

      int indexA = tm.dayCount((int) yellowFever.schedule.getTime()) % 365;
      int indexSep = (indexA + startmonth) % 365;
      currentRain = yellowFever.getDailyRain()[indexSep] / (1.0 * rainDuration);
      rain_liter = currentRain * 0.001 * coversionFactor;
    } else {
      rain_liter = 0;
    }

    this.setCurrentRain(rain_liter);

    for (int x = 0; x < yellowFever.getRegion().getWidth(); x++) {
      for (int y = 0; y < yellowFever.getRegion().getHeight(); y++) {
        Building building = (Building) yellowFever.getRegion().get(x, y);
        // avoud camps - agent houses
        if (building.getType() == 11 || building.getType() == 12 || building.getType() == 21 || building.getType() == 22
            || building.getType() == 31 || building.getType() == 32) {
          building.setWater(0);

        } else {

          double newWater = rain_liter + building.getWater();

          if (newWater < 0) {
            newWater = 0;
          }
          building.setWater(newWater);
        } // adding water on the field
      }
    }
  }

  /* water flow is based on simple hydrology */
  public void drain(YellowFever yellowFever) {

    for (int x = 0; x < yellowFever.getRegion().getWidth(); x++) {
      for (int y = 0; y < yellowFever.getRegion().getHeight(); y++) {
        // ceter cell
        Building building = (Building) yellowFever.getRegion().field[x][y];
        // avoid camps
        if (building.getType() == 11 || building.getType() == 12 || building.getType() == 21 || building.getType() == 22
            || building.getType() == 31 || building.getType() == 32) {
          continue;
        }
        fieldDrainageSimple(building, yellowFever);
      }
    }
  }

  public void fieldDrainageSimple(Building field, YellowFever yellowFever) {
    Bag n = new Bag();
    n.clear();
    /* get moore neighborhood */
    yellowFever.getRegion().getNeighborsMaxDistance(field.getX(), field.getY(), 1, false, n, null, null);
    if (n.isEmpty() == true) {
      return;
    }
    // TODO: Verifica a necessidade deste atributo
    // if field holds borehole avoid it,
    // no miz of water on borehole
    // if (yellowFever.boreHoles.contains(field) == true) {
    // return;
    // }

    for (Object obj : n) {
      Building building = (Building) obj;
      // water can not flow to itself
      if (building.equals(field) == true) {
        continue;
      }
      if (building.getWater() <= 0) {
        continue;
      }
      // avoid camps or facility
      if (building.getType() == 11 || building.getType() == 12 || building.getType() == 21 || building.getType() == 22
          || building.getType() == 31 || building.getType() == 32) {
        continue;
      }
      // TODO: Verifica a necessidade deste atributo
      // avoid borehole points
      // if (d.boreHoles.contains(building) == true) {
      // continue;
      // }

      double avoidW = 0;
      if (field.getX() == 0 || field.getY() == 0 || field.getX() >= 144 || field.getY() >= 268) {
        avoidW = 0;
      } else {
        avoidW = 1.0;
      }
      double h1 = field.getElevation(); // pseudo elevation of center cell
      double h2 = building.getElevation();// pseudo elevation of center cell
      double virbThreshold = MAXVIBROFLOWWAtER * 0.001 * coversionFactor;
      double diff = h2 - h1;

      if (diff <= 0) {
        continue;
      }

      if (diff > 0) {
        double waterRemain = 0;
        double waterflow = 0;
        double waterUpper = building.getWater(); // hold for vibro calc
        double rateFlowtoOtherCell = building.getWater() * (1.0 - 1.0 / (1 + diff)); // 60%
        waterRemain = building.getWater() - rateFlowtoOtherCell;
        waterflow = field.getWater() + rateFlowtoOtherCell;

        if (waterRemain < 0) {
          waterRemain = 0;
        }

        building.setWater(waterRemain * avoidW);
        field.setWater(waterflow * avoidW);

        if (waterUpper > virbThreshold) {
          virbThreshold = waterUpper;
        }
        // TODO: Verifica a necessidade deste atributo
        // double vibroflow = building.getVibrioCholerae() * rateFlowtoOtherCell /
        // (virbThreshold);
        // double virbroRemain = (building.getVibrioCholerae() - vibroflow) * avoidW;
        // if (virbroRemain < 0) {
        // virbroRemain = 0;
        // }
        // nf.setVibrioCholerae(virbroRemain);
        // field.setVibrioCholerae((field.getVibrioCholerae() + vibroflow) * avoidW);
      }
    }
  }

  /* only happens if there is water- if not seepage is 0 */
  public void waterAbsorbtion(YellowFever yellowFever) {
    for (int x = 0; x < yellowFever.getRegion().getWidth(); x++) {
      for (int y = 0; y < yellowFever.getRegion().getHeight(); y++) {
        Building building = (Building) yellowFever.getRegion().field[x][y];
        double w = 0;
        w = building.getWater() - (ABSORPTION_RATE_PER_MINUTE * coversionFactor * 0.001);
        if (w <= 0) {
          building.setWater(0);
        } else {
          building.setWater(w);
        }
        if (x == 0 || y == 0 || x == 145 || y == 269) {
          building.setWater(0);
          // TODO: Verifica a necessidade deste atributo
          // building.setVibrioCholerae(0);
        }
      }
    }
  }

  /* visualization of rain flows */
  public void drawRiver(YellowFever yellowFever) {
    yellowFever.getRainfallWater().clear();
    double totBac = 0;
    for (int x = 0; x < yellowFever.getRegion().getWidth(); x++) {
      for (int y = 0; y < yellowFever.getRegion().getHeight(); y++) {

        Building building = (Building) yellowFever.getRegion().field[x][y];
        yellowFever.getRainfallGrid().field[building.getX()][building.getY()] = building.getWater();
        // TODO: Verifica a necessidade deste atributo
        // totBac = totBac + building.getVibrioCholerae();
        if (building.getWater() > MAXIMUM_WATER_REQUIREMENT) {
          yellowFever.getRainfallWater().add(building);
        }
      }
    }
    setTotalBacterialLoad(totBac);
  }

  //
  public void step(SimState state) {
    YellowFever yellowFever = (YellowFever) state;
    if ((int) yellowFever.schedule.getTime() % 1440 == 1) {
      int interval = 1440 - (2 * rainDuration);
      rainMinute = 2 + yellowFever.random.nextInt(interval);
    }
    recieveRain(yellowFever);
    waterAbsorbtion(yellowFever);
    drain(yellowFever);
    drawRiver(yellowFever);
  }

  public void setRainDay(int r) {
    this.rainDay = r;
  }

  public int getRainDay() {
    return rainDay;
  }

  public void setCurrentRain(double r) {
    this.currentRain = r;
  }

  public double getCurrentRain() {
    return currentRain;
  }

  public void setTotalBacterialLoad(double r) {
    this.totalBacterialLoad = r;
  }

  public double getTotalBacterialLoad() {
    return totalBacterialLoad;
  }

  public static int getOrdering() {
    return ORDERING;
  }
}
