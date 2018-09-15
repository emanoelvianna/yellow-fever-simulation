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

  private double speed;
  private boolean hungry;
  private int sensoryAmplitude;
  private Dadaab dadaab;
  private int currentStep;
  private FieldUnit currentPosition;
  private TimeManager time;
  protected Stoppable stopper;

  public Mosquito(FieldUnit position, TimeManager time) {
    this.speed = 1.0;
    this.hungry = true;
    this.sensoryAmplitude = 3;
    this.currentPosition = position;
    this.time = time;
    this.dadaab = null;
  }

  public void toBite(Human human) {
    // TODO: Considerar probabilidade
    if (!HealthStatus.RECOVERED.equals(human.getCurrentHealthStatus())) {
      if (!HealthStatus.isInfected(human.getCurrentHealthStatus())) {
        human.infected();
      }
    }
  }

  public void ThereIsFood() {

  }

  public void step(SimState state) {
    this.dadaab = (Dadaab) state;
    currentStep = (int) dadaab.schedule.getSteps();

    this.isActivity(currentStep);
  }

  public void setStoppable(Stoppable stopp) {
    stopper = stopp;
  }

  public void stop() {
    stopper.stop();
  }

  private void isActivity(int currentStep) {
    if (this.time.currentHour(currentStep) >= 7 && this.time.currentHour(currentStep) <= 18) {
      // TODO: Considerar está mudança junto ao modelo do mosquito
      if (this.hungry) {
        if (this.carryingEggs()) {
          this.bloodFood();
        } else {
          this.normalFood();
        }
      }
      if (this.carryingEggs() && this.isMature()) {
        // TODO: realiza processo de busca de fonte de água para oviposição
      } else {
        // TODO: realiza probabilidade da maturação
      }
    } else {
      // TODO: Mosquito parado
    }
  }

  private boolean isMature() {
    // TODO:
    return true;
  }

  private void normalFood() {
    // TODO: Uma residencia irá sempre conter este tipo de alimento?
    // TODO: Faz sentido ser algum tipo de probabilidade?
    if (this.currentPosition.containsNectar() || this.currentPosition.containsSap()) {
      this.hungry = false;
    }
  }

  private void bloodFood() {
    if (this.currentPosition.containsPresentHumans()) {
      // TODO: Considerar probabilidade de alimentação
      this.toBite((Human) currentPosition.getRefugee().get(0));
      this.hungry = true;
    } else {
      this.hungry = false;
    }
  }

  public boolean carryingEggs() {
    // TODO:
    return true;
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

}
