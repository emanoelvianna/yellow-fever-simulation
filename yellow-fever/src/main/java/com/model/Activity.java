package com.model;

import com.core.YellowFever;
import com.core.algorithms.TimeManager;
import com.model.enumeration.ActivityMapping;

import ec.util.MersenneTwisterFast;
import sim.util.Bag;

public class Activity {

  private YellowFever dadaab;
  private Human human;
  private TimeManager time;
  private int currentStep;
  private int minuteInDay;
  private MersenneTwisterFast random;

  public Activity(YellowFever dadaab, Human human, TimeManager time, int currentStep, int minuteInDay) {
    this.dadaab = dadaab;
    this.human = human;
    this.time = time;
    this.currentStep = currentStep;
    this.random = new MersenneTwisterFast();
    this.minuteInDay = minuteInDay;
  }

  public ActivityMapping defineActivity(YellowFever dadaab) {
    ActivityMapping activity = ActivityMapping.STAY_HOME;
    if (this.gettingMedicalHelp()) { // 50% chance
      return ActivityMapping.HEALTH_CENTER;
    } else if (this.human.hasSymptomsOfInfection()) {
      return ActivityMapping.STAY_HOME;
    } else if (this.minuteInDay >= (8 * 60) && this.minuteInDay <= (18 * 60)) {
      if (time.currentDayInWeek(currentStep) < 6) {
        if (random.nextDouble() < 0.1) { // 1% chance stay home
          return ActivityMapping.STAY_HOME;
        } else if (this.human.isWorker()) {
          activity = ActivityMapping.WORK;
        } else if (this.human.isStudent() && this.minuteInDay >= (8 * 60) && this.minuteInDay <= (12 * 60)) {
          activity = ActivityMapping.SCHOOL;
        }
      } else {
        // TODO: Melhorias sobre as atividades entraria aqui!
        if (dadaab.random.nextDouble() < 0.8) { // 80% chance
          if (dadaab.random.nextDouble() < 0.4) { // 40% chance
            return ActivityMapping.RELIGION_ACTIVITY;
          } else if (dadaab.random.nextDouble() < 0.7) { // 70% chance
            return ActivityMapping.SOCIAL_VISIT;
          } else {
            return ActivityMapping.MARKET;
          }
          // TODO: Adicionar atividade de ir ao mercado
        } else { // 20% chance of home activity
          return ActivityMapping.STAY_HOME;
        }
      }
    }
    return activity;
  }

  public Building bestActivityLocation(Human ref, Building position, ActivityMapping activityMapping, YellowFever d) {
    switch (activityMapping) {
    case STAY_HOME:
      return ref.getHome();
    case WORK:
      return betstLocation(ref.getHome(), d.getWorks(), d);
    case SCHOOL:
      return betstLocation(ref.getHome(), d.getSchooles(), d);
    case RELIGION_ACTIVITY:
      return betstLocation(ref.getHome(), d.getMosques(), d);
    case MARKET:
      return betstLocation(ref.getHome(), d.getMarket(), d);
    case HEALTH_CENTER:
      return betstLocation(ref.getHome(), d.getHealthCenters(), d);
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
      period = minimumStay + random.nextInt(2 * MINUTE);
      break;
    case MARKET:
      // time at maximum unti 2 hours
      period = minimumStay + random.nextInt(2 * MINUTE);
      break;
    case HEALTH_CENTER:
      // time at maximum unti 2 hours
      Building goal = this.human.getGoal();
      if (goal.getFacility().isReachedCapacity(goal, dadaab)) {
        period = minimumStay + random.nextInt(2 * MINUTE);
        this.human.getGoal().addPatient();
      } else {
        period = 0;
      }
      break;
    }
    return (period + this.minuteInDay);
  }

  public void doActivity(Building f, ActivityMapping activityMapping, YellowFever dadaab) {
    switch (activityMapping) {
    case STAY_HOME:
      break;
    case HEALTH_CENTER:
      this.human.receiveTreatment();
      break;
    default:

    }
  }

  private Building betstLocation(Building fLoc, Bag fieldBag, YellowFever d) {
    Bag newLoc = new Bag();
    double bestScoreSoFar = Double.POSITIVE_INFINITY;
    for (int i = 0; i < fieldBag.numObjs; i++) {
      Building potLoc = ((Building) fieldBag.objs[i]);
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
    Building f = null;
    if (newLoc != null) {
      int winningIndex = 0;
      if (newLoc.numObjs >= 1) {
        winningIndex = d.random.nextInt(newLoc.numObjs);
      }
      // System.out.println("other" + newLoc.numObjs);
      f = (Building) newLoc.objs[winningIndex];
    }
    return f;
  }

  public Building getNextTile(YellowFever dadaab, Building subgoal, Building position) {
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
    Building xmove = ((Building) dadaab.allCamps.field[position.getLocationX() + moveX][position.getLocationY()]);
    Building ymove = ((Building) dadaab.allCamps.field[position.getLocationX()][position.getLocationY() + moveY]);

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

  private Building socialize(Human ref, YellowFever d) {
    Bag potential = new Bag();
    Building newLocation = null;
    potential.clear();

    int camp = ref.getHome().getCampID(); // get camp id
    // select any camp site but not the camp that belong to the agent
    for (Object campsite : d.getFamilyHousing()) {
      Building cmp = ((Building) campsite);
      if (cmp.getCampID() == camp && cmp.equals(ref.getHome()) != true && cmp.getRefugeeHH().numObjs > 0) {
        potential.add(cmp); // potential locations to visit
      }
    }

    if (potential.numObjs == 1) {
      newLocation = (Building) potential.objs[0];
    } else {
      newLocation = (Building) potential.objs[d.random.nextInt(potential.numObjs)];
    }

    return newLocation;
  }

  private boolean gettingMedicalHelp() {
    if (this.human.hasSymptomsOfInfection() && !this.human.getReceivedTreatment())
      if (random.nextDouble() <= 0.5) // 50% chance
        return true;
    return false;
  }

  public Human getRefugee() {
    return human;
  }

  public void setRefugee(Human refugee) {
    this.human = refugee;
  }

}
