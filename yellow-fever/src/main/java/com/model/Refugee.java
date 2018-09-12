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
  private boolean isWorker;
  private boolean isStudent;
  private FieldUnit currentPosition;
  private FieldUnit home;
  private FieldUnit goal;
  private int currentStep;
  private double jitterX;
  private double jitterY;
  private HealthStatus currentHealthStatus;
  private HealthStatus previousHealthStatus;
  private Family family;
  private ActivityMapping currentActivity;

  public static final int ORDERING = 2;
  protected Stoppable stopper;
  public int stayingTime;
  // infection info
  private boolean vaccinated;
  private double bodyResistance;
  private int incubationPeriod;
  private int infectionPeriod;
  private int toxicPeriod;

  private Dadaab dadaab;
  public int minuteInDay;
  private TimeManager time;// time contorler-identify the hour, day, week
  private ArrayList<FieldUnit> path = null; // the agent's current path to its
                                            // current goal
  private MersenneTwisterFast random;

  public Refugee(int age, int sex, Family family, FieldUnit home, FieldUnit position, MersenneTwisterFast seed,
      Continuous2D allRefugees) {
    this.setAge(age);
    this.setSex(sex);
    this.setFamily(family);
    this.setHome(home);
    this.setGoal(home);
    this.jitterX = seed.nextDouble();
    this.jitterY = seed.nextDouble();
    this.currentPosition = position;
    this.previousHealthStatus = HealthStatus.SUSCEPTIBLE;
    this.time = new TimeManager();
    this.minuteInDay = 0;
    this.random = seed;
    this.dadaab = null;
    this.currentStep = 0;
    this.vaccinated = false;
    this.incubationPeriod = 0;
    this.infectionPeriod = 0;
    this.toxicPeriod = 0;
    // stayingTime = 0;
    // frequencyLaterine = 1;
    allRefugees.setObjectLocation(this, new Double2D(family.getCampLocation().getLocationX() + jitterX,
        family.getCampLocation().getLocationY() + jitterY));
  }

  // where to move
  public void move(int steps) {
    Activity activity = new Activity(this, time, currentStep, random, minuteInDay);
    // if you do not have goal then return
    if (this.getGoal() == null) {
      // this.setGoal(this.getHome());
      return;
    } else if (this.getCurrentPosition().equals(this.getGoal()) == true && this.getGoal().equals(this.getHome()) != true
        && this.isStay() == true) {
      return;
    }
    // at your goal- do activity and recalulate goal
    else if (this.getCurrentPosition().equals(this.getGoal()) == true) {
      activity.doActivity(this.getGoal(), this.getCurrentActivity(), dadaab);
      if (steps % 1440 < 17) { // TODO: Qual é a necessidade disto? Parece
                               // relacionado a hora
        if (random.nextDouble() > 0.3) { // TODO: Qual é a necessidade disto?
          calculateGoal();
        }
      } else {
        calculateGoal();
      }
    } // else move to your goal
    else {
      // make sure we have a path to the goal!
      if (path == null || path.size() == 0) {
        path = AStar.astarPath(dadaab,
            (Node) dadaab.closestNodes.get(this.getCurrentPosition().getLocationX(),
                this.getCurrentPosition().getLocationY()),
            (Node) dadaab.closestNodes.get(this.getGoal().getLocationX(), this.getGoal().getLocationY()));
        if (path != null) {
          path.add(this.getGoal());
        }
      }
      // determine the best location to immediately move *toward*
      FieldUnit subgoal;
      // It's possible that the agent isn't close to a node that can take it to
      // the
      // center.
      // In that case, the A* will return null. If this is so the agent should
      // move
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

      FieldUnit loc = activity.getNextTile(dadaab, subgoal, this.getCurrentPosition());
      FieldUnit oldLoc = this.getCurrentPosition();
      oldLoc.removeRefugee(this);

      this.setCurrentPosition(loc);
      loc.addRefugee(this);
      dadaab.allRefugees.setObjectLocation(this,
          new Double2D(loc.getLocationX() + this.jitterX, loc.getLocationY() + jitterY));
    }
  }

  // assign the best goal
  public void calculateGoal() {
    if (this.getCurrentPosition().equals(this.getHome()) == true) {
      Activity activity = new Activity(this, time, currentStep, random, minuteInDay);
      ActivityMapping bestActivity = activity.defineActivity(dadaab); // select
                                                                      // the
                                                                      // best
                                                                      // goal
      this.setGoal(activity.bestActivityLocation(this, this.getHome(), bestActivity, dadaab)); // search
                                                                                               // the
                                                                                               // best
      // your selected activity
      this.setCurrentActivity(bestActivity); // track current activity - for the
                                             // visualization
      this.setStayingTime(activity.stayingPeriod(this.getCurrentActivity()));
      return;
    } // from goal to home
    else if (this.getCurrentPosition().equals(this.getGoal()) == true
        && this.getGoal().equals(this.getHome()) != true) {
      this.setGoal(this.getHome());
      // this.setStayingTime(activity.stayingPeriod(ActivityMapping.STAY_HOME));
      this.setCurrentActivity(ActivityMapping.STAY_HOME);
      return;
    } // incase
    else {
      this.setGoal(this.getHome());
      this.setCurrentActivity(ActivityMapping.STAY_HOME);
      return;
    }
  }

  // how long agent need to stay at location
  public boolean isStay() {
    if (this.minuteInDay < this.getStayingTime()) {
      return true;
    } else
      return false;
  }

  public void infected() {
    this.incubationPeriod = 3 + dadaab.random.nextInt(10);
    this.currentHealthStatus = HealthStatus.EXPOSED;
  }

  public void currentStateOfInfection() {
    if (this.incubationPeriod == 0 && HealthStatus.EXPOSED.equals(this.currentHealthStatus)) {
      if (dadaab.random.nextInt(10) <= 9) { // 90% of cases are mild
        this.setCurrentHealthStatus(HealthStatus.MILD_INFECTION);
        this.definePeriodOfInfection();
      } else {
        this.setCurrentHealthStatus(HealthStatus.SEVERE_INFECTION);
        this.definePeriodOfInfection();
      }
    } else if (this.incubationPeriod == 0 && HealthStatus.SEVERE_INFECTION.equals(this.currentHealthStatus)) {
      // TODO: Com a probabilidade X o agente acaba piorando o estado
      // TODO: Valor aleatório para testes
      if (dadaab.random.nextInt(10) <= 1) { // 10% of cases are toxic
        this.currentHealthStatus = HealthStatus.TOXIC_INFECTION;
      } else {
        this.currentHealthStatus = HealthStatus.MILD_INFECTION;
      }
    } else {
      this.incubationPeriod--;
    }
  }

  public boolean isPeriodOfInfection() {
    if (this.infectionPeriod > 0 || this.toxicPeriod > 0) {
      return true;
    } else {
      return false;
    }
  }

  public void definePeriodOfInfection() {
    this.infectionPeriod = 3 + dadaab.random.nextInt(4);
  }

  public void definePeriodOfToxicInfection() {
    this.toxicPeriod = 8;
  }

  // TODO: Importante rever este conceito para a febre amarela
  // TODO: Considerar periodo infeccioso como agravante?
  public void healthDepretiation() {
    if (HealthStatus.isInfected(this.currentHealthStatus)) {
      // childern may die sooner than old people
      this.setBodyResistance(this.getBodyResistance()
          - (dadaab.getParams().getGlobal().getHealthDepreciation() * (1 / Math.pow(this.age, 2))));
    }

    if (HealthStatus.TOXIC_INFECTION.equals(this.currentHealthStatus) && this.isPeriodOfInfection()) {
      if (dadaab.random.nextDouble() > 0.5) { // 50-50 chance
        this.currentHealthStatus = HealthStatus.RECOVERED;
      } else {
        this.currentHealthStatus = HealthStatus.DEAD;
        dadaab.killrefugee(this);
      }
    }
  }

  // TODO: Como irá funcionar o tratamento?
  public void receiveTreatment(FieldUnit f, Dadaab d) {
    if (dadaab.random.nextDouble() > 0.5) {
      this.setCurrentHealthStatus(HealthStatus.RECOVERED);
    }
  }

  public void applyVaccine() {
    if (HealthStatus.SUSCEPTIBLE.equals(this.currentHealthStatus)) {
      this.vaccinated = true;
      // TODO: Existe algum tempo para acabar sendo imune?
      this.currentHealthStatus = HealthStatus.RECOVERED;
    }
  }

  public void step(SimState state) {
    dadaab = (Dadaab) state;
    currentStep = (int) dadaab.schedule.getSteps();

    if (currentStep < 1440) {
      minuteInDay = currentStep;
    } else {
      minuteInDay = currentStep % 1440;
    }

    // TODO: Importante rever estes conceitos relacionados a saúde
    this.setPreviousHealthStatus(this.getCurrentHealthStatus());
    if (HealthStatus.isInfected(this.currentHealthStatus)) {
      this.currentStateOfInfection();
    }

    if (HealthStatus.isInfected(this.currentHealthStatus)) {
      // TODO: Existem casos de recuperação sem tratamento?
      // TODO: Recebendo tratamento a chance recuperação acaba aumentando?
      if (!this.isPeriodOfInfection()) {
        this.setCurrentHealthStatus(HealthStatus.RECOVERED);
        this.setBodyResistance(1.0);
      }
    }

    if (random.nextDouble() < dadaab.getParams().getGlobal().getProbRecoveryToSuscebtable()
        && this.getCurrentHealthStatus().equals(HealthStatus.RECOVERED)) {
      this.setCurrentHealthStatus(HealthStatus.RECOVERED);
    }

    healthDepretiation();

    // death
    if (this.getBodyResistance() <= 0) {
      dadaab.killrefugee(this);
    }

    this.move(currentStep);
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

  public boolean isWorker() {
    return isWorker;
  }

  public void setWorker(boolean isWorker) {
    this.isWorker = isWorker;
  }

  // family memeber
  public void setFamily(Family family) {
    this.family = family;
  }

  public Family getFamily() {
    return family;
  }

  public void setPreviousHealthStatus(HealthStatus status) {
    this.previousHealthStatus = status;
  }

  public HealthStatus getPreviousHealthStatus() {
    return previousHealthStatus;
  }

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
  public void setIncubationPeriod(int inf) {
    this.incubationPeriod = inf;
  }

  public int getIncubationPeriod() {
    return incubationPeriod;
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

  public int getInfectionPeriod() {
    return infectionPeriod;
  }

  public void setInfectionPeriod(int infectionPeriod) {
    this.infectionPeriod = infectionPeriod;
  }

  public int getToxicPeriod() {
    return toxicPeriod;
  }

  public void setToxicPeriod(int toxicPeriod) {
    this.toxicPeriod = toxicPeriod;
  }

  public boolean isVaccinated() {
    return vaccinated;
  }

  public void setVaccinated(boolean vaccinated) {
    this.vaccinated = vaccinated;
  }

}
