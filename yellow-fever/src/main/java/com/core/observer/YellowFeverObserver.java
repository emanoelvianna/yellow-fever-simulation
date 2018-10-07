package com.core.observer;

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

public class YellowFeverObserver implements Steppable {

  private BufferedWriter dataFileBuffer;
  private CSVWriter dataCSVFile_;
  
  private BufferedWriter dataFileBuffer_act;
  private CSVWriter dataCSVFile_act; // CSV file that contains run data

  // hold by camp
  private BufferedWriter dataFileBuffer_camp; // output file buffer for
                                              // dataCSVFile_
  private CSVWriter dataCSVFile_camp;

  private BufferedWriter dataFileBuffer_cStatus; // output file buffer for
                                                 // dataCSVFile_
  private CSVWriter dataCSVFile_cStatus;

  Bag choleraGridBag = new Bag();

  YellowFever yellowFever;

  public final static int ORDERING = 3;

  private int step = 0;
  private boolean writeGrid = false;

  public YellowFeverObserver(YellowFever dadaab) {
    // setup(world);
    // <GCB>: you may want to adjust the number of columns based on these flags.
    // both in createLogFile, and step
    yellowFever = null;
    startLogFile();
  }

  YellowFeverObserver() {
    startLogFile();
  }

  private void startLogFile() {
    // Create a CSV file to capture data for this run.
    try {
      createLogFile();

      // First line of file contains field names
      String[] header = new String[] { "Job", "Step", "Susciptable", "Exposed", "Infected", "Recovered", "Death",
          "Total_vibrio_Cholerae" };
      dataCSVFile_.writeLine(header);

      String[] header_cStatus = new String[] { "Job", "Step", "Newly_Susciptable", "Newly_Exposed", "Newly_Infected",
          "Newly_Recovered", "Death" };
      dataCSVFile_cStatus.writeLine(header_cStatus);
      // activity

      String[] header_act = new String[] { "Job", "Step", "total refugee", "At Home", "School", "Water", "Mosque",
          "Market", "Food C.", "Health C.", "Visit R.", "Social", "Hygiene" };

      dataCSVFile_act.writeLine(header_act);

      String[] header_camp = new String[] { "Job", "Step", "Total Pop", "Dag_sus", "Dag_exp", "Dag_inf", "Dag_rec",
          "Info_sus", "Info_exp", "Info_inf", "Info_rec", "Hag_sus", "Hag_exp", "Hag_inf", "Hag_rec" };

      dataCSVFile_camp.writeLine(header_camp);

    } catch (IOException ex) {
      Logger.getLogger(YellowFever.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  int count = 0;

  public void step(SimState state) {
    yellowFever = (YellowFever) state;

    String job = Long.toString(state.job());
    String numSuscpitable = Integer.toString(yellowFever.getNumberOfSuscipitable());
    String numExposed = Integer.toString(yellowFever.getNumberOfExposed());
    String numInfected = Integer.toString(yellowFever.getNumberOfInfected());
    String numRecovered = Integer.toString(yellowFever.getNumberOfRecovered());
    String numDeath = Integer.toString(yellowFever.countDeath());

    // "At Home","School","Water", "Mosque","Market", "Food C.", "Health
    // C.","Visit
    // R.", "Social","Hygiene"
    // TODO: Refatorar, existem atividades inexistentes
    String numTotAgent = Integer.toString(yellowFever.allHumans.getAllObjects().numObjs);
    String numAtHome = Integer.toString(yellowFever.getTotalActivity()[0]);
    String numSchool = Integer.toString(yellowFever.getTotalActivity()[1]);
    String numWater = Integer.toString(yellowFever.getTotalActivity()[2]);
    String numMosque = Integer.toString(yellowFever.getTotalActivity()[3]);
    String numMarket = Integer.toString(yellowFever.getTotalActivity()[4]);
    String numFoodC = Integer.toString(yellowFever.getTotalActivity()[5]);
    String numHealthC = Integer.toString(yellowFever.getTotalActivity()[6]);
    String numVisitR = Integer.toString(yellowFever.getTotalActivity()[7]);
    String numSocail = Integer.toString(yellowFever.getTotalActivity()[8]);
    String numHygeiene = Integer.toString(yellowFever.getTotalActivity()[9]);

    String numSusDag = Integer.toString(yellowFever.campSuscpitable[0]);
    String numExpDag = Integer.toString(yellowFever.campExposed[0]);
    String numInfDag = Integer.toString(yellowFever.campInfected[0]);
    String numRecDag = Integer.toString(yellowFever.campRecovered[0]);

    String numSusInfo = Integer.toString(yellowFever.campSuscpitable[1]);
    String numExpInfo = Integer.toString(yellowFever.campExposed[1]);
    String numInfInfo = Integer.toString(yellowFever.campInfected[1]);
    String numRecInfo = Integer.toString(yellowFever.campRecovered[1]);

    String numSusHag = Integer.toString(yellowFever.campSuscpitable[2]);
    String numExpHag = Integer.toString(yellowFever.campExposed[2]);
    String numInfHag = Integer.toString(yellowFever.campInfected[2]);
    String numRecHag = Integer.toString(yellowFever.campRecovered[2]);

    // newly cholera cases
    String numSuscpitable_cS = Integer.toString(yellowFever.getNumberOfSuscipitableNewly());
    String numExposed_cS = Integer.toString(yellowFever.getNumberOfExposedNewly());
    String numInfected_cS = Integer.toString(yellowFever.getNumberOfInfectedNewly());
    String numRecovered_cS = Integer.toString(yellowFever.getNumberOfRecoveredNewly());

    // when to export raster;- everyday at midnight
    // // writeGrid =true;
    if (yellowFever.schedule.getSteps() % 1440 == 5) {
      writeGrid = true;
    } else {
      writeGrid = false;
    }
    // writeGrid =false;

    String[] data = new String[] { job, Integer.toString(this.step), numSuscpitable, numExposed, numInfected,
        numRecovered, numDeath };
    String[] data_cS = new String[] { job, Integer.toString(this.step), numSuscpitable_cS, numExposed_cS,
        numInfected_cS, numRecovered_cS, numDeath };

    String[] data_act = new String[] { job, Integer.toString(this.step), numTotAgent, numAtHome, numSchool, numWater,
        numMosque, numMarket, numFoodC, numHealthC, numVisitR, numSocail, numHygeiene };

    String[] data_camp = new String[] { job, Integer.toString(this.step), numTotAgent, numSusDag, numExpDag, numInfDag,
        numRecDag, numSusInfo, numExpInfo, numInfInfo, numRecInfo, numSusHag, numExpHag, numInfHag, numRecHag };

    try {
      this.dataCSVFile_.writeLine(data);

      this.dataCSVFile_act.writeLine(data_act);

      this.dataCSVFile_camp.writeLine(data_camp);

      this.dataCSVFile_cStatus.writeLine(data_cS);

      // some trick to write grid every x step
      if (writeGrid == true) {
        count = count + 1;
        long now = System.currentTimeMillis();
        String filename = String.format("%ty%tm%td%tH%tM%tS", now, now, now, now, now, now) + "d_" + count
            + "_choleraASC.asc";

        BufferedWriter dataASCCholera = new BufferedWriter(new FileWriter(filename));

        writeCholeraSpread();

        ArcInfoASCGridExporter.write(yellowFever.allCampGeoGrid, dataASCCholera);
        choleraGridBag.add(dataASCCholera);

      }

    } catch (IOException ex) {
      Logger.getLogger(YellowFeverObserver.class.getName()).log(Level.SEVERE, null, ex);
    }

    this.step++;
  }

  public void finish() {
    try {
      this.dataFileBuffer.close();
      this.dataFileBuffer_act.close();
      this.dataFileBuffer_camp.close();
      this.dataFileBuffer_cStatus.close();

      for (Object o : choleraGridBag) {
        BufferedWriter bw = (BufferedWriter) o;
        bw.close();
      }

    } catch (IOException ex) {
      Logger.getLogger(YellowFeverObserver.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void createLogFile() throws IOException {
    long now = System.currentTimeMillis();
    //
    String filename = String.format("%ty%tm%td%tH%tM%tS", now, now, now, now, now, now) + "d_cholera.csv";
    // // newly cholera cases
    String filename_cS = String.format("%ty%tm%td%tH%tM%tS", now, now, now, now, now, now) + "d_cholera_newly.csv";
    //
    String filename_act = String.format("%ty%tm%td%tH%tM%tS", now, now, now, now, now, now) + "d_activity.csv";

    String filename_camp = String.format("%ty%tm%td%tH%tM%tS", now, now, now, now, now, now) + "d_cholera_camp.csv";

    // cholera
    this.dataFileBuffer = new BufferedWriter(new FileWriter(filename));

    this.dataCSVFile_ = new CSVWriter(dataFileBuffer);

    // activity
    this.dataFileBuffer_act = new BufferedWriter(new FileWriter(filename_act));

    this.dataCSVFile_act = new CSVWriter(dataFileBuffer_act);

    this.dataFileBuffer_camp = new BufferedWriter(new FileWriter(filename_camp));

    this.dataCSVFile_camp = new CSVWriter(dataFileBuffer_camp);

    // newly cholera cases
    this.dataFileBuffer_cStatus = new BufferedWriter(new FileWriter(filename_cS));
    this.dataCSVFile_cStatus = new CSVWriter(dataFileBuffer_cStatus);

  }

  //
  public void writeCholeraSpread() {

    DoubleGrid2D grid = new DoubleGrid2D(yellowFever.allCamps.getWidth(), yellowFever.allCamps.getHeight());
    // first put all values zero

    for (int i = 0; i < yellowFever.allCamps.getWidth(); i++) {
      for (int j = 0; j < yellowFever.allCamps.getHeight(); j++) {

        Building faci = (Building) yellowFever.allCamps.get(i, j);
        if (faci.getCampID() > 0) {
          grid.field[i][j] = 0;
        }

      }
    }
    // TODO: Verificar a funcionalidade sobre a febre amarela
    // then write the current refugee health status
    for (Object o : yellowFever.allHumans.allObjects) {
      Human r = (Human) o;
      double tot = grid.field[r.getCurrentPosition().getLocationX()][r.getCurrentPosition().getLocationY()];
      if (r.getCurrentHealthStatus() == HealthStatus.MILD_INFECTION) {
        grid.field[r.getCurrentPosition().getLocationX()][r.getCurrentPosition().getLocationY()] = tot + 1;
      } else if (r.getCurrentHealthStatus() == HealthStatus.SEVERE_INFECTION) {
        grid.field[r.getCurrentPosition().getLocationX()][r.getCurrentPosition().getLocationY()] = tot + 1;
      } else if (r.getCurrentHealthStatus() == HealthStatus.TOXIC_INFECTION) {
        grid.field[r.getCurrentPosition().getLocationX()][r.getCurrentPosition().getLocationY()] = tot + 1;
      } else {
        grid.field[r.getCurrentPosition().getLocationX()][r.getCurrentPosition().getLocationY()] = tot;
      }

    }

    yellowFever.allCampGeoGrid.setGrid(grid);

  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeInt(step);

  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    step = in.readInt();

    startLogFile();
  }

}
