package com.spacecraftpropagator.services;

import java.util.List;

import com.spacecraftpropagator.model.Quarternion;

public interface AttitudeModelService {
    List<List<List<Double>>> init(double momentOfInertia);
    List<List<List<Double>>> step(double stepSeconds);
    List<Double> applyTorque(Quarternion torqueQuarternion, double torqueNewtonMeters, double numSeconds);
    Quarternion getAttitude();
    Quarternion getLastAttitude();
    Quarternion getInitialAttitude();
    List<List<Double>> getSpacecraftPoints();
    List<List<List<Double>>> getVisible2DProjectedSpacecraftPolygons();
}
