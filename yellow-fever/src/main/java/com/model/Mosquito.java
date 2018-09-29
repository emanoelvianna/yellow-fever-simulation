package com.model;

import java.io.Serializable;
import java.util.List;

import com.core.Dadaab;
import com.core.TimeManager;
import com.model.enumeration.HealthStatus;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Valuable;

public class Mosquito implements Steppable, Valuable, Serializable {

  public static final int ORDERING = 2;
  protected Stoppable stopper;
  private MersenneTwisterFast random;
  private Dadaab dadaab;
  private Building currentPosition;
  private TimeManager time;
  private int daysOfLife;
  private double speed;
  private boolean hungry;
  private boolean carryingEggs;
  private boolean matureEggs;
  private int incubationPeriod;
  private int daysWithoutFood;
  private HealthStatus currentHealthStatus;
  private HealthStatus previousHealthStatus;
  private int sensoryAmplitude;
  private double timeOfMaturation;
  private int currentStep;
  private int currentDay;
  private double temperature;

  public Mosquito(Building position) {
    this.random = new MersenneTwisterFast();
    this.daysOfLife = 30 + random.nextInt(16);
    this.currentHealthStatus = HealthStatus.SUSCEPTIBLE;
    this.speed = 1.0;
    this.hungry = true;
    this.sensoryAmplitude = 3;
    this.currentPosition = position;
    this.carryingEggs = false;
    this.incubationPeriod = 0;
    this.daysWithoutFood = 0;
    this.currentDay = 0;
    this.temperature = 21; // TODO: Refatorar
    this.timeOfMaturation = 0;
  }

  public void step(SimState state) {
    this.dadaab = (Dadaab) state;
    this.time = this.dadaab.getTime();
    this.currentStep = (int) dadaab.schedule.getSteps();
    if (this.isNewDay()) {
      if (this.hungry == true) {
        this.daysWithoutFood++;
      }
      this.hungry = true; // reset the power
      this.daysOfLife--;
      this.probabilityOfDying();
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
    if (this.time.currentHour(currentStep) >= 7 && this.time.currentHour(currentStep) <= 18) {
      // TODO: Considerar está mudança junto ao modelo do mosquito
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
    if (this.dadaab.random.nextDouble() <= 0.2) { // 20% chance
      this.setCarryingEggs(true);
    } else {
      this.setCarryingEggs(false);
    }
  }

  private void defineTimeOfMaturation() {
    this.timeOfMaturation = 3 + Math.abs((this.temperature - 21) / 5);
  }

  private void ovipositionProcess() {
    this.currentPosition.addEgg(100);
    this.currentPosition.defineTheMaturationTimeOfTheEggs(this.temperature);
    this.carryingEggs = false;
  }

  private void bloodFood() {
    if (this.currentPosition.containsHumans()) {
      int size = currentPosition.getHumans().size();
      this.dadaab.random.nextInt(size);
      this.toBite((Human) currentPosition.getHumans().get(this.dadaab.random.nextInt(size)));
      // TODO: Considerar uma probabilidade do mosquito conseguir
      this.hungry = false;
    } else {
      this.hungry = true;
    }
  }

  private void normalFood() {
    if (this.currentPosition.containsNectar() || this.currentPosition.containsSap()) {
      // TODO: Considerar uma probabilidade do mosquito conseguir
      this.hungry = false;
    } else {
      this.hungry = true;
    }
  }

  // TODO: Rever os valores em relação ao modelo bibliográfico
  public void toBite(Human human) {
    if (HealthStatus.SUSCEPTIBLE.equals(human.getCurrentHealthStatus())) {
      if (HealthStatus.INFECTED.equals(this.currentHealthStatus)) {
        // TODO: Adicionar as probabilidades
        // TODO: Relacionada a conseguir realizar a picada
        // TODO: Relacionada a acabar morrendo durante a tentativa
        if (this.dadaab.random.nextDouble() <= 0.7) { // 70% chance of infection
          human.infected();
        }
      }
    }
    if (HealthStatus.isInfected(human.getCurrentHealthStatus())) {
      if (this.dadaab.random.nextDouble() <= 0.7) { // 70% chance of infection
        this.infected();
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
    this.defineIncubationPeriod();
    this.currentHealthStatus = HealthStatus.EXPOSED;
  }

  private void defineIncubationPeriod() {
    this.incubationPeriod = 3 + this.dadaab.random.nextInt(4);
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

  private void setTemperature() {
    List<Double> temperatures = dadaab.getClimate().getTemperature();
    if (currentDay < temperatures.size()) {
      this.temperature = temperatures.get(currentDay);
    } else {
      // TODO:
    }
  }

  private void probabilityOfDying() {
    if (this.dadaab.random.nextDouble() <= 0.05) { // 5% chance
      this.dadaab.killmosquito(this);
    } else if (this.daysOfLife <= 0) {
      this.dadaab.killmosquito(this);
    } else if (this.daysWithoutFood > 1) {
      this.dadaab.killmosquito(this);
    }
  }

  public void setStoppable(Stoppable stopp) {
    stopper = stopp;
  }

  public double doubleValue() {
    return 0;
  }

  public double getSpeed() {
    return speed;
  }

  public void setSpeed(double speed) {
    this.speed = speed;
  }

  public boolean isHungry() {
    return hungry;
  }

  public void setHungry(boolean hungry) {
    this.hungry = hungry;
  }

  public int getSensoryAmplitude() {
    return sensoryAmplitude;
  }

  public void setSensoryAmplitude(int sensoryAmplitude) {
    this.sensoryAmplitude = sensoryAmplitude;
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

}
