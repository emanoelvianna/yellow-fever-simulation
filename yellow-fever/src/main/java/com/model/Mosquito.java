package com.model;

import java.io.Serializable;
import java.util.List;

import com.core.YellowFever;
import com.core.algorithms.TimeManager;
import com.model.enumeration.HealthStatus;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Valuable;

public class Mosquito implements Steppable, Valuable, Serializable {

  private static final long serialVersionUID = 1L;
  public static final int ORDERING = 2;
  protected Stoppable stopper;
  private MersenneTwisterFast random;
  private YellowFever yellowFever;
  private Building currentPosition;
  private TimeManager time;
  private HealthStatus currentHealthStatus;
  private int currentStep;
  private int currentDay;
  private double temperature;
  private int daysOfLife;
  private boolean hungry;
  private boolean carryingEggs;
  private boolean matureEggs;
  private int incubationPeriod;
  private int daysWithoutFood;
  private double timeOfMaturation;
  private int spaceBetweenEggLaying;
  private boolean dead;
  private String causeOfDeath;

  public Mosquito(Building position, MersenneTwisterFast random) {
    this.random = random;
    this.currentHealthStatus = HealthStatus.SUSCEPTIBLE;
    this.hungry = true;
    this.currentPosition = position;
    this.carryingEggs = false;
    this.incubationPeriod = 0;
    this.daysWithoutFood = 0;
    this.currentDay = 0;
    this.temperature = 0;
    this.timeOfMaturation = 0;
    this.defineSpaceBetweenEggLaying();
    this.defineVectorLifespan();
    this.dead = false;
  }

  public void step(SimState state) {
    if (this.dead)
      return;
    this.yellowFever = (YellowFever) state;
    this.time = this.yellowFever.getTime();
    this.currentStep = (int) yellowFever.schedule.getSteps();
    if (this.isNewDay()) {
      if (this.hungry == true) {
        this.daysWithoutFood++;
      }
      this.hungry = true; // reset the power
      this.daysOfLife--;
      this.dead = this.probabilityOfDying();
      if (this.dead)
        return;
      if (this.spaceBetweenEggLaying > 0)
        this.spaceBetweenEggLaying--;
      this.setTemperature();
      this.checkCurrentStateOfMaturation();
      this.checkCurrentStateOfInfection();
    }
    this.isActive(currentStep);
  }

  public void stop() {
    stopper.stop();
  }

  private void isActive(int currentStep) {
    if (this.time.currentHour(currentStep) >= 7 && this.time.currentHour(currentStep) <= 19) {
      if (this.hungry) {
        if (this.isCarryingEggs()) {
          this.bloodFood();
        } else {
          this.normalFood();
        }
      }
      if (this.isCarryingEggs()) {
        if (this.isMatureEggs()) {
          this.timeOfMaturation = 0; // reset time
          if (this.currentPosition.containsWater()) {
            this.ovipositionProcess();
          }
        }
      } else {
        this.probabilityOfCarryingEggs();
      }
    }
  }

  private void probabilityOfCarryingEggs() {
    synchronized (this.random) {
      if (this.spaceBetweenEggLaying > 0)
        return;
      double probability = this.yellowFever.getParams().getGlobal().getProbabilityOfCarryingEggs();
      if (probability >= this.random.nextDouble()) {
        this.carryingEggs = true;
        this.defineTimeOfMaturation();
      }
    }
  }

  public void defineTimeOfMaturation() {
    this.timeOfMaturation = Math.round(3 + Math.abs((this.temperature - 21) / 5));
  }

  private void ovipositionProcess() {
    synchronized (this.random) {
      double maturationTimeOfTheEggs = Math.round(8 + Math.abs(temperature - 25));
      int amount = 1 + this.random.nextInt(100);
      Egg egg = new Egg(this.currentPosition, maturationTimeOfTheEggs, amount);
      egg.setImported(false);
      this.yellowFever.addEgg(egg);
      this.carryingEggs = false;
      this.defineSpaceBetweenEggLaying();
    }
  }

  private void normalFood() {
    if (this.currentPosition.containsNectar() || this.currentPosition.containsSap()) {
      this.hungry = false;
      this.daysWithoutFood = 0;
    } else {
      this.hungry = true;
    }
  }

  private void bloodFood() {
    synchronized (this.random) {
      if (this.currentPosition.getHumans().size() > 0) {
        double probability = this.yellowFever.getParams().getGlobal().getProbabilityOfGettingBloodFood();
        if (probability >= this.random.nextDouble()) {
          int size = this.currentPosition.getHumans().size();
          Human human = (Human) currentPosition.getHumans().get(this.random.nextInt(size));
          this.toBite(human);
          this.hungry = false;
          this.daysWithoutFood = 0;
          this.dead = this.probabilityOfDying();
        } else {
          this.hungry = true;
        }
      } else {
        this.hungry = true;
      }
    }
  }

  public void toBite(Human human) {
    synchronized (this.random) {
      if (HealthStatus.INFECTED.equals(this.currentHealthStatus)) {
        double probability = this.yellowFever.getParams().getGlobal().getTransmissionProbabilityFromVectorToHost();
        if (probability >= this.random.nextDouble()) {
          human.infected();
        }
      } else if (HealthStatus.SUSCEPTIBLE.equals(this.currentHealthStatus)) {
        if (HealthStatus.MILD_INFECTION.equals(human.getCurrentHealthStatus())) {
          double probability = this.yellowFever.getParams().getGlobal()
              .getTransmissionProbabilityMildInfectionToVector();
          if (probability >= this.random.nextDouble()) {
            this.infected();
          }
        } else if (HealthStatus.SEVERE_INFECTION.equals(human.getCurrentHealthStatus())) {
          double probability = this.yellowFever.getParams().getGlobal()
              .getTransmissionProbabilitySevereInfectionToVector();
          if (probability >= this.random.nextDouble()) {
            this.infected();
          }
        }
      }
    }
  }

  private void checkCurrentStateOfInfection() {
    if (this.incubationPeriod == 0 && HealthStatus.EXPOSED.equals(this.currentHealthStatus)) {
      this.setCurrentHealthStatus(HealthStatus.INFECTED);
    } else if (this.incubationPeriod > 0 && HealthStatus.EXPOSED.equals(this.currentHealthStatus)) {
      this.incubationPeriod--;
    }
  }

  public void infected() {
    if (!HealthStatus.SUSCEPTIBLE.equals(this.currentHealthStatus))
      return;
    this.defineIncubationPeriod();
    this.currentHealthStatus = HealthStatus.EXPOSED;
  }

  private void defineIncubationPeriod() {
    synchronized (this.random) {
      this.incubationPeriod = 8 + this.random.nextInt(5); // 8-12 days
    }
  }

  private void checkCurrentStateOfMaturation() {
    this.timeOfMaturation--;
    if (this.timeOfMaturation <= 0) {
      this.setMatureEggs(true);
    }
  }

  private boolean isNewDay() {
    if (this.time.dayCount(currentStep) > this.currentDay) {
      this.currentDay = this.time.dayCount(currentStep);
      return true;
    } else {
      return false;
    }
  }

  private boolean probabilityOfDying() {
    synchronized (this.random) {
      double probability = this.yellowFever.getParams().getGlobal().getProbabilityOfMosquitoesDying();
      if (probability >= this.random.nextDouble()) {
        this.causeOfDeath = "probability";
        return true;
      } else if (this.daysOfLife <= 0) {
        this.causeOfDeath = "daysOfLife";
        return true;
      } else if (this.daysWithoutFood > 1) {
        this.causeOfDeath = "daysWithoutFood";
        return true;
      }
      return false;
    }
  }

  private void setTemperature() {
    List<Double> temperatures = this.yellowFever.getClimate().getTemperature();
    if (this.currentDay < temperatures.size()) {
      this.temperature = temperatures.get(currentDay);
    }
  }

  public void setInitialTemperature(Double initial) {
    this.temperature = initial;
  }

  private void defineVectorLifespan() {
    synchronized (this.random) {
      this.daysOfLife = 4 + this.random.nextInt(32); // 4-35 days
    }
  }

  public void defineSpaceBetweenEggLaying() {
    synchronized (this.random) {
      this.spaceBetweenEggLaying = 3 + this.random.nextInt(5); // 3-7 days
    }
  }

  public void setStoppable(Stoppable stopp) {
    stopper = stopp;
  }

  public double doubleValue() {
    return 0;
  }

  public boolean isHungry() {
    return hungry;
  }

  public void setHungry(boolean hungry) {
    this.hungry = hungry;
  }

  public Building getCurrentPosition() {
    return currentPosition;
  }

  public void setCurrentPosition(Building currentPosition) {
    this.currentPosition = currentPosition;
  }

  public boolean isCarryingEggs() {
    return carryingEggs;
  }

  public void setCarryingEggs(boolean carryingEggs) {
    this.carryingEggs = carryingEggs;
  }

  private boolean isMatureEggs() {
    return this.matureEggs;
  }

  private void setMatureEggs(boolean matureEggs) {
    this.matureEggs = matureEggs;
  }

  public int getIncubationPeriod() {
    return incubationPeriod;
  }

  public void setIncubationPeriod(int incubationPeriod) {
    this.incubationPeriod = incubationPeriod;
  }

  public HealthStatus getCurrentHealthStatus() {
    return currentHealthStatus;
  }

  public void setCurrentHealthStatus(HealthStatus currentHealthStatus) {
    this.currentHealthStatus = currentHealthStatus;
  }

  public int getDaysWithoutFood() {
    return daysWithoutFood;
  }

  public void setDaysWithoutFood(int daysWithoutFood) {
    this.daysWithoutFood = daysWithoutFood;
  }

  public boolean isDead() {
    return dead;
  }

  public void setDead(boolean dead) {
    this.dead = dead;
  }

  public int getEggLaying() {
    return spaceBetweenEggLaying;
  }

  public void setEggLaying(int eggLaying) {
    this.spaceBetweenEggLaying = eggLaying;
  }

  public String getCauseOfDeath() {
    return causeOfDeath;
  }

  public void setCauseOfDeath(String causeOfDeath) {
    this.causeOfDeath = causeOfDeath;
  }

}
