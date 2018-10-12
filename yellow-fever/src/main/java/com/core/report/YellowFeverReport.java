package com.core.report;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.core.YellowFever;
import com.model.Building;
import com.model.Human;
import com.model.enumeration.HealthStatus;

import net.sf.csv4j.CSVWriter;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.io.geo.ArcInfoASCGridExporter;
import sim.util.Bag;

public class YellowFeverReport implements Steppable {

  private static final long serialVersionUID = 1L;
  private static final String INFECTION_STATISTICS_FILE_NAME = "infection_statistics.csv";
  public final static int ORDERING = 3;
  private YellowFever yellowFever;
  private BufferedWriter bufferedWriter;
  private CSVWriter csvWriter;

  public YellowFeverReport(YellowFever dadaab) {
    yellowFever = null;
    this.buildHeaders();
  }

  public YellowFeverReport() {
    this.buildHeaders();
  }

  int count = 0;

  public void step(SimState state) {
    try {
      yellowFever = (YellowFever) state;
      String day = Long.toString(yellowFever.getCurrentDay());
      String numberHumans = Integer.toString(yellowFever.allHumans.getAllObjects().numObjs);
      String numberMosquitoes = Integer.toString(yellowFever.getAllMosquitoes().size());
      String numberSuscpitable = Integer.toString(yellowFever.getNumberOfSusceptible());
      String numberExposed = Integer.toString(yellowFever.getNumberOfExposed());
      String numberMildInfected = Integer.toString(yellowFever.getNumberOfMildInfected());
      String numberSevereInfected = Integer.toString(yellowFever.getNumberOfSevereInfected());
      String numberToxicInfected = Integer.toString(yellowFever.getNumberOfToxicInfected());
      String numberRecovered = Integer.toString(yellowFever.getNumberOfRecovered());
      String numberDeath = Integer.toString(yellowFever.getNumberDeadHumans());

      String[] data;
      data = new String[] { day, numberSuscpitable, numberExposed, numberMildInfected, numberSevereInfected,
          numberToxicInfected, numberRecovered, numberDeath };
      this.csvWriter.writeLine(data);
      
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void finish() {
    try {
      this.bufferedWriter.close();
    } catch (IOException ex) {
      Logger.getLogger(YellowFeverReport.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void createLogFile() throws IOException {
    long now = System.currentTimeMillis();
    String filename;

    filename = String.format("%ty%tm%td%tH%tM%tS", now, now, now, now, now, now) + INFECTION_STATISTICS_FILE_NAME;
    this.bufferedWriter = new BufferedWriter(new FileWriter(filename));
    this.csvWriter = new CSVWriter(bufferedWriter);
  }

  private void buildHeaders() {
    try {
      this.createLogFile();
      String[] humanHealthHeader = new String[] { "DAY", "SUSCIPTABLE", "EXPOSED", "MILD_INFECTION", "SEVERE_INFECTION",
          "TOXIC_INFECTION", "RECOVERED", "DEAD" };
      csvWriter.writeLine(humanHealthHeader);

      String[] mosquitoHealthHeader = new String[] { "DAY", "SUSCIPTABLE", "EXPOSED", "INFECTION", "DEAD" };

      String[] eggsStatusHeader = new String[] { "DAY", "AMOUNT", "DEAD" };

      // TODO: Considerar pessoas não atendidas pela falta de vaga
      String[] healthCenterStateHeader = new String[] { "DAY", "NUMBER_OF_VISITS", "QUANTITY_OF_AVAILABLE_VACCINES",
          "QUANTITY_OF_VACCINES_APPLIED" };

    } catch (IOException ex) {
      Logger.getLogger(YellowFever.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
