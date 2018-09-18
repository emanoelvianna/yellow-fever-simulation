package com.model;

import com.core.Dadaab;
import com.core.TimeManager;
import com.model.enumeration.ActivityMapping;
import com.model.enumeration.HealthStatus;

import ec.util.MersenneTwisterFast;
import sim.util.Bag;

public class Activity {

  private Human human;
  private TimeManager time;
  private int currentStep;
  private int minuteInDay;
  private MersenneTwisterFast random;

  public Activity(Human human, TimeManager time, int currentStep, int minuteInDay) {
    this.human = human;
    this.time = time;
    this.currentStep = currentStep;
    this.random = new MersenneTwisterFast();
    this.minuteInDay = minuteInDay;
  }

  // check the crowded level on the road or at your goal location
  // this method is taken from Haiti project
  /*
   * activity selection currently is made by simple assumption that consider
   * age, sex, need and time in most cases based on these each activity is given
   * some weight and the best of all will e selected
   */
  public ActivityMapping defineActivity(Dadaab dadaab) {
    ActivityMapping activity = ActivityMapping.STAY_HOME;
    if (this.gettingMedicalHelp(dadaab)) {
      return ActivityMapping.HEALTH_CENTER;
    } else if (this.minuteInDay >= (8 * 60) && this.minuteInDay <= (18 * 60)) {
      // TODO: A definição do dia da semana possui um problema!
      if (time.currentDayInWeek(currentStep) < 5) {
        if (this.human.isWorker()) {
          activity = ActivityMapping.WORK;
        } else if (this.human.isStudent() && this.minuteInDay >= (8 * 60) && this.minuteInDay <= (12 * 60)) {
          activity = ActivityMapping.SCHOOL;
        }
      } else {
        int random = dadaab.random.nextInt(100);
        if (random <= 80) { // 80% chance of activity away from home
          if (random <= 40) {
            return ActivityMapping.RELIGION_ACTIVITY;
          } else {
            return ActivityMapping.SOCIAL_VISIT;
          }
        } else {
          return ActivityMapping.STAY_HOME;
        }
      }
    }
    return activity;
  }

  // TODO: Bug sobre a busca de recursos médicos!
  private boolean gettingMedicalHelp(Dadaab dadaab) {
    if (this.human.hasSymptomsOfInfection()) {
      if (dadaab.random.nextInt(11) < 5) { // 50-50 chance
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  // best location is mainly determine by distance
  public FieldUnit bestActivityLocation(Human ref, FieldUnit position, ActivityMapping activityMapping, Dadaab d) {
    switch (activityMapping) {
    case STAY_HOME:
      return ref.getHome();
    case WORK:
      // TODO: BoreHoles podem ser considerados os locais de trabalho?
      return betstLoc(ref.getHome(), d.boreHoles, d);
    case SCHOOL:
      return betstLoc(ref.getHome(), d.schooles, d);
    case RELIGION_ACTIVITY:
      return betstLoc(ref.getHome(), d.mosques, d);
    case HEALTH_CENTER:
      return betstLoc(ref.getHome(), d.healthCenters, d);
    case SOCIAL_VISIT:
      return socialize(ref, d);
    default:
      return ref.getHome();
    }
  }

  // TODO: Importante rever as atividades e os tempos relacionados
  public int stayingPeriod(ActivityMapping activityMapping) {
    final int MINUTE = 60;
    int period = 0;
    int minimumStay = 20; // minimum delay
    int maximumStay = 180; // three hour

    switch (activityMapping) {
    case STAY_HOME:
      period = maximumStay;
      break;
    case SCHOOL:
      // time at school maximum until ~12:00 pm
      period = 4 * MINUTE;
      break;
    case WORK:
      // time at work maximum until ~19:00 pm
      period = 10 * MINUTE;
      break;
    case SOCIAL_VISIT:
      // the average visit time is 2 hours
      period = minimumStay + random.nextInt(2 * MINUTE);
      break;
    case RELIGION_ACTIVITY:
      // time at maximum unti 2 hours
      System.out.println("RELIGION_ACTIVITY");
      period = minimumStay + random.nextInt(2 * MINUTE);
      break;
    case HEALTH_CENTER:
      // time at maximum unti 2 hours
      period = minimumStay + random.nextInt(2 * MINUTE);
      break;
    }
    return (period + this.minuteInDay);
  }

  // TODO: Verificar a necessidade de realizar alguma operação na atividade
  // TODO: Atualmente a que parece fazer sentido é somente a relacionada ao
  // médico
  public void doActivity(FieldUnit f, ActivityMapping activityMapping, Dadaab dadaab) {
    switch (activityMapping) {
    case STAY_HOME:
      break;
    case HEALTH_CENTER:
      this.human.receiveTreatment(f, dadaab);
      // TODO: Recebe orientação para remoção de focos do mosquito?
      break;
    case SOCIAL_VISIT:
      if (random.nextDouble() < dadaab.getParams().getGlobal().getProbabilityGuestContaminationRate()) {
      }
      break;
    default:
    }
  }

  private FieldUnit betstLoc(FieldUnit fLoc, Bag fieldBag, Dadaab d) {
    Bag newLoc = new Bag();
    double bestScoreSoFar = Double.POSITIVE_INFINITY;
    for (int i = 0; i < fieldBag.numObjs; i++) {
      FieldUnit potLoc = ((FieldUnit) fieldBag.objs[i]);
      double fScore = fLoc.distanceTo(potLoc);
      if (fScore > bestScoreSoFar) {
        continue;
      }
      if (fScore <= bestScoreSoFar) {
        bestScoreSoFar = fScore;
        newLoc.clear();
      }
      newLoc.add(potLoc);
    }
    FieldUnit f = null;
    if (newLoc != null) {
      int winningIndex = 0;
      if (newLoc.numObjs >= 1) {
        winningIndex = d.random.nextInt(newLoc.numObjs);
      }
      // System.out.println("other" + newLoc.numObjs);
      f = (FieldUnit) newLoc.objs[winningIndex];
    }
    return f;
  }

  public FieldUnit getNextTile(Dadaab dadaab, FieldUnit subgoal, FieldUnit position) {
    // move in which direction?
    int moveX = 0, moveY = 0;
    int dx = subgoal.getLocationX() - position.getLocationX();
    int dy = subgoal.getLocationY() - position.getLocationY();
    if (dx < 0) {
      moveX = -1;
    } else if (dx > 0) {
      moveX = 1;
    }
    if (dy < 0) {
      moveY = -1;
    } else if (dy > 0) {
      moveY = 1;
    }
    // can either move in Y direction or X direction: see which is better
    FieldUnit xmove = ((FieldUnit) dadaab.allCamps.field[position.getLocationX() + moveX][position.getLocationY()]);
    FieldUnit ymove = ((FieldUnit) dadaab.allCamps.field[position.getLocationX()][position.getLocationY() + moveY]);

    boolean xmoveToRoad = ((Integer) dadaab.roadGrid.get(xmove.getLocationX(), xmove.getLocationY())) > 0;
    boolean ymoveToRoad = ((Integer) dadaab.roadGrid.get(ymove.getLocationX(), ymove.getLocationX())) > 0;

    if (moveX == 0 && moveY == 0) {
      // we are ON the subgoal, so don't move at all
      // both are the same result, so just return the xmove (which is identical)
      return xmove;
    } else if (moveX == 0) {
      // this means that moving in the x direction is not a valid move: it's +0
      return ymove;
    } else if (moveY == 0) {
      // this means that moving in the y direction is not a valid move: it's +0
      return xmove;
    } else if (xmoveToRoad == ymoveToRoad) {
      // equally good moves: pick randomly between them
      if (dadaab.random.nextBoolean()) {
        return xmove;
      } else {
        return ymove;
      }
    } else if (xmoveToRoad && moveX != 0) { // x is a road: pick it
      return xmove;
    } else if (ymoveToRoad && moveY != 0) { // yes// y is a road: pick it
      return ymove;
    } else if (moveX != 0) { // move in the better direction
      return xmove;
    } else if (moveY != 0) { // yes
      return ymove;
    } else {
      return ymove; // no justification
    }
  }

  // three camp sites in the model
  // agent select one camp which is not their camp randomly
  private FieldUnit socialize(Human ref, Dadaab d) {
    Bag potential = new Bag();
    FieldUnit newLoc = null;
    potential.clear();

    // socialize - visit friend or any place in
    // potential = d.campSites;

    int camp = ref.getHome().getCampID(); // get camp id

    // select any camp site but not the camp that belong to the agent
    for (Object campsite : d.familyHousing) {
      FieldUnit cmp = ((FieldUnit) campsite);
      if (cmp.getCampID() == camp && cmp.equals(ref.getHome()) != true && cmp.getRefugeeHH().numObjs > 0) {
        potential.add(cmp); // potential locations to visit
      }
    }

    // if(potential.isEmpty() ==true){
    // System.out.println("empty");
    // }
    if (potential.numObjs == 1) {
      newLoc = (FieldUnit) potential.objs[0];
    } else {
      newLoc = (FieldUnit) potential.objs[d.random.nextInt(potential.numObjs)];
    }

    return newLoc;
  }

  // find the nearest water points
  public FieldUnit nearestWaterPoint(FieldUnit f, Dadaab d) {
    return betstLoc(f, d.rainfallWater, d);
  }

  // search nearest borehold
  // this is useful if one of the borehole is empty
  // agent will selct another borehole nearest from the current
  private FieldUnit nearestBorehole(FieldUnit f, Dadaab d) {
    return betstLoc(f, d.boreHoles, d);
  }

  // select water source either from borehole or rainfall water points
  // agent preference weight and distance affect the choice

  public FieldUnit nearestWaterSource(FieldUnit f, Dadaab d) {
    FieldUnit fieldP = null;

    double preference_river = 0.0;
    double preference_borehole = 0.0;

    // incase no water point field to select, preference is 0
    if (nearestWaterPoint(f, d) == null) {
      preference_river = 0.0;
    }
    // preference depend on inverse distance and preference weight
    else {
      preference_river = (1.0 / (1.0 + Math.log(1 + f.distanceTo(nearestWaterPoint(f, d)))))
          * d.getParams().getGlobal().getWaterSourcePreference_River() + (0.2 * d.random.nextDouble());
    }

    if (nearestBorehole(f, d) == null) {
      preference_borehole = 0.0;
    } else {
      preference_borehole = (1.0 / (1.0 + Math.log(1 + f.distanceTo(nearestBorehole(f, d)))))
          * d.getParams().getGlobal().getWaterSourcePreference_Borehole() + (0.2 * d.random.nextDouble());
    }

    if (preference_river > preference_borehole) {
      fieldP = nearestWaterPoint(f, d);
    } else {
      fieldP = nearestBorehole(f, d);
    }

    return fieldP;
  }

  public Human getRefugee() {
    return human;
  }

  public void setRefugee(Human refugee) {
    this.human = refugee;
  }

}
