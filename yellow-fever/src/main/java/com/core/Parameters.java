package com.core;

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

    // TODO: Padronizar as probabilidades como inteiro ou fracionario
    intParameter = returnIntParameter(parameterDB, "InitialHumansNumberInfected", global.initialHumansNumberInfected);
    global.initialHumansNumberInfected = intParameter;

    intParameter = returnIntParameter(parameterDB, "InitialMosquitoesNumber", global.initialMosquitoesNumber);
    global.initialMosquitoesNumber = intParameter;

    intParameter = returnIntParameter(parameterDB, "InitialHumansNumber", global.initialHumansNumber);
    global.initialHumansNumber = intParameter;

    intParameter = returnIntParameter(parameterDB, "maximumFamilyOccumpancyPerBuilding",
        global.maximumFamilyOccumpancyPerBuilding);
    global.maximumFamilyOccumpancyPerBuilding = intParameter;

    doubleParameter = returnDoubleParameter(parameterDB, "waterAbsorption(mm)", global.waterAbsorption);
    global.waterAbsorption = doubleParameter;

    intParameter = returnIntParameter(parameterDB, "quantityOfVaccinesApplied", global.quantityOfVaccinesApplied);
    global.quantityOfVaccinesApplied = intParameter;

    intParameter = returnIntParameter(parameterDB, "resourcesInMedicalCenters", global.resourcesInMedicalCenters);
    global.resourcesInMedicalCenters = intParameter;

    doubleParameter = returnDoubleParameter(parameterDB, "probabilityOfEggsAppearInHouses",
        global.probabilityOfEggsAppearInHouses);
    global.probabilityOfEggsAppearInHouses = doubleParameter;

    doubleParameter = returnDoubleParameter(parameterDB, "probabilityOfGettingBloodFood",
        global.probabilityOfGettingBloodFood);
    global.probabilityOfGettingBloodFood = doubleParameter;

    doubleParameter = returnDoubleParameter(parameterDB, "transmissionProbabilityFromVectorToHost",
        global.transmissionProbabilityFromVectorToHost);
    global.transmissionProbabilityFromVectorToHost = doubleParameter;

    doubleParameter = returnDoubleParameter(parameterDB, "transmissionProbabilityMildInfectionToVector",
        global.transmissionProbabilityMildInfectionToVector);
    global.transmissionProbabilityMildInfectionToVector = doubleParameter;

    doubleParameter = returnDoubleParameter(parameterDB, "transmissionProbabilitySevereInfectionToVector",
        global.transmissionProbabilitySevereInfectionToVector);
    global.transmissionProbabilitySevereInfectionToVector = doubleParameter;

    intParameter = returnIntParameter(parameterDB, "probabilityOfMildInfection", global.probabilityOfMildInfection);
    global.probabilityOfMildInfection = intParameter;

    intParameter = returnIntParameter(parameterDB, "probabilityFromSevereInfectionTotoxicInfection",
        global.probabilityFromSevereInfectionTotoxicInfection);
    global.probabilityFromSevereInfectionTotoxicInfection = intParameter;

    getGlobal().setMaximumNumberRelative(
        returnIntParameter(parameterDB, "MaximumNumberRelative", getGlobal().getMaximumNumberRelative()));

    getGlobal().setHeaalthFacilityCapacity(
        returnIntParameter(parameterDB, "healthFacilityCapacity", getGlobal().getHeaalthFacilityCapacity()));
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

    // TODO: Rever os parametros padrão junto ao documento!
    private int initialHumansNumberInfected = 10;
    private int initialMosquitoesNumber = 1000;
    private int initialHumansNumber = 500;
    private double probabilityOfEggsAppearInHouses = 0.01;
    private double waterAbsorption = 0.1;
    private int quantityOfVaccinesApplied = 0;
    private int resourcesInMedicalCenters = 20;
    private double probabilityOfGettingBloodFood = 0.5;
    public int maximumFamilyOccumpancyPerBuilding = 1000; // arbitrary
    private int probabilityOfMildInfection = 85;
    private int probabilityFromSevereInfectionTotoxicInfection = 10;
    private double transmissionProbabilityFromVectorToHost = 0.75;
    private double transmissionProbabilityMildInfectionToVector = 0.30;
    private double transmissionProbabilitySevereInfectionToVector = 0.75;
    private int healthFacilityCapacity = 1000;

    // TODO: Rever
    public int MaximumNumberRelative = 15;

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

    // determien the number of agent per field or parcel

    public void setMaximumFamilyOccumpancyPerBuilding(int number) {
      this.maximumFamilyOccumpancyPerBuilding = number;
    }

    public int getMaximumFamilyOccumpancyPerBuilding() {
      return maximumFamilyOccumpancyPerBuilding;

    }

    public void setHeaalthFacilityCapacity(int capacity) {
      this.healthFacilityCapacity = capacity;
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

    public double getProbabilityOfEggsAppearInHouses() {
      return probabilityOfEggsAppearInHouses;
    }

    public void setProbabilityOfEggsAppearInHouses(double probability) {
      this.probabilityOfEggsAppearInHouses = probability;
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

    public double getProbabilityOfGettingBloodFood() {
      return probabilityOfGettingBloodFood;
    }

    public void setProbabilityOfGettingBloodFood(double probability) {
      this.probabilityOfGettingBloodFood = probability;
    }

    public int getProbabilityOfMildInfection() {
      return probabilityOfMildInfection;
    }

    public void setProbabilityOfMildInfection(int probability) {
      this.probabilityOfMildInfection = probability;
    }

    public int getProbabilityFromSevereInfectionTotoxicInfection() {
      return probabilityFromSevereInfectionTotoxicInfection;
    }

    public void setProbabilityFromSevereInfectionTotoxicInfection(int probability) {
      this.probabilityFromSevereInfectionTotoxicInfection = probability;
    }

    public double getTransmissionProbabilityFromVectorToHost() {
      return transmissionProbabilityFromVectorToHost;
    }

    public void setTransmissionProbabilityFromVectorToHost(int transmissionProbabilityFromVectorToHost) {
      this.transmissionProbabilityFromVectorToHost = transmissionProbabilityFromVectorToHost;
    }

    public double getTransmissionProbabilityMildInfectionToVector() {
      return transmissionProbabilityMildInfectionToVector;
    }

    public void setTransmissionProbabilityMildInfectionToVector(
        int transmissionProbabilityFromHostWithMildInfectionToVector) {
      this.transmissionProbabilityMildInfectionToVector = transmissionProbabilityFromHostWithMildInfectionToVector;
    }

    public double getTransmissionProbabilitySevereInfectionToVector() {
      return transmissionProbabilitySevereInfectionToVector;
    }

    public void setTransmissionProbabilitySevereInfectionToVector(
        int transmissionProbabilityFromHostWithSevereInfectionToVector) {
      this.transmissionProbabilitySevereInfectionToVector = transmissionProbabilityFromHostWithSevereInfectionToVector;
    }
  }
}
