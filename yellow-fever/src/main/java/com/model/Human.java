package com.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.core.YellowFever;
import com.core.Node;
import com.core.algorithms.AStar;
import com.core.algorithms.TimeManager;
import com.model.enumeration.ActivityMapping;
import com.model.enumeration.HealthStatus;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;

import sim.util.Double2D;
import sim.util.Valuable;

public class Human implements Steppable, Valuable, Serializable {

  public static final int ORDERING = 2;
  protected Stoppable stopper;
  private YellowFever dadaab;
  // the agent's current path to its current goal
  private ArrayList<Building> path = null;
  private MersenneTwisterFast random;
  // time contorler-identify the hour, day, week
  private TimeManager time;
  public int minuteInDay;

  private int age;
  private int sex;
  private boolean isWorker;
  private boolean isStudent;
  private Building currentPosition;
  private Building home;
  private Building goal;
  private int currentStep;
  private double jitterX;
  private double jitterY;
  private HealthStatus currentHealthStatus;
  private HealthStatus previousHealthStatus;
  private Family family;
  private ActivityMapping currentActivity;
  public int stayingTime;
  private boolean vaccinated;
  private double bodyResistance;
  private int incubationPeriod;
  private int infectionPeriod;
  private int toxicPeriod;
  private boolean serious;
  private int currentDay;

  public Human(int age, int sex, Family family, Building home, Building position, MersenneTwisterFast seed,
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
    this.currentDay = 0;
    this.setObjectLocation(allRefugees);
  }

  public void move(int steps) {
    Activity activity = new Activity(dadaab, this, time, currentStep, minuteInDay);
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
      // TODO: Qual é a necessidade disto? Parece relacionado a hora
      if (steps % 1440 < 17) {
        if (random.nextDouble() > 0.3) { // TODO: Qual é a necessidade disto?
          calculateGoal();
        }
      } else {
        calculateGoal();
      }
    }
    // else move to your goal
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
      Building subgoal;
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

      Building loc = activity.getNextTile(dadaab, subgoal, this.getCurrentPosition());
      Building oldLoc = this.getCurrentPosition();
      oldLoc.removeRefugee(this);
      this.setCurrentPosition(loc);
      loc.addRefugee(this);
      dadaab.allHumans.setObjectLocation(this,
          new Double2D(loc.getLocationX() + this.jitterX, loc.getLocationY() + jitterY));
    }
  }

  // assign the best goal
  public void calculateGoal() {
    if (this.getCurrentPosition().equals(this.getHome()) == true) {
      Activity activity = new Activity(dadaab, this, time, currentStep, minuteInDay);
      ActivityMapping bestActivity = activity.defineActivity(dadaab);
      this.setGoal(activity.bestActivityLocation(this, this.getHome(), bestActivity, dadaab));
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
    this.defineIncubationPeriod();
    this.currentHealthStatus = HealthStatus.EXPOSED;
  }

  private void checkCurrentStateOfInfection() {
    this.defineInfection();
    this.defineMildInfectionEvolution();
    this.defineSevereInfectionEvolution();
    this.defineToxicInfectionEvolution();
  }

  private void defineInfection() {
    if (this.incubationPeriod == 0 && HealthStatus.EXPOSED.equals(this.currentHealthStatus)) {
      if (dadaab.random.nextInt(11) <= 9) { // 90% of cases are mild
        this.setCurrentHealthStatus(HealthStatus.MILD_INFECTION);
        this.definePeriodOfInfection();
      } else {
        this.setCurrentHealthStatus(HealthStatus.SEVERE_INFECTION);
        this.definePeriodOfInfection();
        this.serious = this.infectionPeriod == 4 ? true : false;
      }
    } else if (this.incubationPeriod > 0 && this.isNewDay() && HealthStatus.EXPOSED.equals(this.currentHealthStatus)) {
      this.incubationPeriod--;
    }
  }

  private void defineMildInfectionEvolution() {
    if (this.infectionPeriod == 0 && HealthStatus.MILD_INFECTION.equals(this.currentHealthStatus)) {
      this.currentHealthStatus = HealthStatus.RECOVERED;
    } else if (this.infectionPeriod > 0 && this.isNewDay()
        && HealthStatus.MILD_INFECTION.equals(this.currentHealthStatus)) {
      this.infectionPeriod--;
    }
  }

  private void defineSevereInfectionEvolution() {
    if (this.serious && this.infectionPeriod == 0 && HealthStatus.SEVERE_INFECTION.equals(this.currentHealthStatus)) {
      this.currentHealthStatus = HealthStatus.TOXIC_INFECTION;
      this.definePeriodOfToxicInfection();
    } else if (!this.serious && this.infectionPeriod == 0
        && HealthStatus.SEVERE_INFECTION.equals(this.currentHealthStatus)) {
      this.currentHealthStatus = HealthStatus.RECOVERED;
    } else if (this.infectionPeriod > 0 && this.isNewDay()
        && HealthStatus.SEVERE_INFECTION.equals(this.currentHealthStatus)) {
      this.infectionPeriod--;
    }
  }

  private void defineToxicInfectionEvolution() {
    if (this.toxicPeriod == 0 && HealthStatus.TOXIC_INFECTION.equals(this.currentHealthStatus)) {
      if (dadaab.random.nextInt(11) < 5) { // 50-50 chance
        this.currentHealthStatus = HealthStatus.RECOVERED;
      } else {
        this.currentHealthStatus = HealthStatus.DEAD;
        dadaab.killrefugee(this);
      }
    } else if (this.toxicPeriod > 0 && this.isNewDay()
        && HealthStatus.TOXIC_INFECTION.equals(this.currentHealthStatus)) {
      this.toxicPeriod--;
    }
  }

  public boolean isPeriodOfInfection() {
    if (this.infectionPeriod > 0 || this.toxicPeriod > 0) {
      return true;
    } else {
      return false;
    }
  }

  public boolean hasSymptomsOfInfection() {
    switch (this.currentHealthStatus) {
    case MILD_INFECTION:
      return true;
    case SEVERE_INFECTION:
      return true;
    case TOXIC_INFECTION:
      return true;
    default:
      return false;
    }
  }

  // TODO: Como irá funcionar o tratamento?
  // TODO: O tempo de recuperação deve considerar o tempo?
  public void receiveTreatment(Building f, YellowFever d) {
    if (dadaab.random.nextDouble() > 0.5) {
      // this.setCurrentHealthStatus(HealthStatus.RECOVERED);
    }
  }

  public void applyVaccine() {
    if (HealthStatus.SUSCEPTIBLE.equals(this.currentHealthStatus)) {
      this.vaccinated = true;
      // TODO: Existe algum tempo para acabar sendo imune?
      this.currentHealthStatus = HealthStatus.RECOVERED;
    }
  }

  private void definePeriodOfInfection() {
    this.infectionPeriod = 3 + this.dadaab.random.nextInt(2);
  }

  private void definePeriodOfToxicInfection() {
    this.toxicPeriod = 8;
  }

  private void defineIncubationPeriod() {
    this.incubationPeriod = 3 + this.dadaab.random.nextInt(4);
  }

  private boolean isNewDay() {
    if (this.time.dayCount(currentStep) > this.currentDay) {
      this.currentDay = this.time.dayCount(currentStep);
      return true;
    } else {
      return false;
    }
  }

  public void step(SimState state) {
    this.dadaab = (YellowFever) state;
    this.currentStep = (int) dadaab.schedule.getSteps();

    if (this.currentStep < 1440) {
      this.minuteInDay = this.currentStep;
    } else {
      this.minuteInDay = this.currentStep % 1440;
    }

    this.setPreviousHealthStatus(this.getCurrentHealthStatus());
    if (HealthStatus.isInfected(this.currentHealthStatus)) {
      this.checkCurrentStateOfInfection();
    }

    this.move(currentStep);

    // TODO: Remover, utilizado para realização de testes
    if (HealthStatus.DEAD.equals(this.currentHealthStatus)) {
      System.out.println("---");
      System.out.println("Status da saúde: " + this.currentHealthStatus);
      System.out.println("Idade: " + this.age);
      System.out.println("Perido de inbuvação: " + this.incubationPeriod);
      System.out.println("Perido de infecção: " + this.infectionPeriod);
      System.out.println("Perido de tóxico: " + this.toxicPeriod);
      System.out.println("Estou em casa:" + this.currentPosition.equals(this.home));
      System.out.println("Meu objetivo:" + this.currentActivity);
      System.out.println("---");
    }
  }

  public double doubleValue() {
    switch (this.currentHealthStatus) {
    case SUSCEPTIBLE:
      return 1;
    case EXPOSED:
      return 2;
    case MILD_INFECTION:
      return 3;
    case SEVERE_INFECTION:
      return 4;
    case TOXIC_INFECTION:
      return 5;
    default:
      return 6;
    }
  }

  private void setObjectLocation(Continuous2D allRefugees) {
    allRefugees.setObjectLocation(this, new Double2D(family.getCampLocation().getLocationX() + jitterX,
        family.getCampLocation().getLocationY() + jitterY));
  }

  public void setStoppable(Stoppable stopp) {
    stopper = stopp;
  }

  public void stop() {
    stopper.stop();
  }

  private void setCurrentPosition(Building position) {
    this.currentPosition = position;
  }

  public Building getCurrentPosition() {
    return currentPosition;
  }

  // goal position - where to go
  public void setGoal(Building position) {
    this.goal = position;

  }

  public Building getGoal() {
    return goal;
  }

  // home location
  public void setHome(Building home) {
    this.home = home;

  }

  public Building getHome() {
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

  public boolean isSerious() {
    return serious;
  }

  public void setSerious(boolean serious) {
    this.serious = serious;
  }

}
