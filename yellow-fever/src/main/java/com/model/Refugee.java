package com.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.core.Dadaab;
import com.core.Node;
import com.core.TimeManager;
import com.core.algorithms.AStar;
import com.model.enumeration.ActivityMapping;
import com.model.enumeration.HealthStatus;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
/**
 *
 * @author gmu
 */
import sim.util.Double2D;
import sim.util.Valuable;

public class Refugee implements Steppable, Valuable, Serializable {

  private int age;
  private int sex;
  private int studyID; // 0 = not enrolled 1 = enrolled,
  private FieldUnit position; // current position
  private FieldUnit home;// home
  private FieldUnit goal; // location of the goal
  public int cStep;
  private double jitterX; // Visualization
  private double jitterY;
  private HealthStatus currentHealthStatus;// health status - susceptable - 1, exposed - 2, infected-3, recovered - 4
  private HealthStatus previousHealthStatus; // monitors health status of agent in the previous step - to capture the
                                             // change
  // in each day
  // private int frequencyLaterine; // once in a day for health agent- infected
  // agent may go up to 10 times more
  // Nochola et al - symptomic patient may lose 1 litre/hour floud for 2-3 weeks -
  // but asymptomic - 1l/day
  Family hh;
  private int currentAct;

  // private int[] activityAccomplished = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  // if accomplsh0 if not 1

  public static final int ORDERING = 2;
  protected Stoppable stopper;
  // cholera info
  public boolean isrecieveTreatment = false;
  public int stayingTime;
  private double bodyResistance;// after infection how long stay alive- depreciate as cholera progress
  private int infectionPerdiod; // time after first infection to show syptom
  private int recoveryPeriod;

  private int symtomaticType; // either sympmatic==1 or asymptomatic =2
//    private double protectiveImmunity; // after recovery agent will not likely infected immediately
  // but immunity will decay over time
  Dadaab d;
  //
  public int minuteInDay;
  //
  TimeManager tm;// time contorler-identify the hour, day, week
  ArrayList<FieldUnit> path = null; // the agent's current path to its current goal
  MersenneTwisterFast randomN;

  public Refugee(int age, int sex, Family hh, FieldUnit home, FieldUnit position, MersenneTwisterFast random,
      Continuous2D allRefugees) {
    this.setAge(age);
    this.setSex(sex);
    this.setFamily(hh);
    this.setHome(home);
    this.setGoal(home);
    this.jitterX = random.nextDouble();
    this.jitterY = random.nextDouble();
    this.setPosition(position);
    this.setPreviousHealthStatus(HealthStatus.SUSCEPTIBLE);
    tm = new TimeManager();
    d = null;
    cStep = 0;
    infectionPerdiod = 0;
    recoveryPeriod = 0;
    minuteInDay = 0;
    randomN = random;
    // stayingTime = 0;
    // frequencyLaterine = 1;
    allRefugees.setObjectLocation(this,
        new Double2D(hh.getCampLocation().getX() + jitterX, hh.getCampLocation().getY() + jitterY));
  }

  public void healthDepretiation() {
    if (this.isInfected()) {
      // childern may die sooner than old people
      this.setBodyResistance(
          this.getBodyResistance() - (d.getParams().getGlobal().getHealthDepreciation() * (1 / Math.pow(this.age, 2))));
    }
  }

  // TODO: Importante rever estes conceitos relacionados a saúde
  public void infected() {
    // now you are officially infected - will show sympom
    if (this.getHealthStatus().equals(HealthStatus.EXPOSED)) {
      if (cStep == this.getInfectionPeriod()) {
        if (this.getSymtomaticType() == 2) { // asymtomatic paitient
          this.setHealthStatus(HealthStatus.RECOVERED);// recovered
          this.setInfectionPeriod(0);
        } else {
          // TODO: Infecção deve levar em consideração probabilidade
          this.setHealthStatus(HealthStatus.MILD_INFECTION); // immediately infected
          this.setInfectionPeriod(0);
        }
      }
    }
  }

  // assign the best goal
  public void calcGoal() {

    if (this.getPosition().equals(this.getHome()) == true) {
      int cAct = actSelect(); // select the best goal
      Activity act = new Activity();
      this.setGoal(act.bestActivityLocation(this, this.getHome(), cAct, d)); // search the best location of your
                                                                             // selected activity
      this.setCurrentActivity(cAct); // track current activity - for the visualization
      this.setStayingTime(stayingPeriod(this.getCurrentActivity()));

      return;

    } // from goal to home
    else if (this.getPosition().equals(this.getGoal()) == true && this.getGoal().equals(this.getHome()) != true) {

      this.setGoal(this.getHome());
      this.setStayingTime(stayingPeriod(0));
      this.setCurrentActivity(0);
      return;
      //
    } // incase
    else {
      this.setGoal(this.getHome());
      this.setCurrentActivity(0);
      return;
    }
  }

  // where to move
  public void move(int steps) {

    // if you do not have goal- return
    if (this.getGoal() == null) {
      // this.setGoal(this.getHome());
      return;
    } else if (this.getPosition().equals(this.getGoal()) == true && this.getGoal().equals(this.getHome()) != true
        && isStay() == true) {
      return;
    }
    // at your goal- do activity and recalulate goal
    else if (this.getPosition().equals(this.getGoal()) == true) {

      doActivity(this.getGoal(), this.getCurrentActivity());
      if (steps % 1440 < 17) {
        if (randomN.nextDouble() > 0.3) {
          calcGoal();
        }
      } else {
        calcGoal();
      }
    } // else move to your goal
    else {

//                           make sure we have a path to the goal!
      if (path == null || path.size() == 0) {
        path = AStar.astarPath(d, (Node) d.closestNodes.get(this.getPosition().getX(), this.getPosition().getY()),
            (Node) d.closestNodes.get(this.getGoal().getX(), this.getGoal().yLoc));
        if (path != null) {
          path.add(this.getGoal());
        }
      }

      // determine the best location to immediately move *toward*
      FieldUnit subgoal;

      // It's possible that the agent isn't close to a node that can take it to the
      // center.
      // In that case, the A* will return null. If this is so the agent should move
      // toward
      // the goal until such a node is found.
      if (path == null) {
        subgoal = this.getGoal();
      } // Otherwise we have a path and should continue to move along it
      else {
        // have we reached the end of an edge? If so, move to the next edge
        if (path.get(0).equals(this.getPosition())) {
          path.remove(0);
        }

        // our current subgoal is the end of the current edge
        if (path.size() > 0) {
          subgoal = path.get(0);
        } else {
          subgoal = this.getGoal();
        }
      }

      Activity current = new Activity();
      FieldUnit loc = current.getNextTile(d, subgoal, this.getPosition());
      FieldUnit oldLoc = this.getPosition();
      oldLoc.removeRefugee(this);

      this.setPosition(loc);
      loc.addRefugee(this);
      d.allRefugees.setObjectLocation(this, new Double2D(loc.getX() + this.jitterX, loc.getY() + jitterY));
    }

  }

  // <editor-fold defaultstate="collapsed" desc="Activity Weights">
  private double schoolActivityWeight() {
    boolean isSchoolDay = (tm.currentDayInWeek(cStep) < 5); // school only open from monday to friday ( day 1 to 5 of
                                                            // the week)

    // if student second priority is school
    if (this.getStudyID() == 1 && isSchoolDay) {
      return 0.8 + 0.2 * randomN.nextDouble();
    } else {
      return 0;
    }
  }

  private double healthActivityWeight() {
    double wHealthC;
    if (this.isInfected() && this.getIsrecieveTreatment() == false) {
      wHealthC = 0.8 + 0.2 * randomN.nextDouble();

    } else if (randomN.nextDouble() < 0.05) {
      wHealthC = 0.5 + 0.5 * randomN.nextDouble();
    } else {
      wHealthC = randomN.nextDouble() * (0.1 + 0.2 * randomN.nextDouble());
    }
    return wHealthC;
  }

  private double foodActivityWeight() {

    // food distibution will take third
    // because ration is given on scheduled time, agent give priority for food at
    // tat day

    double wFoodDist;
    int foodDate = 1 + (tm.dayCount(cStep) % 9);
    int dummyFood = (foodDate == this.getFamily().getRationDate()) ? 1 : 0; // if the day is not a ration day, agent
                                                                            // will not go to food center
    if (dummyFood == 1 && this.getAge() > 15) {
      wFoodDist = 0.6 + 0.3 * randomN.nextDouble();

    } else {
      wFoodDist = 0.1 + 0.2 * randomN.nextDouble();
    }
    return wFoodDist * randomN.nextDouble();
  }

  private double marketActivityWeight() {
    double wMarket;
    //
    if (this.getAge() > 15 && minuteInDay < (16 * 60)) {
      wMarket = 0.7 * Math.sin(this.getAge()) + 0.2 * randomN.nextDouble();
    } else {
      wMarket = 0;
    }
    return wMarket * randomN.nextDouble();
  }

  private double mosqueActivityWeight() {
    // worship time
    double wMosque = 0.0;
    if (this.getAge() > 10) {

      if (minuteInDay > (60 * 5) && minuteInDay < (60 * 6) || minuteInDay > (60 * 12) && minuteInDay < (60 * 14)
          || minuteInDay > (60 * 15) && minuteInDay < (60 * 17)) {

        if (this.getHome().getCampID() == 1 && minuteInDay > 60 * 14) {
          wMosque = 0.4 * (this.getAge() / 150.0) + 0.2 * randomN.nextDouble();
        } else
          wMosque = 0.5 * (this.getAge() / 150.0) + 0.4 * randomN.nextDouble();
      }
    } else {
      wMosque = 0.0;
    }
    // visiting other camp should be in the monring only - if it afternoon - agent
    // will get late to return??
    return wMosque * randomN.nextDouble();
  }

  /* define go to activity in relation current hour */
  public ActivityMapping goActivity(Dadaab dadaab) {
    // TODO: está ativo?

    if (this.minuteInDay >= (8 * 60) && this.minuteInDay <= (18 * 60)) {
      if ((tm.currentDayInWeek(cStep) > 5)) {
        // TODO: realizar atividade de lazer
      } else {
        if (this.age > 16 && this.age < 65) {
          // TODO: frequentar a escolha
        } else {
          // TODO: frequentar o trabalho
        }
      }
    } else {
      // TODO: permanecer em casa
    }
    return null;
  }

  // check the crowded level on the road or at your goal location
  // this method is taken from Haiti project
  /*
   * activity selection currently is made by simple assumption that consider age,
   * sex, need and time in most cases based on these each activity is given some
   * weight and the best of all will e selected
   */
  public int actSelect() {
    int alpha = (60 * 6) + randomN.nextInt(60 * 3); // in minute - working hour start
    int beta = (60 * 17) + randomN.nextInt(120); // in minute working hour end
    boolean isDayTime = minuteInDay >= alpha && minuteInDay <= beta;
    if (!isDayTime) {
      double wHealth = 0;
      if (this.isInfected()) {
        wHealth = healthActivityWeight();
      } else {
        wHealth = 0;
      }
      return (wHealth < 0.3) ? Activity.STAY_HOME : Activity.HEALTH_CENTER;
    } else {
      double[] activityPriortyWeight = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
      activityPriortyWeight[1] = schoolActivityWeight();// 0.08;
      activityPriortyWeight[2] = mosqueActivityWeight();// 0.1;
      activityPriortyWeight[3] = marketActivityWeight();// 0.07;
      activityPriortyWeight[4] = foodActivityWeight();
      activityPriortyWeight[5] = healthActivityWeight();// 0.16;;//0.12;
      activityPriortyWeight[6] = visitRelativeActivityWeight();// 0.09;
      activityPriortyWeight[7] = socialVisitActivityWeight();// 0.08;

      int curAct = 0;

      // Find the activity with the heighest weight
      double maximum = activityPriortyWeight[0]; // start with the first value
      for (int i = 1; i < 8; i++) {
        if (activityPriortyWeight[i] > maximum) {
          maximum = activityPriortyWeight[i]; // new maximum
          curAct = i;
        }
      }

      // Maximum weight must be > 0.3, else stay home
      if (activityPriortyWeight[curAct] < 0.3) {
        curAct = Activity.STAY_HOME;
      }

      return curAct;
    }

  }

  public boolean isInfected() {
    if (this.getHealthStatus().equals(HealthStatus.MILD_INFECTION)) {
      return true;
    } else if (this.getHealthStatus().equals(HealthStatus.SEVERE_INFECTION)) {
      return true;
    } else if (this.getHealthStatus().equals(HealthStatus.TOXIC_INFECTION)) {
      return true;
    }
    return false;
  }

  private double visitRelativeActivityWeight() {
    double wSocialRel;
    //
    if (this.getAge() > 10 && minuteInDay < (16 * 60)) {
      wSocialRel = 0.3 * Math.sin(this.getAge()) + 0.4 * randomN.nextDouble();
    } else {
      wSocialRel = 0;
    }
    return wSocialRel * randomN.nextDouble();
  }

  private double socialVisitActivityWeight() {
    double wVisitSoc;
    if (this.getAge() > 18 && minuteInDay < (16 * 60)) {
      wVisitSoc = 0.3 * (this.getAge() / 100.0) + 0.4 * randomN.nextDouble();
    } else {
      wVisitSoc = 0;
    }
    return wVisitSoc * randomN.nextDouble();
  }

  public void doActivity(FieldUnit f, int activity) {
    switch (activity) {
    default:
    case Activity.STAY_HOME:
      break;
    case Activity.HEALTH_CENTER:
      recieveTreatment(f, d);
      break;
    case Activity.SOCIAL_RELATIVES:
      if (randomN.nextDouble() < d.getParams().getGlobal().getProbabilityGuestContaminationRate()) {
      }
      break;
    case Activity.VISIT_SOCIAL:
      if (randomN.nextDouble() < d.getParams().getGlobal().getProbabilityGuestContaminationRate()) {
      }
      break;
    }
  }

  public int stayingPeriod(int act) {
    int period = 0;
    int minStay = 20; // minumum time to stay in any facility
    int maxStay = 180; // three hour
    int curMin = minuteInDay;

    switch (act) {
    case 0:
      period = maxStay;
      break;
    case 1:
      // time at school max until 4;00pm
      if (curMin + maxStay + 120 > (17 * 60)) {
        period = minStay;
      } else
        period = maxStay + 120;
      break;
    case 2:
      // time borehole max 20 minute
      period = minStay + 20;
      break;
    case 3:
      // time staying at mosq max 80 minute
      if (curMin + maxStay > (16 * 60)) {
        period = minStay;
      } else
        period = minStay + randomN.nextInt(maxStay);
      break;
    case 4:
      // time at the market max 5 hour
      if (curMin + maxStay > (12 * 60)) {
        period = minStay;
      } else
        period = minStay + randomN.nextInt(maxStay);
      break;
    case 5:
      // time at food dist max 5 hour
      if (curMin + maxStay > (15 * 60)) {
        period = minStay;
      } else
        period = minStay + randomN.nextInt(maxStay);
      break;
    case 6:
      // depend on time quee
      period = 0;
//                if(curMin + maxStay >(18*60)){
//                   period = minStay;
//                }
//                else period = minStay  + random.nextInt(maxStay) ;
//                
      break;
    case 7:
      // time for social max until 5;00pm
      if (curMin + maxStay > (12 * 60)) {
        period = minStay;
      } else
        period = minStay + randomN.nextInt(maxStay);
      break;
    case 8:
      // time vist camp 2 hour
      if (curMin + maxStay > (12 * 60)) {
        period = minStay;
      } else
        period = minStay + randomN.nextInt(maxStay - 60);
      break;
    case 9:
      // time laterine max until 4;00pm
      period = minStay;
      break;
    }
    return (period + curMin);
  }

  // how long agent need to stay at location
  public boolean isStay() {
    // TimeManager tm = new TimeManager();
    boolean isStay = false;
    if (minuteInDay < this.getStayingTime()) {
      isStay = true;
    } else
      isStay = false;
    return isStay;

  }

  public void recieveTreatment(FieldUnit f, Dadaab d) {
    // based on the capacity of the

    if (this.isInfected() && f.getFacility().isReachedCapacity(f, d) == false) {
      f.setPatientCounter(f.getPatientCounter() + 1);
      if (randomN.nextDouble() < d.getParams().getGlobal().getprobabilityOfEffectiveNessofmedicine()) {
        int recovery = cStep + (400 + randomN.nextInt(1440));
        this.setIsrecieveTreatment(true);
        this.setRecoveryPeriod(recovery);
        this.setBodyResistance(1.0);
      }
    }
  }

  public void step(SimState state) {

    d = (Dadaab) state;
    cStep = (int) d.schedule.getSteps();

    if (cStep < 1440) {
      minuteInDay = cStep;
    } else {
      minuteInDay = cStep % 1440;
    }

    // TODO: Importante rever estes conceitos relacionados a saúde
    this.setPreviousHealthStatus(this.getHealthStatus());
    if (this.getHealthStatus().equals(HealthStatus.EXPOSED)) {
      infected();
    }

    if (this.isInfected()) {
      if (this.getRecoveryPeriod() == cStep && this.getIsrecieveTreatment() == true) {
        this.setHealthStatus(HealthStatus.RECOVERED);
        this.setRecoveryPeriod(0);
        this.setBodyResistance(1.0);
        this.setIsrecieveTreatment(false);
      }
    }

    if (randomN.nextDouble() < d.getParams().getGlobal().getProbRecoveryToSuscebtable()
        && this.getHealthStatus().equals(HealthStatus.RECOVERED)) {
      this.setHealthStatus(HealthStatus.RECOVERED);
    }

    healthDepretiation();

    // death
    if (this.getBodyResistance() <= 0) {
      d.killrefugee(this);
    }

    move(cStep);
  }

  public void setStoppable(Stoppable stopp) {
    stopper = stopp;
  }

  public void stop() {
    stopper.stop();
  }

  private void setPosition(FieldUnit position) {
    this.position = position;

  }

  public FieldUnit getPosition() {
    return position;
  }

  // goal position - where to go
  public void setGoal(FieldUnit position) {
    this.goal = position;

  }

  public FieldUnit getGoal() {
    return goal;
  }

  // home location
  public void setHome(FieldUnit home) {
    this.home = home;

  }

  public FieldUnit getHome() {
    return home;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public int getAge() {
    return age;
  }

  public void setSex(int sex) {
    this.sex = sex;
  }

  public int getSex() {
    return sex;
  }

  // education status- student or not
  public void setStudyID(int id) {
    this.studyID = id;
  }

  public int getStudyID() {
    return studyID;
  }

  // faimly memeber
  public void setFamily(Family hh) {
    this.hh = hh;
  }

  public Family getFamily() {
    return hh;
  }

  public void setPreviousHealthStatus(HealthStatus status) {
    this.previousHealthStatus = status;
  }

  public HealthStatus getPreviousHealthStatus() {
    return previousHealthStatus;
  }

  public void setSymtomaticType(int im) {
    this.symtomaticType = im;

  }

  public int getSymtomaticType() {
    return symtomaticType;
  }

  // current activity
  public void setCurrentActivity(int a) {
    this.currentAct = a;
  }

  public int getCurrentActivity() {
    return currentAct;
  }

  // resistance to show symptom after infection -
  public void setBodyResistance(double r) {
    this.bodyResistance = r;
  }

  public double getBodyResistance() {
    return bodyResistance;
  }

  // counts time after infection
  public void setInfectionPeriod(int inf) {
    this.infectionPerdiod = inf;
  }

  public int getInfectionPeriod() {
    return infectionPerdiod;
  }

  public void setRecoveryPeriod(int inf) {
    this.recoveryPeriod = inf;
  }

  public int getRecoveryPeriod() {
    return recoveryPeriod;
  }

  public void setIsrecieveTreatment(boolean tr) {
    isrecieveTreatment = tr;
  }

  public boolean getIsrecieveTreatment() {
    return isrecieveTreatment;
  }

  // counts time after infection
  public void setStayingTime(int sty) {
    this.stayingTime = sty;
  }

  public int getStayingTime() {
    return stayingTime;
  }

  public HealthStatus getHealthStatus() {
    return currentHealthStatus;
  }

  public void setHealthStatus(HealthStatus healthStatus) {
    this.currentHealthStatus = healthStatus;
  }

  public double doubleValue() {
    // TODO:
    return 0;
  }

}
