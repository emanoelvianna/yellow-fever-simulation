package com.model;

import java.io.Serializable;

import com.core.Dadaab;
import com.core.TimeManager;
import com.model.enumeration.HealthStatus;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Valuable;

public class Mosquito implements Steppable, Valuable, Serializable {

  public static final int ORDERING = 2;
  protected Stoppable stopper;
  private int age;
  private double speed;
  private boolean hungry;
  private boolean carryingEggs;
  private boolean matureEggs;
  private boolean activity;
  private int sensoryAmplitude;
  private Dadaab dadaab;
  private int currentStep;
  private FieldUnit currentPosition;
  private TimeManager time;
  private int currentDay;

  public Mosquito(FieldUnit position) {
    this.age = 0;
    this.speed = 1.0;
    this.hungry = true;
    this.activity = false;
    this.sensoryAmplitude = 3;
    this.currentPosition = position;
    this.carryingEggs = false;
  }

  public void step(SimState state) {
    this.dadaab = (Dadaab) state;
    this.time = this.dadaab.getTime();
    this.currentStep = (int) dadaab.schedule.getSteps();

    this.isActivity(currentStep);
    if (this.isNewDay()) {
      this.probabilityOfDie();
    }
  }

  public void stop() {
    stopper.stop();
  }

  private void isActivity(int currentStep) {
    if (this.time.currentHour(currentStep) >= 7 && this.time.currentHour(currentStep) <= 22) {
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
          if (this.currentPosition.containsWater()) {
            this.ovipositionProcess();
          }
        } else {
          this.probabilityOfMature();
        }
      } else {
        this.probabilityOfCarryingEggs();
      }
      this.activity = true;
    } else {
      this.activity = false;
    }
  }

  private void probabilityOfCarryingEggs() {
    if (this.dadaab.random.nextDouble() < 0.2) { // 20% chance
      this.setCarryingEggs(true);
    } else {
      this.setCarryingEggs(false);
    }
  }

  private void probabilityOfMature() {
    // TODO: Como devo representar a equação?
    this.setMatureEggs(true);
  }

  private int ovipositionProcess() {
    if (dadaab.random.nextDouble() > 0.5) { // 50-50 chance
      return 0;
    } else {
      return 1;
    }
  }

  private void normalFood() {
    // TODO: Uma residencia irá sempre conter este tipo de alimento?
    // TODO: Faz sentido ser algum tipo de probabilidade?
    if (this.currentPosition.containsNectar() || this.currentPosition.containsSap()) {
      this.hungry = true;
    }
  }

  private void bloodFood() {
    if (this.currentPosition.containsPresentHumans()) {
      // TODO: Considerar probabilidade de alimentação
      this.toBite((Human) currentPosition.getRefugee().get(0));
      this.hungry = false;
    } else {
      this.hungry = true;
    }
  }

  private void probabilityOfDie() {
    if (this.dadaab.random.nextDouble() < 0.2) { // 20% chance
      this.dadaab.killmosquito(this);
      // TODO: Rever a probabilidade relacionada a idade
    } else if (this.age > 30 + this.dadaab.random.nextInt(16)) {
      this.dadaab.killmosquito(this);
    }
  }

  public void toBite(Human human) {
    // TODO: Considerar probabilidade
    if (HealthStatus.SUSCEPTIBLE.equals(human.getCurrentHealthStatus())) {
      human.infected();
    }
  }

  private boolean isNewDay() {
    if (this.time.dayCount(currentStep) > this.currentDay) {
      this.currentDay = this.time.dayCount(currentStep);
      this.hungry = false;
      return true;
    } else {
      return false;
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

  public FieldUnit getCurrentPosition() {
    return currentPosition;
  }

  public void setCurrentPosition(FieldUnit currentPosition) {
    this.currentPosition = currentPosition;
  }

  public boolean isActivity() {
    return activity;
  }

  public void setActivity(boolean activity) {
    this.activity = activity;
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

}
