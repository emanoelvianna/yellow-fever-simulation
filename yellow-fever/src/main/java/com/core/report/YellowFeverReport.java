package com.core.report;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.core.YellowFever;

import net.sf.csv4j.CSVWriter;
import sim.engine.SimState;
import sim.engine.Steppable;

public class YellowFeverReport implements Steppable {

  public static final int ORDERING = 3;
  private static final long serialVersionUID = 1L;
  // file names
  private static final String MOSQUITO_STATE_FILE_NAME = "mosquito_state.csv";
  private static final String EGGS_STATE_FILE_NAME = "eggs_state.csv";
  private static final String HUMAN_HEALTH_FILE_NAME = "human_health.csv";
  private static final String MOSQUITO_HEALTH_FILE_NAME = "mosquito_health.csv";
  private static final String HEALTH_CENTER_STATE_FILE_NAME = "health_center_state.csv";

  private YellowFever yellowFever;
  // state statistics
  private BufferedWriter bufferedMosquitoStateWriter;
  private CSVWriter csvMosquitoStatehWriter;
  private BufferedWriter bufferedEggsStatesWriter;
  private CSVWriter csvEggsStatesWriter;
  // health statistics
  private BufferedWriter bufferedMosquitoHealthWriter;
  private CSVWriter csvMosquitoHealthWriter;
  private BufferedWriter bufferedHumanHealthWriter;
  private CSVWriter csvHumanHealthWriter;
  // intervention statistics
  private BufferedWriter bufferedHealthCenterStateWriter;
  private CSVWriter csvHealthCenterStateWriter;

  public YellowFeverReport(YellowFever yellowFever) {
    this.yellowFever = yellowFever;
    this.buildHeaders();
  }

  public void step(SimState state) {
    this.yellowFever = (YellowFever) state;

    // near midnight generate results
    if (this.yellowFever.schedule.getSteps() % 1440 == 1439) {
      this.writeMosquitoStateStatistics();
      this.WriteEggsStatusStatistics();
      this.writeMosquitoHealthStatistics();
      this.writeHumanHealthStatistics();
      this.writeHealthCenterStateStatistics();
    }
  }

  // statistics related to the parameterization of mosquito evolution
  private void writeMosquitoStateStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String amount = Integer.toString(yellowFever.getAllMosquitoes().size());
      String amountOfTransportedEggs = Integer.toString(yellowFever.getAmountOfTransportedEggs());
      String numberDeadMosquitoes = Integer.toString(yellowFever.getAmountDeadMosquitoes());

      data = new String[] { day, amount, amountOfTransportedEggs, numberDeadMosquitoes };
      this.csvMosquitoStatehWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void WriteEggsStatusStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String amount = Integer.toString(yellowFever.getAmountOfEggsInHouses());
      String hatched = Integer.toString(yellowFever.getAmountOfEggsHatched());
      String dead = Integer.toString(yellowFever.getAmountOfDeadEggs());

      data = new String[] { day, amount, hatched, dead };
      this.csvEggsStatesWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  // statistics related to the evolution of the infection
  private void writeMosquitoHealthStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String numberSuscpitable = Integer.toString(yellowFever.getTotalOfMosquitoSusceptible());
      String numberExposed = Integer.toString(yellowFever.getTotalOfMosquitoExposed());
      String numberInfected = Integer.toString(yellowFever.getTotalOfMosquitoesWithInfection());
      String numberDeadMosquitoes = Integer.toString(yellowFever.getAmountDeadMosquitoes());

      data = new String[] { day, numberSuscpitable, numberExposed, numberInfected, numberDeadMosquitoes };
      this.csvMosquitoHealthWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void writeHumanHealthStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String numberSuscpitable = Integer.toString(yellowFever.getTotalOfHumansSusceptible());
      String numberExposed = Integer.toString(yellowFever.getTotalOfHumansExposed());
      String numberMildInfected = Integer.toString(yellowFever.getTotalOfHumansWithMildInfection());
      String numberSevereInfected = Integer.toString(yellowFever.getTotalOfHumansWithSevereInfected());
      String numberToxicInfected = Integer.toString(yellowFever.getTotalOfHumansWithToxicInfected());
      String numberRecovered = Integer.toString(yellowFever.getTotalOfHumansRecovered());
      String numberDeadHumans = Integer.toString(yellowFever.getAmountDeadHumans());

      data = new String[] { day, numberSuscpitable, numberExposed, numberMildInfected, numberSevereInfected,
          numberToxicInfected, numberRecovered, numberDeadHumans };
      this.csvHumanHealthWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  // TODO:
  private void writeHealthCenterStateStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String numberOfVisits = Integer.toString(yellowFever.getTotalVisitsMedicalCenter());
      String numberOfRefused = Integer.toString(yellowFever.getTotalRefusalsInMedicalCenter());
      String maximumCapacity = Boolean.toString(yellowFever.isMaximumCapacity());
      String vaccinesApplied = null;
      String vaccinesAvailable = null;

      data = new String[] { day, numberOfVisits, maximumCapacity };
      this.csvHealthCenterStateWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void finish() {
    try {

      this.bufferedMosquitoStateWriter.close();
      this.bufferedEggsStatesWriter.close();
      this.bufferedMosquitoHealthWriter.close();
      this.bufferedHumanHealthWriter.close();
      this.bufferedHealthCenterStateWriter.close();
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void createFiles() throws IOException {
    this.bufferedMosquitoStateWriter = new BufferedWriter(new FileWriter(MOSQUITO_STATE_FILE_NAME));
    this.csvMosquitoStatehWriter = new CSVWriter(bufferedMosquitoStateWriter);

    this.bufferedEggsStatesWriter = new BufferedWriter(new FileWriter(EGGS_STATE_FILE_NAME));
    this.csvEggsStatesWriter = new CSVWriter(bufferedEggsStatesWriter);

    this.bufferedMosquitoHealthWriter = new BufferedWriter(new FileWriter(MOSQUITO_HEALTH_FILE_NAME));
    this.csvMosquitoHealthWriter = new CSVWriter(bufferedMosquitoHealthWriter);

    this.bufferedHumanHealthWriter = new BufferedWriter(new FileWriter(HUMAN_HEALTH_FILE_NAME));
    this.csvHumanHealthWriter = new CSVWriter(bufferedHumanHealthWriter);

    this.bufferedHealthCenterStateWriter = new BufferedWriter(new FileWriter(HEALTH_CENTER_STATE_FILE_NAME));
    this.csvHealthCenterStateWriter = new CSVWriter(bufferedHealthCenterStateWriter);
  }

  private void buildHeaders() {
    try {
      this.createFiles();
      // statistics related to the parameterization of mosquito evolution
      String[] mosquitoStateHeader = new String[] { "DAY", "AMOUNT", "CARRYING_EGGS", "AMOUNT_OF_DEAD" };
      csvMosquitoStatehWriter.writeLine(mosquitoStateHeader);

      String[] eggsStateHeader = new String[] { "DAY", "AMOUNT_OF_EGGS_IN_HOUSES", "AMOUNT_OF_EGGS_HATCHED",
          "AMOUNT_OF_DEAD_EGGS" };
      csvEggsStatesWriter.writeLine(eggsStateHeader);

      // statistics related to the evolution of the infection
      String[] mosquitosHealthHeader = new String[] { "DAY", "SUSCIPTABLE", "EXPOSED", "INFECTED", "AMOUNT_OF_DEAD" };
      csvMosquitoHealthWriter.writeLine(mosquitosHealthHeader);

      String[] humanHealthHeader = new String[] { "DAY", "SUSCIPTABLE", "EXPOSED", "MILD_INFECTION", "SEVERE_INFECTION",
          "TOXIC_INFECTION", "RECOVERED", "AMOUNT_OF_DEAD" };
      csvHumanHealthWriter.writeLine(humanHealthHeader);

      // statistics related to the intervention
      String[] healthCenterStateHeader = new String[] { "DAY", "NUMBER_OF_VISITS", "MAXIMUM_CAPACITY_ON_DAY" };
      csvHealthCenterStateWriter.writeLine(healthCenterStateHeader);

    } catch (IOException ex) {
      Logger.getLogger(YellowFever.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
