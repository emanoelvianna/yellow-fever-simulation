package com.core.enumeration;

import java.io.File;
import java.io.IOException;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class Parameters {

  private GlobalParamters global = new GlobalParamters();

  private final static String A_FILE = "-file";

  public Parameters(String[] args) {
    if (args != null) {
      loadParameters(openParameterDatabase(args));
    }
  }

  // <editor-fold defaultstate="collapsed" desc="ECJ ParameterDatabase methods">
  /**
   * Initialize parameter database from file
   *
   * If there exists an command line argument '-file', create a parameter
   * database from the file specified. Otherwise create an empty parameter
   * database.
   *
   * @param args
   *          contains command line arguments
   * @return newly created parameter data base
   *
   * @see loadParameters()
   */
  private static ParameterDatabase openParameterDatabase(String[] args) {
    ParameterDatabase parameters = null;
    for (int x = 0; x < args.length - 1; x++) {
      if (args[x].equals(A_FILE)) {
        try {
          File parameterDatabaseFile = new File(args[x + 1]);
          parameters = new ParameterDatabase(parameterDatabaseFile.getAbsoluteFile());
        } catch (IOException ex) {
          ex.printStackTrace();
        }
        break;
      }
    }
    if (parameters == null) {
      System.out.println("\nNot in a parameter Mode");// ("\nNo parameter file
                                                      // was specified");
      parameters = new ParameterDatabase();
    }
    return parameters;
  }

  private void loadParameters(ParameterDatabase parameterDB) {
    int intParameter = 0;
    double doubleParameter = 0;

    intParameter = returnIntParameter(parameterDB, "InitialHumansNumberInfected", global.initialHumansNumberInfected);
    global.initialHumansNumberInfected = intParameter;

    intParameter = returnIntParameter(parameterDB, "InitialMosquitoesNumber", global.initialMosquitoesNumber);
    global.initialMosquitoesNumber = intParameter;

    intParameter = returnIntParameter(parameterDB, "InitialHumansNumber", global.initialHumansNumber);
    global.initialHumansNumber = intParameter;

    intParameter = returnIntParameter(parameterDB, "probabilityOfEggsAppearInHouses",
        global.probabilityOfEggsAppearInHouses);
    global.probabilityOfEggsAppearInHouses = intParameter;

    doubleParameter = returnDoubleParameter(parameterDB, "waterAbsorption(mm)", global.waterAbsorption);
    global.waterAbsorption = doubleParameter;

    intParameter = returnIntParameter(parameterDB, "quantityOfVaccinesApplied", global.quantityOfVaccinesApplied);
    global.quantityOfVaccinesApplied = intParameter;

    intParameter = returnIntParameter(parameterDB, "resourcesInMedicalCenters", global.resourcesInMedicalCenters);
    global.resourcesInMedicalCenters = intParameter;

    getGlobal().setMaximumNumberRelative(
        returnIntParameter(parameterDB, "MaximumNumberRelative", getGlobal().getMaximumNumberRelative()));

    getGlobal().setProbRecoveryToSuscebtable(
        returnDoubleParameter(parameterDB, "recovery_To_Susceb_Rate", getGlobal().getProbRecoveryToSuscebtable()));

    getGlobal().setHeaalthFacilityCapacity(
        returnIntParameter(parameterDB, "healthFacilityCapacity", getGlobal().getHeaalthFacilityCapacity()));

    getGlobal().setMaximumCrowedLevel(
        returnDoubleParameter(parameterDB, "CROWED_LEVEL_THRESHOLD", getGlobal().getMaximumCrowedLevel()));

    getGlobal().setMaximumHHOccumpancyPerField(
        returnIntParameter(parameterDB, "maximum_occupancy_Threshold", getGlobal().getMaximumHHOccumpancyPerField()));

  }

  public int returnIntParameter(ParameterDatabase paramDB, String parameterName, int defaultValue) {
    return paramDB.getIntWithDefault(new Parameter(parameterName), null, defaultValue);
  }

  public boolean returnBooleanParameter(ParameterDatabase paramDB, String parameterName, boolean defaultValue) {
    return paramDB.getBoolean(new Parameter(parameterName), null, defaultValue);
  }

  double returnDoubleParameter(ParameterDatabase paramDB, String parameterName, double defaultValue) {
    return paramDB.getDoubleWithDefault(new Parameter(parameterName), null, defaultValue);
  }

  public GlobalParamters getGlobal() {
    return global;
  }

  public void setGlobal(GlobalParamters global) {
    this.global = global;
  }

  public class GlobalParamters {

    private int initialHumansNumberInfected = 10;
    private int initialMosquitoesNumber = 1000;
    private int initialHumansNumber = 4000; // 4000
    private int probabilityOfEggsAppearInHouses = 30;
    private double waterAbsorption = 0.1;
    private int quantityOfVaccinesApplied = 0;
    private int resourcesInMedicalCenters = 20;

    // TODO: adicionar a primeira temperatura e precipitação como parametro!

    public double recovery_To_Susceb_Rate = 0.000001; // prob of change from
                                                      // recovered to
                                                      // suscebtible
    public int maximum_occupancy_Threshold = 1000; // arbitrary
                                                   // uses
    public int MaximumNumberRelative = 15;
    public double CROWED_LEVEL_THRESHOLD = 4000; // 50% of the cellsize
                                                 // 90*90=8100

    public int healthFacilityCapacity = 1000; // 500 person/day efficient to
                                              // treat cholera victim

    public void setInitialHumansNumber(int num) {
      this.initialHumansNumber = num;
    }

    public int getInitialHumansNumber() {
      return initialHumansNumber;

    }

    public void setMaximumNumberRelative(int num) {
      this.MaximumNumberRelative = num;
    }

    public int getMaximumNumberRelative() {
      return MaximumNumberRelative;

    }

    // probability of recovered agent to be suscebtable again
    public void setProbRecoveryToSuscebtable(double rec) {
      this.recovery_To_Susceb_Rate = rec;
    }

    public double getProbRecoveryToSuscebtable() {
      return recovery_To_Susceb_Rate;
    }

    // determien the number of agent per field or parcel

    public void setMaximumHHOccumpancyPerField(int num) {
      this.maximum_occupancy_Threshold = num;
    }

    public int getMaximumHHOccumpancyPerField() {
      return maximum_occupancy_Threshold;

    }

    // determine how many agent can be stay in a given field or road at the same
    // time
    public void setMaximumCrowedLevel(double c) {
      this.CROWED_LEVEL_THRESHOLD = c;
    }

    public double getMaximumCrowedLevel() {
      return CROWED_LEVEL_THRESHOLD;
    }

    public void setHeaalthFacilityCapacity(int ca) {
      this.healthFacilityCapacity = ca;
    }

    public int getHeaalthFacilityCapacity() {
      return healthFacilityCapacity;
    }

    public int getInitialHumansNumberInfected() {
      return initialHumansNumberInfected;
    }

    public void setInitialHumansNumberInfected(int initialHumansNumberInfected) {
      this.initialHumansNumberInfected = initialHumansNumberInfected;
    }

    public int getInitialMosquitoesNumber() {
      return initialMosquitoesNumber;
    }

    public void setInitialMosquitoesNumber(int initialMosquitoesNumber) {
      this.initialMosquitoesNumber = initialMosquitoesNumber;
    }

    public int getProbabilityOfEggsAppearInHouses() {
      return probabilityOfEggsAppearInHouses;
    }

    public void setProbabilityOfEggsAppearInHouses(int probabilityOfEggsAppearInHouses) {
      this.probabilityOfEggsAppearInHouses = probabilityOfEggsAppearInHouses;
    }

    public double getWaterAbsorption() {
      return waterAbsorption;
    }

    public void setWaterAbsorption(double waterAbsorption) {
      this.waterAbsorption = waterAbsorption;
    }

    public int getQuantityOfVaccinesApplied() {
      return quantityOfVaccinesApplied;
    }

    public void setQuantityOfVaccinesApplied(int quantityOfVaccinesApplied) {
      this.quantityOfVaccinesApplied = quantityOfVaccinesApplied;
    }

    public int getResourcesInMedicalCenters() {
      return resourcesInMedicalCenters;
    }

    public void setResourcesInMedicalCenters(int resourcesInMedicalCenters) {
      this.resourcesInMedicalCenters = resourcesInMedicalCenters;
    }
  }
}
