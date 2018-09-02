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
  private boolean isStudent;
  private FieldUnit currentPosition;
  private FieldUnit home;
  private FieldUnit goal;
  private int currentStep;
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
  private Family hh;
  private ActivityMapping currentActivity;

  public static final int ORDERING = 2;
  protected Stoppable stopper;
  public int stayingTime;
  // infection info
  private boolean bitten;
  public boolean isrecieveTreatment = false;
  private double bodyResistance;// after infection how long stay alive- depreciate as cholera progress
  private int infectionPeriod; // time after first infection to show syptom
  private int recoveryPeriod;

  private int symtomaticType; // either sympmatic==1 or asymptomatic =2
  // private double protectiveImmunity; // after recovery agent will not likely infected immediately
  // but immunity will decay over time
  private Dadaab d;
  public int minuteInDay;
  private TimeManager time;// time contorler-identify the hour, day, week
  private ArrayList<FieldUnit> path = null; // the agent's current path to its current goal
  private MersenneTwisterFast random;

  public Refugee(int age, int sex, Family hh, FieldUnit home, FieldUnit position, MersenneTwisterFast seed,
      Continuous2D allRefugees) {
    this.setAge(age);
    this.setSex(sex);
    this.setFamily(hh);
    this.setHome(home);
    this.setGoal(home);
    this.jitterX = seed.nextDouble();
    this.jitterY = seed.nextDouble();
    this.setCurrentPosition(position);
    this.setPreviousHealthStatus(HealthStatus.SUSCEPTIBLE);
    time = new TimeManager();
    d = null;
    currentStep = 0;
    this.setBitten(false);
    infectionPeriod = 0;
    recoveryPeriod = 0;
    minuteInDay = 0;
    random = seed;
    // stayingTime = 0;
    // frequencyLaterine = 1;
    allRefugees.setObjectLocation(this,
        new Double2D(hh.getCampLocation().getX() + jitterX, hh.getCampLocation().getY() + jitterY));
  }

  // TODO: Importante rever este conceito para a febre amarela
  public void healthDepretiation() {
    if (this.isInfected()) {
      // childern may die sooner than old people
      this.setBodyResistance(
          this.getBodyResistance() - (d.getParams().getGlobal().getHealthDepreciation() * (1 / Math.pow(this.age, 2))));
    }
  }

  // TODO: Importante rever estes conceitos relacionados a saúde
  public void infected() {
    if (this.getCurrentHealthStatus().equals(HealthStatus.EXPOSED)) {
      this.infectionPeriod++;
      // TODO: tempo de incupação medio da febre amarela
      if (this.infectionPeriod > 4) {
        // TODO: Calcular probabilidade de ficar em algum caso de infecção
        if (d.random.nextDouble() <= 0.5) {
          this.setCurrentHealthStatus(HealthStatus.MILD_INFECTION); // immediately infected
        } else if (d.random.nextDouble() <= 0.3) {
          this.setCurrentHealthStatus(HealthStatus.SEVERE_INFECTION); // immediately infected
        }
      }
    } else if (this.getCurrentHealthStatus().equals(HealthStatus.SEVERE_INFECTION)) {
      // TODO: Considerar o caso grave da infecção TOXIC_INFECTION
    }
  }

  // assign the best goal
  public void calcGoal() {
    if (this.getCurrentPosition().equals(this.getHome()) == true) {
      Activity activity = new Activity(this, time, currentStep, random, minuteInDay);
      ActivityMapping bestActivity = activity.calculateActivityWeight(); // select the best goal
      this.setGoal(activity.bestActivityLocation(this, this.getHome(), bestActivity, d)); // search the best location of
      // your selected activity
      this.setCurrentActivity(bestActivity); // track current activity - for the visualization
      this.setStayingTime(stayingPeriod(this.getCurrentActivity()));
      return;
    } // from goal to home
    else if (this.getCurrentPosition().equals(this.getGoal()) == true
        && this.getGoal().equals(this.getHome()) != true) {
      this.setGoal(this.getHome());
      this.setStayingTime(stayingPeriod(ActivityMapping.STAY_HOME));
      this.setCurrentActivity(ActivityMapping.STAY_HOME);
      return;
    } // incase
    else {
      this.setGoal(this.getHome());
      this.setCurrentActivity(ActivityMapping.STAY_HOME);
      return;
    }
  }

  // where to move
  public void move(int steps) {
    // if you do not have goal- return
    if (this.getGoal() == null) {
      // this.setGoal(this.getHome());
      return;
    } else if (this.getCurrentPosition().equals(this.getGoal()) == true && this.getGoal().equals(this.getHome()) != true
        && isStay() == true) {
      return;
    }
    // at your goal- do activity and recalulate goal
    else if (this.getCurrentPosition().equals(this.getGoal()) == true) {
      doActivity(this.getGoal(), this.getCurrentActivity());
      if (steps % 1440 < 17) {
        if (random.nextDouble() > 0.3) {
          calcGoal();
        }
      } else {
        calcGoal();
      }
    } // else move to your goal
    else {
      // make sure we have a path to the goal!
      if (path == null || path.size() == 0) {
        path = AStar.astarPath(d,
            (Node) d.closestNodes.get(this.getCurrentPosition().getX(), this.getCurrentPosition().getY()),
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
        if (path.get(0).equals(this.getCurrentPosition())) {
          path.remove(0);
        }
        // our current subgoal is the end of the current edge
        if (path.size() > 0) {
          subgoal = path.get(0);
        } else {
          subgoal = this.getGoal();
        }
      }

      Activity current = new Activity(this, time, currentStep, random, minuteInDay);
      FieldUnit loc = current.getNextTile(d, subgoal, this.getCurrentPosition());
      FieldUnit oldLoc = this.getCurrentPosition();
      oldLoc.removeRefugee(this);

      this.setCurrentPosition(loc);
      loc.addRefugee(this);
      d.allRefugees.setObjectLocation(this, new Double2D(loc.getX() + this.jitterX, loc.getY() + jitterY));
    }
  }

  /* define go to activity in relation current hour */
  public ActivityMapping goActivity(Dadaab dadaab) {
    // TODO: está ativo?
    if (this.minuteInDay >= (8 * 60) && this.minuteInDay <= (18 * 60)) {
      if ((time.currentDayInWeek(currentStep) > 5)) {
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

  public boolean isInfected() {
    if (this.getCurrentHealthStatus().equals(HealthStatus.MILD_INFECTION)) {
      return true;
    } else if (this.getCurrentHealthStatus().equals(HealthStatus.SEVERE_INFECTION)) {
      return true;
    } else if (this.getCurrentHealthStatus().equals(HealthStatus.TOXIC_INFECTION)) {
      return true;
    }
    return false;
  }

  public void doActivity(FieldUnit f, ActivityMapping activityMapping) {
    switch (activityMapping) {
    default:
    case STAY_HOME:
      break;
    case HEALTH_CENTER:
      receiveTreatment(f, d);
      break;
    case VISIT_SOCIAL:
      if (random.nextDouble() < d.getParams().getGlobal().getProbabilityGuestContaminationRate()) {
      }
      break;
    }
  }

  // TODO: Importante rever as atividades
  // TODO: Deve ser incluido o tempo relativo ao tratamento médico
  public int stayingPeriod(ActivityMapping activityMapping) {
    int period = 0;
    int minStay = 20; // minumum time to stay in any facility
    int maxStay = 180; // three hour
    int currentMinute = minuteInDay;

    switch (activityMapping) {
    case STAY_HOME:
      period = maxStay;
      break;
    case SCHOOL:
      // time at school max until 4;00pm
      if (currentMinute + maxStay + 120 > (17 * 60)) {
        period = minStay;
      } else
        period = maxStay + 120;
      break;
    case WORK:
      // TODO: tempo de permanencia do trabalho
    case VISIT_SOCIAL:
      // time vist camp 2 hour
      if (currentMinute + maxStay > (12 * 60)) {
        period = minStay;
      } else
        period = minStay + random.nextInt(maxStay - 60);
      break;
    case RELIGION_ACTIVITY:
      // time staying at mosq max 80 minute
      if (currentMinute + maxStay > (16 * 60)) {
        period = minStay;
      } else
        period = minStay + random.nextInt(maxStay);
      break;
    case MARKET:
      // time at the market max 5 hour
      if (currentMinute + maxStay > (12 * 60)) {
        period = minStay;
      } else
        period = minStay + random.nextInt(maxStay);
      break;
    case HEALTH_CENTER:
      // TODO:
    }
    return (period + currentMinute);
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

  public void receiveTreatment(FieldUnit f, Dadaab d) {
    // based on the capacity of the
    if (this.isInfected() && f.getFacility().isReachedCapacity(f, d) == false) {
      f.setPatientCounter(f.getPatientCounter() + 1);
      if (random.nextDouble() < d.getParams().getGlobal().getprobabilityOfEffectiveNessofmedicine()) {
        int recovery = currentStep + (400 + random.nextInt(1440));
        this.setIsrecieveTreatment(true);
        this.setRecoveryPeriod(recovery);
        this.setBodyResistance(1.0);
      }
    }
  }

  public void step(SimState state) {
    d = (Dadaab) state;
    currentStep = (int) d.schedule.getSteps();

    if (currentStep < 1440) {
      minuteInDay = currentStep;
    } else {
      minuteInDay = currentStep % 1440;
    }

    // TODO: Importante rever estes conceitos relacionados a saúde
    this.setPreviousHealthStatus(this.getCurrentHealthStatus());
    if (this.getCurrentHealthStatus().equals(HealthStatus.EXPOSED)) {
      infected();
    }

    if (this.isInfected()) {
      if (this.getRecoveryPeriod() == currentStep && this.getIsrecieveTreatment() == true) {
        this.setCurrentHealthStatus(HealthStatus.RECOVERED);
        this.setRecoveryPeriod(0);
        this.setBodyResistance(1.0);
        this.setIsrecieveTreatment(false);
      }
    }

    if (random.nextDouble() < d.getParams().getGlobal().getProbRecoveryToSuscebtable()
        && this.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
      this.setCurrentHealthStatus(HealthStatus.RECOVERED);
    }

    healthDepretiation();

    // death
    if (this.getBodyResistance() <= 0) {
      d.killrefugee(this);
    }

    move(currentStep);
  }

  public double doubleValue() {
    switch (this.currentHealthStatus) {
    case SUSCEPTIBLE:
      return 1;
    case EXPOSED:
      return 2;
    case RECOVERED:
      return 4;
    default:
      return 3;
    }
  }

  public void setStoppable(Stoppable stopp) {
    stopper = stopp;
  }

  public void stop() {
    stopper.stop();
  }

  private void setCurrentPosition(FieldUnit position) {
    this.currentPosition = position;
  }

  public FieldUnit getCurrentPosition() {
    return currentPosition;
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
  public void setStudent(boolean student) {
    this.isStudent = student;
  }

  public boolean isStudent() {
    return isStudent;
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
  public void setCurrentActivity(ActivityMapping activityMapping) {
    this.currentActivity = activityMapping;
  }

  public ActivityMapping getCurrentActivity() {
    return currentActivity;
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
    this.infectionPeriod = inf;
  }

  public int getInfectionPeriod() {
    return infectionPeriod;
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

  public HealthStatus getCurrentHealthStatus() {
    return currentHealthStatus;
  }

  public void setCurrentHealthStatus(HealthStatus healthStatus) {
    this.currentHealthStatus = healthStatus;
  }

  public boolean isBitten() {
    return bitten;
  }

  public void setBitten(boolean bitten) {
    this.bitten = bitten;
  }
}
