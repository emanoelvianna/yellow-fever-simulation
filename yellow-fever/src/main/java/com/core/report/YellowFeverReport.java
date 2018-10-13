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
  private static final String HUMAN_HEALTH_FILE_NAME = "human_health.csv";
  private static final String MOSQUITO_HEALTH_FILE_NAME = "mosquito_health.csv";
  private static final String EGGS_STATE_FILE_NAME = "eggs_state.csv";
  private static final String HEALTH_CENTER_STATE_FILE_NAME = "health_center_state.csv";
  private YellowFever yellowFever;
  private BufferedWriter bufferedHumanHealthWriter;
  private CSVWriter csvHumanHealthWriter;
  private BufferedWriter bufferedMosquitoHealthWriter;
  private CSVWriter csvMosquitoHealthWriter;
  private BufferedWriter bufferedEggsStatesWriter;
  private CSVWriter csvEggsStatesWriter;
  private BufferedWriter bufferedHealthCenterStateWriter;
  private CSVWriter csvHealthCenterStateWriter;
  private int currentDay;

  public YellowFeverReport(YellowFever dadaab) {
    yellowFever = null;
    this.currentDay = 0;
    this.buildHeaders();
  }

  public void step(SimState state) {
    yellowFever = (YellowFever) state;
    if (yellowFever.getCurrentDay() != currentDay) {
      this.currentDay = yellowFever.getCurrentDay();
      this.writeHumanHealthStatistics();
      this.writeMosquitoHealthStatistics();
      this.WriteEggsStatusStatistics();
      this.writeHealthCenterStateStatistics();
    }
  }

  private void writeHumanHealthStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String numberHumans = Integer.toString(yellowFever.allHumans.getAllObjects().numObjs);
      String numberSuscpitable = Integer.toString(yellowFever.getTotalOfHumansSusceptible());
      String numberExposed = Integer.toString(yellowFever.getTotalOfHumansExposed());
      String numberMildInfected = Integer.toString(yellowFever.getTotalOfHumansWithMildInfection());
      String numberSevereInfected = Integer.toString(yellowFever.getTotalOfHumansWithSevereInfected());
      String numberToxicInfected = Integer.toString(yellowFever.getTotalOfHumansWithToxicInfected());
      String numberRecovered = Integer.toString(yellowFever.getTotalOfHumansRecovered());
      String numberDeath = Integer.toString(yellowFever.getNumberDeadHumans());

      data = new String[] { day, numberSuscpitable, numberExposed, numberMildInfected, numberSevereInfected,
          numberToxicInfected, numberRecovered, numberDeath };
      this.csvHumanHealthWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void writeMosquitoHealthStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String numberMosquitoes = Integer.toString(yellowFever.getAllMosquitoes().size());
      String numberSuscpitable = Integer.toString(yellowFever.getTotalOfMosquitoSusceptible());
      String numberExposed = Integer.toString(yellowFever.getTotalOfMosquitoExposed());
      String numberInfected = Integer.toString(yellowFever.getTotalOfMosquitoesWithInfection());
      String numberDeath = Integer.toString(yellowFever.getNumberDeadMosquitoes());

      data = new String[] { day, numberSuscpitable, numberExposed, numberInfected, numberDeath };
      this.csvMosquitoHealthWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void WriteEggsStatusStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String amount = Integer.toString(yellowFever.getTotalEggsInHouses());
      String dead = Integer.toString(yellowFever.getTotalOfDeadEggs());

      data = new String[] { day, amount, dead };
      this.csvEggsStatesWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void writeHealthCenterStateStatistics() {
    try {
      String[] data;
      String day = Long.toString(yellowFever.getCurrentDay());
      String numberOfVisits = null;
      String vaccinesAvailable = null;
      String vaccinesApplied = null;

      data = new String[] { day, numberOfVisits, vaccinesAvailable, vaccinesApplied };
      this.csvHealthCenterStateWriter.writeLine(data);
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void finish() {
    try {
      this.bufferedHumanHealthWriter.close();
      this.bufferedMosquitoHealthWriter.close();
      this.bufferedEggsStatesWriter.close();
      this.bufferedHealthCenterStateWriter.close();
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void createFiles() throws IOException {
    this.bufferedHumanHealthWriter = new BufferedWriter(new FileWriter(HUMAN_HEALTH_FILE_NAME));
    this.csvHumanHealthWriter = new CSVWriter(bufferedHumanHealthWriter);

    this.bufferedMosquitoHealthWriter = new BufferedWriter(new FileWriter(MOSQUITO_HEALTH_FILE_NAME));
    this.csvMosquitoHealthWriter = new CSVWriter(bufferedMosquitoHealthWriter);

    this.bufferedEggsStatesWriter = new BufferedWriter(new FileWriter(EGGS_STATE_FILE_NAME));
    this.csvEggsStatesWriter = new CSVWriter(bufferedEggsStatesWriter);

    this.bufferedHealthCenterStateWriter = new BufferedWriter(new FileWriter(HEALTH_CENTER_STATE_FILE_NAME));
    this.csvHealthCenterStateWriter = new CSVWriter(bufferedHealthCenterStateWriter);
  }

  private void buildHeaders() {
    try {
      this.createFiles();
      String[] humanHealthHeader = new String[] { "DAY", "SUSCIPTABLE", "EXPOSED", "MILD_INFECTION", "SEVERE_INFECTION",
          "TOXIC_INFECTION", "RECOVERED", "DEAD" };
      csvHumanHealthWriter.writeLine(humanHealthHeader);

      String[] mosquitoHealthHeader = new String[] { "DAY", "SUSCIPTABLE", "EXPOSED", "INFECTED", "DEAD" };
      csvMosquitoHealthWriter.writeLine(mosquitoHealthHeader);

      String[] eggsStateHeader = new String[] { "DAY", "AMOUNT", "DEAD" };
      csvEggsStatesWriter.writeLine(eggsStateHeader);

      // TODO: Considerar pessoas não atendidas pela falta de vaga
      String[] healthCenterStateHeader = new String[] { "DAY", "NUMBER_OF_VISITS", "VACCINES_AVAILABLE",
          "VACCINES_APPLIED" };
      csvHealthCenterStateWriter.writeLine(healthCenterStateHeader);

    } catch (IOException ex) {
      Logger.getLogger(YellowFever.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
