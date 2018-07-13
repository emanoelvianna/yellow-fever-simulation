package com.main;

import java.io.IOException;
import java.net.URL;

public class Region {

  public Region() {
    Region.create();
  }

  private static void create() {
    try {
      URL campShapUL = getUrl("/dadaab/dadaabData/Road/Camp_n.shp");
      // TODO:
    } catch (IOException e) {
      // TODO: handle exception
    }
  }

  private static URL getUrl(String nodesFilename) throws IOException {
    // TODO:
    return null;
  }

}
