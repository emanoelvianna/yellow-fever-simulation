package com.model;

import com.main.YellowFever;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;

public class Human implements Steppable {
  private static final long serialVersionUID = 1L;
  private static GeometryFactory factory;

  private MasonGeometry location;
  private PointMoveTo pointMoveTo;
  private Point point;

  public Human(YellowFever yellowFever) {
    Bag allRegions = yellowFever.getBuildingsShape().getGeometries();
    MasonGeometry region = ((MasonGeometry) allRegions.objs[yellowFever.random.nextInt(allRegions.numObjs)]);
    this.point = region.getGeometry().getCentroid();
  }

  public void step(SimState state) {
    YellowFever yellowFever = (YellowFever) state;
    GeomVectorField world = yellowFever.getBuildingsShape();
    Coordinate coord = (Coordinate) point.getCoordinate().clone();
    AffineTransformation translate = null;
  }

  public MasonGeometry getGeometry() {
    return location;
  }

  public PointMoveTo getPointMoveTo() {
    return pointMoveTo;
  }

  public Point getPoint() {
    return point;
  }
}
