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
  private int currentStep;
  private int currentDay;
  private double temperature;
  private int daysOfLife;
  private boolean hungry;
  private boolean carryingEggs;
  private boolean matureEggs;
  private int incubationPeriod;
  private int daysWithoutFood;
  private HealthStatus currentHealthStatus;
  private HealthStatus previousHealthStatus;
  private double timeOfMaturation;
  private boolean dead;

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
    // TODO: Conversar com a epidemiologista em relação ao horario
    if (this.time.currentHour(currentStep) >= 7 && this.time.currentHour(currentStep) <= 18) {
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
        } else if (timeOfMaturation == 0) {
          this.defineTimeOfMaturation();
        }
      } else {
        this.probabilityOfCarryingEggs();
      }
    }
  }

  private void probabilityOfCarryingEggs() {
    if (0.2 >= this.random.nextDouble()) { // 20% chance
      this.setCarryingEggs(true);
    } else {
      this.setCarryingEggs(false);
    }
  }

  private void defineTimeOfMaturation() {
    this.timeOfMaturation = 3 + Math.abs((this.temperature - 21) / 5);
  }

  // TODO: Problema com concorrencia!
  private void ovipositionProcess() {
    double maturationTimeOfTheEggs = 8 + Math.abs(temperature - 25);
    int amount = 1 + this.random.nextInt(100);
    this.yellowFever.addEgg(new Egg(this.currentPosition, maturationTimeOfTheEggs, amount));
    this.carryingEggs = false;
    // used to the statistics
    this.yellowFever.addToTheTotalEggsInTheEnvironment(amount);
  }

  private void normalFood() {
    if (this.currentPosition.containsNectar() || this.currentPosition.containsSap()) {
      this.hungry = false;
    } else {
      this.hungry = true;
    }
  }

  private void bloodFood() {
    if (this.currentPosition.getHumans().size() > 0) {
      double probability = this.yellowFever.getParams().getGlobal().getProbabilityOfGettingBloodFood();
      if (probability >= this.random.nextDouble()) {
        int size = this.currentPosition.getHumans().size();
        Human human = (Human) currentPosition.getHumans().get(this.random.nextInt(size));
        this.toBite(human);
        this.hungry = false;
        this.dead = this.probabilityOfDying();
      } else {
        this.hungry = true;
      }
    } else {
      this.hungry = true;
    }
  }

  public void toBite(Human human) {
    synchronized (human.getCurrentHealthStatus()) {
      switch (human.getCurrentHealthStatus()) {
      case SUSCEPTIBLE:
        if (HealthStatus.INFECTED.equals(this.getCurrentHealthStatus())) {
          double probability = this.yellowFever.getParams().getGlobal().getTransmissionProbabilityFromVectorToHost();
          if (probability >= this.random.nextDouble()) {
            human.infected();
          }
        }
        break;
      case MILD_INFECTION:
        if (HealthStatus.SUSCEPTIBLE.equals(this.getCurrentHealthStatus())) {
          double probability = this.yellowFever.getParams().getGlobal()
              .getTransmissionProbabilityMildInfectionToVector();
          if (probability >= this.random.nextDouble()) {
            this.infected();
          }
        }
        break;
      case SEVERE_INFECTION:
        if (HealthStatus.SUSCEPTIBLE.equals(this.getCurrentHealthStatus())) {
          double probability = this.yellowFever.getParams().getGlobal()
              .getTransmissionProbabilitySevereInfectionToVector();
          if (probability >= this.random.nextDouble()) {
            this.infected();
          }
        }
        break;
      default:
        break;
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

  private void infected() {
    if (!HealthStatus.SUSCEPTIBLE.equals(this.currentHealthStatus))
      return;
    this.defineIncubationPeriod();
    this.currentHealthStatus = HealthStatus.EXPOSED;
  }

  private void defineIncubationPeriod() {
    this.incubationPeriod = 8 + this.random.nextInt(5); // 8-12 days
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
    double probability = 0.05; // 5% chance
    if (probability >= this.random.nextDouble()) {
      return true;
    } else if (this.daysOfLife <= 0) {
      return true;
    } else if (this.daysWithoutFood > 1) {
      return true;
    }
    return false;
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
    this.daysOfLife = 4 + this.random.nextInt(32); // 4-35 days
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

  public HealthStatus getPreviousHealthStatus() {
    return previousHealthStatus;
  }

  public void setPreviousHealthStatus(HealthStatus previousHealthStatus) {
    this.previousHealthStatus = previousHealthStatus;
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

}
