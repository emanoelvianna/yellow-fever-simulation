package com.model.enumeration;

public enum HealthStatus {
  SUSCEPTIBLE, EXPOSED, SEVERE_INFECTION, MILD_INFECTION, TOXIC_INFECTION, RECOVERED, DEAD;

  public static HealthStatus typeOfHealthStatus(HealthStatus currentHealthStatus) {
    switch (currentHealthStatus) {
    case SUSCEPTIBLE:
      return HealthStatus.SUSCEPTIBLE;
    case EXPOSED:
      return HealthStatus.EXPOSED;
    case MILD_INFECTION:
      return HealthStatus.MILD_INFECTION;
    case SEVERE_INFECTION:
      return HealthStatus.SEVERE_INFECTION;
    case TOXIC_INFECTION:
      return HealthStatus.TOXIC_INFECTION;
    default:
      return HealthStatus.SUSCEPTIBLE;
    }
  }

  public static boolean isInfected(HealthStatus currentHealthStatus) {
    switch (currentHealthStatus) {
    case EXPOSED:
      return true;
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
}
