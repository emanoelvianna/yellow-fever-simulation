package com.core;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;

public class YellowFever extends SimState {

  private static final long serialVersionUID = 8374233360244345303L;

  private ObjectGrid2D region;
  private GeomGridField regionGeoGrid;
  private DoubleGrid2D rainfallGrid;
  private Continuous2D allHumans;
  private SparseGrid2D facilityGrid;
  private IntGrid2D roadGrid;
  private GeomVectorField roadShape;
  private GeomVectorField regioShape;
  private SparseGrid2D nodes;
  private ObjectGrid2D closestNodes;

  private Bag regionSites;

  public YellowFever(long seed) {
    super(seed);
  }

  public static void main(String[] args) {
    doLoop(YellowFever.class, args);
    System.exit(0);
  }

  @Override
  public void start() {
    new BuildRegion().create(this);
    super.start();
  }

  public ObjectGrid2D getRegion() {
    return region;
  }

  public void setRegion(ObjectGrid2D region) {
    this.region = region;
  }

  public DoubleGrid2D getRainfallGrid() {
    return rainfallGrid;
  }

  public void setRainfallGrid(DoubleGrid2D rainfallGrid) {
    this.rainfallGrid = rainfallGrid;
  }

  public Continuous2D getAllHumans() {
    return allHumans;
  }

  public void setAllHumans(Continuous2D allHumans) {
    this.allHumans = allHumans;
  }

  public SparseGrid2D getFacilityGrid() {
    return facilityGrid;
  }

  public void setFacilityGrid(SparseGrid2D facilityGrid) {
    this.facilityGrid = facilityGrid;
  }

  public IntGrid2D getRoadGrid() {
    return roadGrid;
  }

  public void setRoadGrid(IntGrid2D roadGrid) {
    this.roadGrid = roadGrid;
  }

  public GeomVectorField getRoadShape() {
    return roadShape;
  }

  public void setRoadShape(GeomVectorField roadShape) {
    this.roadShape = roadShape;
  }

  public GeomVectorField getCampShape() {
    return regioShape;
  }

  public void setRegioShape(GeomVectorField regioShape) {
    this.regioShape = regioShape;
  }

  public SparseGrid2D getNodes() {
    return nodes;
  }

  public void setNodes(SparseGrid2D nodes) {
    this.nodes = nodes;
  }

  public ObjectGrid2D getClosestNodes() {
    return closestNodes;
  }

  public void setClosestNodes(ObjectGrid2D closestNodes) {
    this.closestNodes = closestNodes;
  }

  public GeomGridField getRegionGeoGrid() {
    return regionGeoGrid;
  }

  public void setRegionGeoGrid(GeomGridField regionGeoGrid) {
    this.regionGeoGrid = regionGeoGrid;
  }

  public Bag getRegionSites() {
    return regionSites;
  }

  public void setRegionSites(Bag regionSites) {
    this.regionSites = regionSites;
  }

}
