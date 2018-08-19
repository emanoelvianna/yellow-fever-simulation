package com.core;

import com.model.Building;
import com.model.Human;
import com.model.enumeration.HealthStatus;

import sim.util.Bag;

public class BuildFamily {

  private YellowFever yellowFever;

  public BuildFamily(YellowFever yellowFever) {
    this.yellowFever = yellowFever;
  }

  // TODO: Atualmente existe um bug sobre a quantidade
  public Bag create() {
    Bag family = new Bag();
    int quantityMembers = defineQuantityMembersInFamily();
    while (quantityMembers > 0) {
      int maxMembers = this.yellowFever.random.nextInt(5);
      // TODO: definir localização
      Building home = new Building();
      Bag members = new Bag();
      for (int i = 1; i < maxMembers; i++) {
        Human human = new Human(this.yellowFever, home, HealthStatus.HEALTHY);
        members.add(human);
      }
    }
    return null;
  }

  private int defineQuantityMembersInFamily() {
    // TODO: Rever a quantidade, 30% parece um número grande
    if (this.yellowFever.random.nextDouble() > 0.3) {
      return 0;
    } else {
      return 1 + this.yellowFever.random.nextInt(14);
    }
  }
}
