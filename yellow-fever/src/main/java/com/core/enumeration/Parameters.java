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
    int returnIntParameter = 0;

    returnIntParameter = returnIntParameter(parameterDB, "AmountOfInfectedHumans", global.amountOfInfectedHumans);
    global.setAmountOfInfectedHumans(returnIntParameter);

    global.setInitialHumansNumber(
        returnIntParameter(parameterDB, "InitialHumansNumber", global.getInitialHumansNumber()));

    getGlobal().setMaximumNumberRelative(
        returnIntParameter(parameterDB, "MaximumNumberRelative", getGlobal().getMaximumNumberRelative()));
    getGlobal().setPercentageOfAsymptomatic(
        returnDoubleParameter(parameterDB, "PercentageOfAsymptomatic", getGlobal().getPercentageOfAsymptomatic()));
    getGlobal().setProbRecoveryToSuscebtable(
        returnDoubleParameter(parameterDB, "recovery_To_Susceb_Rate", getGlobal().getProbRecoveryToSuscebtable()));
    getGlobal().setHealthDepreciation(
        returnDoubleParameter(parameterDB, "healthDepreciation", getGlobal().getHealthDepreciation()));
    getGlobal().setprobabilityOfEffectiveNessofmedicine(returnDoubleParameter(parameterDB,
        "probabilityOfEffectiveNessofmedicine", getGlobal().getprobabilityOfEffectiveNessofmedicine()));
    getGlobal().setWaterContaminationThreshold(returnDoubleParameter(parameterDB, "WaterContaminationThreshold",
        getGlobal().getWaterContaminationThreshold()));
    getGlobal().setvibrioCholeraePerHealthyPerson(returnDoubleParameter(parameterDB, "vibrioCholeraePerHealthyPerson",
        getGlobal().getvibrioCholeraePerHealthyPerson()));
    getGlobal().setvibrioCholeraePerExposedPerson(returnDoubleParameter(parameterDB, "vibrioCholeraePerExposedPerson",
        getGlobal().getvibrioCholeraePerExposedPerson()));
    getGlobal().setvibrioCholeraePerInfectedPerson(returnDoubleParameter(parameterDB, "vibrioCholeraePerInfectedPerson",
        getGlobal().getvibrioCholeraePerInfectedPerson()));
    getGlobal().setcholeraInfectionDurationMAX(
        returnIntParameter(parameterDB, "choleraInfectionDurationMAX", getGlobal().getcholeraInfectionDurationMAX()));
    getGlobal().setcholeraInfectionDurationMIN(
        returnIntParameter(parameterDB, "choleraInfectionDurationMIN", getGlobal().getcholeraInfectionDurationMIN()));
    getGlobal().setMaxDistanceLaterine(
        returnIntParameter(parameterDB, "MaxDistanceLaterine", getGlobal().getMaxDistanceLaterine()));
    getGlobal().setBacteriaErosionRate(
        returnDoubleParameter(parameterDB, "bacteriaErosionRate", getGlobal().getBacteriaErosionRate()));
    getGlobal().setBoreHoleDischareRatePerMinute(
        returnDoubleParameter(parameterDB, "boreHoleDischareRate", getGlobal().getBoreHoleDischareRatePerMinute()));
    getGlobal().setBoreholeWaterSupplyPerDay(
        returnDoubleParameter(parameterDB, "waterCapacityBorehole", getGlobal().getBoreholeWaterSupplyPerDay()));
    getGlobal().setHeaalthFacilityCapacity(
        returnIntParameter(parameterDB, "healthFacilityCapacity", getGlobal().getHeaalthFacilityCapacity()));
    getGlobal().setMaximumCrowedLevel(
        returnDoubleParameter(parameterDB, "CROWED_LEVEL_THRESHOLD", getGlobal().getMaximumCrowedLevel()));
    getGlobal().setProbabilityGuestContaminationRate(returnDoubleParameter(parameterDB,
        "probabilityGuestContaminationRate", getGlobal().getProbabilityGuestContaminationRate()));
    getGlobal().setMaximumHHOccumpancyPerField(
        returnIntParameter(parameterDB, "maximum_occupancy_Threshold", getGlobal().getMaximumHHOccumpancyPerField()));
    getGlobal().setMaximumWaterRequirement(
        returnDoubleParameter(parameterDB, "Maximum_Water_Requirement", getGlobal().getMaximumWaterRequirement()));
    getGlobal().setMinimumWaterRequirement(
        returnDoubleParameter(parameterDB, "Minimum_Water_Requirement", getGlobal().getMinimumWaterRequirement()));
    getGlobal()
        .setLaterineCoverage(returnDoubleParameter(parameterDB, "laterineCoverage", getGlobal().getLaterineCoverage()));
    getGlobal().setRainfallDuration_Minute(
        returnIntParameter(parameterDB, "rainDuration", getGlobal().getRainfallDuration_Minute()));
    getGlobal()
        .setRainfallFirstDay(returnIntParameter(parameterDB, "firstRainfallDay", getGlobal().getRainfallFirstDay()));
    getGlobal().setRainfallFrequencyInterval_Days(
        returnIntParameter(parameterDB, "rainfallFrequency", getGlobal().getRainfallFrequencyInterval_Days()));
    getGlobal().setRainfall_MM_Per_Minute(
        returnDoubleParameter(parameterDB, "rainfallInMM", getGlobal().getRainfall_MM_Per_Minute()));
    getGlobal().setAbsorbtionRatePerMinute(
        returnDoubleParameter(parameterDB, "absorbtionRatePerMinute", getGlobal().getAbsorbtionRatePerMinute()));

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

    public int amountOfInfectedHumans = 1;
    public int initialRefugeeNumber = 4000;// min-1000
    public double PercentageOfAsymptomatic = 70; // how many of the total
                                                 // refugee are asymtototic
    public double recovery_To_Susceb_Rate = 0.000001; // prob of change from
                                                      // recovered to
                                                      // suscebtible
    public int MaxDistanceLaterine = 20;
    public int maximum_occupancy_Threshold = 1000; // arbitrary
    public double Maximum_Water_Requirement = 15; // 15 liter per day - for all
                                                  // uses
    public double Minimum_Water_Requirement = 2;
    public int MaximumNumberRelative = 15;
    public double CROWED_LEVEL_THRESHOLD = 4000; // 50% of the cellsize
                                                 // 90*90=8100
    public double probabilityOfEffectiveNessofmedicine = 0.9; // 90% of the time
    public double WaterContaminationThreshold = 10000.0; // --1000/ml
                                                         // http://www.medicalecology.org/water/cholera/cholera.htm
    public double bacteriaErosionRate = 0.8; // how much of the bacteria in
                                             // feces are taken up by water
    public double vibrioCholeraePerHealthyPerson = 100.0; // should be very much
                                                          // less than treshold
    public double vibrioCholeraePerExposedPerson = 100000.0; // should be very
                                                             // much less than
                                                             // treshold
    public double vibrioCholeraePerInfectedPerson = 1000000000.0; // person with
                                                                  // cholera
                                                                  // exert
                                                                  // 1000,000,000
                                                                  // virus/ml
                                                                  // franco.et
                                                                  // al 1997
    public int choleraInfectionDurationMIN = 12; // 12 to 72 hours after
                                                 // ingestion Nichlas et. al
                                                 // 2009Cholera
                                                 // transmission: the host,
                                                 // pathogen and bacteriophage
                                                 // dynamic
    public int choleraInfectionDurationMAX = 72; // 72 hours after ingestion
    public double healthDepreciation = 0.001; // agent will die if not get
                                              // medication in 48 hours (
                                              // assumption) -
                                              // childer will die fast
    public double waterCapacityBorehole = 20; // litre/day/person
    public double boreHoleDischareRate = 0.8; // litre/minute // proportion of
                                              // total water capacity
    public double probabilityGuestContaminationRate = 0.005;// assumption -
                                                            // guest who is
                                                            // infected may
                                                            // contaminte host
                                                            // house- vomite
                                                            // discharge
    public double waterSourcePreferenceBorehole = 0.75;
    public double waterSourcePreferenceRiver = 0.25;
    public double waterCoverageRate = 0.7; // % water coverage
    public int healthFacilityCapacity = 1000; // 500 person/day efficient to
                                              // treat cholera victim
    public double laterineCoverage = 0.6; // % of clean laterine coverage
    public double rainfallInMM = 4.5; // assume 75mm/month - duration = 25
                                      // minute, amount = 0.5mm/minute, freq =
                                      // every
                                      // 5days (6days in a month)
    public int rainDuration = 25; // minute //
    public int firstRainfallDay = 0; // the first onset of rainfall
    public int rainfallFrequency = 25; // rain will fall in days interval
    public double absorbtionRatePerMinute = 4.33;// mm/minute evaporation -
                                                 // taking median 1750 =

    public void setInitialHumansNumber(int num) {
      this.initialRefugeeNumber = num;
    }

    public int getInitialHumansNumber() {
      return initialRefugeeNumber;

    }

    public void setMaximumNumberRelative(int num) {
      this.MaximumNumberRelative = num;
    }

    public int getMaximumNumberRelative() {
      return MaximumNumberRelative;

    }
    // percentage of asymtotic agent

    public void setPercentageOfAsymptomatic(double a) {
      this.PercentageOfAsymptomatic = a;
    }

    public double getPercentageOfAsymptomatic() {
      return PercentageOfAsymptomatic;
    }

    // probability of recovered agent to be suscebtable again
    public void setProbRecoveryToSuscebtable(double rec) {
      this.recovery_To_Susceb_Rate = rec;
    }

    public double getProbRecoveryToSuscebtable() {
      return recovery_To_Susceb_Rate;
    }

    public void setBacteriaErosionRate(double er) {
      this.bacteriaErosionRate = er;
    }

    public double getBacteriaErosionRate() {
      return bacteriaErosionRate;
    }

    public void setHealthDepreciation(double er) {
      this.healthDepreciation = er;
    }

    public double getHealthDepreciation() {
      return healthDepreciation;
    }

    public void setvibrioCholeraePerHealthyPerson(double er) {
      this.vibrioCholeraePerHealthyPerson = er;
    }

    public double getvibrioCholeraePerHealthyPerson() {
      return vibrioCholeraePerHealthyPerson;
    }

    public void setvibrioCholeraePerExposedPerson(double er) {
      this.vibrioCholeraePerExposedPerson = er;
    }

    public double getvibrioCholeraePerExposedPerson() {
      return vibrioCholeraePerExposedPerson;
    }

    public void setvibrioCholeraePerInfectedPerson(double er) {
      this.vibrioCholeraePerInfectedPerson = er;
    }

    public double getvibrioCholeraePerInfectedPerson() {
      return vibrioCholeraePerInfectedPerson;
    }

    public void setcholeraInfectionDurationMAX(int er) {
      this.choleraInfectionDurationMAX = er;
    }

    public int getcholeraInfectionDurationMAX() {
      return choleraInfectionDurationMAX;
    }

    public void setcholeraInfectionDurationMIN(int er) {
      this.choleraInfectionDurationMIN = er;
    }

    public int getcholeraInfectionDurationMIN() {
      return choleraInfectionDurationMIN;
    }

    public void setWaterContaminationThreshold(double er) {
      this.WaterContaminationThreshold = er;
    }

    public double getWaterContaminationThreshold() {
      return WaterContaminationThreshold;
    }

    public void setprobabilityOfEffectiveNessofmedicine(double er) {
      this.probabilityOfEffectiveNessofmedicine = er;
    }

    public double getprobabilityOfEffectiveNessofmedicine() {
      return probabilityOfEffectiveNessofmedicine;
    }
    // determien the number of agent per field or parcel

    public void setMaximumHHOccumpancyPerField(int num) {
      this.maximum_occupancy_Threshold = num;
    }

    public int getMaximumHHOccumpancyPerField() {
      return maximum_occupancy_Threshold;

    }

    // determine the maximum water requirement of agent per day
    public void setMaximumWaterRequirement(double w) {
      this.Maximum_Water_Requirement = w;
    }

    public double getMaximumWaterRequirement() {
      return Maximum_Water_Requirement;
    }

    public void setProbabilityGuestContaminationRate(double w) {
      this.probabilityGuestContaminationRate = w;
    }

    public double getProbabilityGuestContaminationRate() {
      return probabilityGuestContaminationRate;
    }

    // determine the minimum water requirement of agent per day
    public void setMinimumWaterRequirement(double w) {
      this.Minimum_Water_Requirement = w;
    }

    public double getMinimumWaterRequirement() {
      return Minimum_Water_Requirement;
    }

    public void setLaterineCoverage(double w) {
      this.laterineCoverage = w;
    }

    public double getLaterineCoverage() {
      return laterineCoverage;
    }

    public void setMaxDistanceLaterine(int w) {
      this.MaxDistanceLaterine = w;
    }

    public int getMaxDistanceLaterine() {
      return MaxDistanceLaterine;
    }

    // determine how many agent can be stay in a given field or road at the same
    // time
    public void setMaximumCrowedLevel(double c) {
      this.CROWED_LEVEL_THRESHOLD = c;
    }

    public double getMaximumCrowedLevel() {
      return CROWED_LEVEL_THRESHOLD;
    }

    // determine water holding capacity of each borehole (per day)
    public void setBoreholeWaterSupplyPerDay(double w) {

      this.waterCapacityBorehole = w;
    }

    public double getBoreholeWaterSupplyPerDay() {
      return (waterCoverageRate * waterCapacityBorehole * this.getInitialHumansNumber()) / 20.0; // 20
                                                                                                 // boreholes
                                                                                                 // each
    }

    // refill rate of each borehole
    public void setBoreHoleDischareRatePerMinute(double w) {
      this.boreHoleDischareRate = w;
    }

    public double getBoreHoleDischareRatePerMinute() {
      return boreHoleDischareRate * (waterCoverageRate * waterCapacityBorehole * this.getInitialHumansNumber())
          / (1440 * 20.0); // 20 boreholes each / 1440 minute
    }

    // rainfall
    public void setRainfall_MM_Per_Minute(double r) {
      this.rainfallInMM = r;
    }

    public double getRainfall_MM_Per_Minute() {
      return rainfallInMM;
    }

    // seepage indicate amount of water loss from the environment
    // can be as evapotranspiration or seepage
    public void setAbsorbtionRatePerMinute(double r) {
      this.absorbtionRatePerMinute = r;
    }

    public double getAbsorbtionRatePerMinute() {
      return absorbtionRatePerMinute;
    }

    /*
     * water preference of agent there are two sources - borehole ( clean and
     * well treated) and rain ( not treated)
     */
    // weight given to water from borehole
    public void setWaterSourcePreference_Borehole(double r) {
      if (r >= 1.0) {
        r = 1.0;
      }
      this.waterSourcePreferenceBorehole = r;
    }

    public double getWaterSourcePreference_Borehole() {
      return waterSourcePreferenceBorehole;
    }

    public void setWaterSourcePreference_River(double r) {
      double rs = 1 - waterSourcePreferenceBorehole;
      if (r == rs) {
        waterSourcePreferenceRiver = r;
      } else {
        this.waterSourcePreferenceRiver = rs;
      }

    }
    // weight given for water from rainfall

    public double getWaterSourcePreference_River() {
      return waterSourcePreferenceRiver;

    }

    public void setHeaalthFacilityCapacity(int ca) {
      this.healthFacilityCapacity = ca;
    }

    public int getHeaalthFacilityCapacity() {
      return healthFacilityCapacity;
    }

    // frequency of rainfall
    public void setRainfallFrequencyInterval_Days(int r) {
      this.rainfallFrequency = r;
    }

    public int getRainfallFrequencyInterval_Days() {
      return rainfallFrequency;
    }

    // first onset of rainfall
    public void setRainfallFirstDay(int r) {
      this.firstRainfallDay = r;
    }

    public int getRainfallFirstDay() {
      return firstRainfallDay;
    }

    // duration of rainfall
    public void setRainfallDuration_Minute(int r) {
      this.rainDuration = r;
    }

    public int getRainfallDuration_Minute() {
      return rainDuration;
    }

    public int getAmountOfInfectedHumans() {
      return amountOfInfectedHumans;
    }

    public void setAmountOfInfectedHumans(int amountOfInfectedHumans) {
      this.amountOfInfectedHumans = amountOfInfectedHumans;
    }
  }
}
