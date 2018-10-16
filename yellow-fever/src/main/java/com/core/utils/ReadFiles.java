package com.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReadFiles {
  private static final String BUILDINGS_ASCGRID = "data-dadaab/d_camp_a.txt";
  private static final String ROADS_ASCGRID = "data-dadaab/d_costp_a.txt";
  private static final String FACILITY_ASCGRID = "data-dadaab/d_faci_a.txt";
  private static final String ELEVATION_ASCGRID = "data-dadaab/d_dem_n.txt";

  public static void main(String[] args) {
    try {
      String line = null;
      BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output/d_dem_n.txt")));
      FileReader fileReader = new FileReader(new File(ELEVATION_ASCGRID));
      BufferedReader reader = new BufferedReader(fileReader);
      // skip the next lines as they contain irrelevant metadata
      for (int i = 0; i < 6; i++) {
        writer.write(reader.readLine().toString());
        writer.newLine();
      }
      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split("\\s+");
        for (int i = 0; i < 60; i++) {
          writer.write(tokens[i]+" ");
        }
        writer.newLine();
      }
      fileReader.close();
      reader.close();
      // Criando o conteúdo do arquivo
      writer.flush();
      // Fechando conexão e escrita do arquivo.
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
