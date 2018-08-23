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
  private Bag rainfallWater;
  private Bag allFacilities;
  private Bag schooles;
  private Bag healthCenters;
  private Bag mosques;
  private Bag market;
  private Bag foodCenter;
  private Bag other;

  private int[] dailyRain = new int[365];

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

  public Bag getRainfallWater() {
    return rainfallWater;
  }

  public void setRainfallWater(Bag rainfallWater) {
    this.rainfallWater = rainfallWater;
  }

  public Bag getAllFacilities() {
    return allFacilities;
  }

  public void setAllFacilities(Bag allFacilities) {
    this.allFacilities = allFacilities;
  }

  public Bag getSchooles() {
    return schooles;
  }

  public void setSchooles(Bag schooles) {
    this.schooles = schooles;
  }

  public Bag getHealthCenters() {
    return healthCenters;
  }

  public void setHealthCenters(Bag healthCenters) {
    this.healthCenters = healthCenters;
  }

  public Bag getMosques() {
    return mosques;
  }

  public void setMosques(Bag mosques) {
    this.mosques = mosques;
  }

  public Bag getMarket() {
    return market;
  }

  public void setMarket(Bag market) {
    this.market = market;
  }

  public Bag getFoodCenter() {
    return foodCenter;
  }

  public void setFoodCenter(Bag foodCenter) {
    this.foodCenter = foodCenter;
  }

  public Bag getOther() {
    return other;
  }

  public void setOther(Bag other) {
    this.other = other;
  }

  public int[] getDailyRain() {
    return dailyRain;
  }

  public void setDailyRain(int[] dailyRain) {
    this.dailyRain = dailyRain;
  }

}
