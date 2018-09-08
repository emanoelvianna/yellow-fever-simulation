package com.model;

import java.io.Serializable;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Valuable;

public class Mosquito implements Steppable, Valuable, Serializable {

  public void toBite(Refugee refugee) {
    // TODO: Com probabilidade x infecção agente humano
    refugee.infected();
  }

  public void step(SimState arg0) {

  }

  public double doubleValue() {
    return 0;
  }

}
