package com.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.core.Dadaab;
import com.core.TimeManager;
import com.model.enumeration.ActivityMapping;

import ec.util.MersenneTwisterFast;
import sim.util.Bag;

public class Activity {

  private double priorty = 0.0;
  private Refugee refugee;
  private TimeManager time;
  private int currentStep;
  private int minuteInDay;
  private MersenneTwisterFast random;

  public Activity(Refugee refugee, TimeManager time, int currentStep, MersenneTwisterFast random, int minuteInDay) {
    this.refugee = refugee;
    this.time = time;
    this.currentStep = currentStep;
    this.random = random;
    this.minuteInDay = minuteInDay;
  }

  // best location is mainly determine by distance
  // near is best
  public FieldUnit bestActivityLocation(Refugee ref, FieldUnit position, ActivityMapping activityMapping, Dadaab d) {
    switch (activityMapping) {
    case STAY_HOME:
      return ref.getHome();
    case WORK:
      // TODO: BoreHoles podem ser considerados os locais de trabalho?
      return betstLoc(ref.getHome(), d.boreHoles, d);
    case SCHOOL:
      return betstLoc(ref.getHome(), d.schooles, d);
    case RELIGION_ACTIVITY:
      System.out.println("RELIGION_ACTIVITY");
      return betstLoc(ref.getHome(), d.mosques, d);
    case MARKET:
      System.out.println("MARKET");
      return betstLoc(ref.getHome(), d.market, d);
    case HEALTH_CENTER:
      return betstLoc(ref.getHome(), d.healthCenters, d);
    case SOCIAL_VISIT:
      System.out.println("MARKET");
      return socialize(ref, d);
    default:
      return ref.getHome();
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

  // Haiti project
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
    // ((FieldUnit) o).loc

    // can either move in Y direction or X direction: see which is better
    FieldUnit xmove = ((FieldUnit) dadaab.allCamps.field[position.getLocationX() + moveX][position.getLocationY()]);
    FieldUnit ymove = ((FieldUnit) dadaab.allCamps.field[position.getLocationX()][position.getLocationY() + moveY]);

    boolean xmoveToRoad = ((Integer) dadaab.roadGrid.get(xmove.getLocationX(), xmove.getLocationY())) > 0;
    boolean ymoveToRoad = ((Integer) dadaab.roadGrid.get(ymove.getLocationX(), ymove.getLocationX())) > 0;

    if (moveX == 0 && moveY == 0) { // we are ON the subgoal, so don't move at all!
      // both are the same result, so just return the xmove (which is identical)
      return xmove;
    } else if (moveX == 0) // this means that moving in the x direction is not a valid move: it's +0
    {
      return ymove;
    } else if (moveY == 0) // this means that moving in the y direction is not a valid move: it's +0
    {
      return xmove;
    } else if (xmoveToRoad == ymoveToRoad) { // equally good moves: pick randomly between them
      if (dadaab.random.nextBoolean()) {
        return xmove;
      } else {
        return ymove;
      }
    } else if (xmoveToRoad && moveX != 0) // x is a road: pick it
    {
      return xmove;
    } else if (ymoveToRoad && moveY != 0)// y is a road: pick it
    {
      return ymove;
    } else if (moveX != 0) // move in the better direction
    {
      return xmove;
    } else if (moveY != 0) // yes
    {
      return ymove;
    } else {
      return ymove; // no justification
    }
  }

  // three camp sites in the model
  // agent select one camp which is not their camp randomly
  private FieldUnit socialize(Refugee ref, Dadaab d) {

    Bag potential = new Bag();
    FieldUnit newLoc = null;
    potential.clear();

    // socialize - visit friend or any place in
    // potential = d.campSites;

    int camp = ref.getHome().getCampID(); // get camp id

    // select any camp site but not the camp that belong to the agent
    for (Object campsite : d.campSites) {
      FieldUnit cmp = ((FieldUnit) campsite);
      if (cmp.getCampID() == camp && cmp.equals(ref.getHome()) != true && cmp.getRefugeeHH().numObjs > 0) {
        potential.add(cmp); // potential locations to visit
      }
    }

//        if(potential.isEmpty() ==true){
//            System.out.println("empty");
//        }
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
    }

    else {
      preference_borehole = (1.0 / (1.0 + Math.log(1 + f.distanceTo(nearestBorehole(f, d)))))
          * d.getParams().getGlobal().getWaterSourcePreference_Borehole() + (0.2 * d.random.nextDouble());
    }

    if (preference_river > preference_borehole) {
      fieldP = nearestWaterPoint(f, d);
    }

    else {
      fieldP = nearestBorehole(f, d);
    }
    return fieldP;

  }

  class ActivityPriority implements Comparable<ActivityPriority> {
    double priority = 0.0;
    ActivityMapping activityMapping;

    // TODO: Isto deve ser testado
    public int compareTo(ActivityPriority activity) {
      if (priority > activity.priority)
        return 1;
      else if (priority < activity.priority)
        return -1;
      return 0;
    }
  }

  // check the crowded level on the road or at your goal location
  // this method is taken from Haiti project
  /*
   * activity selection currently is made by simple assumption that consider age,
   * sex, need and time in most cases based on these each activity is given some
   * weight and the best of all will e selected
   */
  public ActivityMapping calculateActivityWeight() {
    // is active
    if (this.refugee.isInfected()) {
      ActivityPriority health = new ActivityPriority();
      health = healthActivityWeight();
      // TODO: Faz sentido existe uma probabilidade de procurar ajuda médica?
      // TODO: Sim, levando em consideração que nem todos buscam ajuda imediata
      return (health.priority < 0.3) ? ActivityMapping.STAY_HOME : ActivityMapping.HEALTH_CENTER;
    } else if (this.minuteInDay >= (8 * 60) && this.minuteInDay <= (18 * 60)) {
      if (time.currentDayInWeek(currentStep) < 5) {
        if (this.refugee.isStudent()) {
          return ActivityMapping.SCHOOL;
        } else {
          return ActivityMapping.WORK;
        }
      } else {
        List<ActivityPriority> activities = new ArrayList<ActivityPriority>(7);
        activities.add(mosqueActivityWeight());// 0.1;
        activities.add(marketActivityWeight());// 0.07;
        activities.add(socialVisitActivityWeight());// 0.08;

        Collections.sort(activities);

        return activities.get(4).activityMapping;
      }
    } else {
      return ActivityMapping.STAY_HOME;
    }
  }

  // TODO: O agente deve ir ao médico sempre quando não estiver ativo
  // TODO: A porcentagem deve ser desconsiderada
  private ActivityPriority healthActivityWeight() {
    ActivityPriority activity = new ActivityPriority();
    activity.activityMapping = ActivityMapping.HEALTH_CENTER;
    if (this.refugee.isInfected() && this.refugee.getIsrecieveTreatment() == false) {
      activity.priority = 0.8 + 0.2 * this.random.nextDouble();
    } else if (this.random.nextDouble() < 0.05) {
      activity.priority = 0.5 + 0.5 * this.random.nextDouble();
    } else {
      activity.priority = this.random.nextDouble() * (0.1 + 0.2 * this.random.nextDouble());
    }
    return activity;
  }

  private ActivityPriority marketActivityWeight() {
    ActivityPriority activity = new ActivityPriority();
    activity.activityMapping = ActivityMapping.MARKET;
    //
    if (this.refugee.getAge() > 15 && this.minuteInDay < (16 * 60)) {
      activity.priority = 0.7 * Math.sin(this.refugee.getAge()) + 0.2 * this.random.nextDouble();
    } else {
      activity.priority = 0;
    }
    activity.priority = activity.priority * this.random.nextDouble();
    return activity;
  }

  private ActivityPriority mosqueActivityWeight() {
    ActivityPriority activity = new ActivityPriority();
    activity.activityMapping = ActivityMapping.RELIGION_ACTIVITY;
    // worship time
    if (this.refugee.getAge() > 10) {
      if (this.minuteInDay > (60 * 5) && this.minuteInDay < (60 * 6)
          || this.minuteInDay > (60 * 12) && this.minuteInDay < (60 * 14)
          || this.minuteInDay > (60 * 15) && this.minuteInDay < (60 * 17)) {

        if (this.refugee.getHome().getCampID() == 1 && this.minuteInDay > 60 * 14) {
          activity.priority = 0.4 * (this.refugee.getAge() / 150.0) + 0.2 * this.random.nextDouble();
        } else
          activity.priority = 0.5 * (this.refugee.getAge() / 150.0) + 0.4 * this.random.nextDouble();
      }
    } else {
      activity.priority = 0.0;
    }
    // visiting other camp should be in the monring only - if it afternoon - agent
    // will get late to return??
    activity.priority = activity.priority * this.random.nextDouble();
    return activity;
  }

  private ActivityPriority socialVisitActivityWeight() {
    ActivityPriority activity = new ActivityPriority();
    activity.activityMapping = ActivityMapping.SOCIAL_VISIT;
    if (this.refugee.getAge() > 15 && this.minuteInDay < (16 * 60)) {
      activity.priority = 0.3 * (this.refugee.getAge() / 100.0) + 0.4 * this.random.nextDouble();
    } else {
      activity.priority = 0;
    }
    activity.priority = activity.priority * this.random.nextDouble();
    return activity;
  }

  public double getPriorty() {
    return priorty;
  }

  public void setPriorty(double priorty) {
    this.priorty = priorty;
  }

  public Refugee getRefugee() {
    return refugee;
  }

  public void setRefugee(Refugee refugee) {
    this.refugee = refugee;
  }
}
