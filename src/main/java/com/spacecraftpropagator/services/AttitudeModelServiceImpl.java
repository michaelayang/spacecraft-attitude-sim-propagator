package com.spacecraftpropagator.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.spacecraftpropagator.model.LinearAlgebra;
import com.spacecraftpropagator.model.Quarternion;

@Service
public class AttitudeModelServiceImpl implements AttitudeModelService {

    private static int NUM_SPACECRAFT_BORDER_POINTS = 6;
    private static double SPACECRAFT_VERTEX_RADIUS = 10; // meters
    private static double SPACECRAFT_ROD_LENGTH = 5; // meters
    private static double SPACECRAFT_HEIGHT = 6.0; // meters
    private static double VIEWING_DISTANCE = 100; // 100 m
    private static double ZERO_ROTATION_THRESHOLD = 1.0e-6;

    private double momentOfInertia; // kg-m^2
    private double radiansPerSecond;

    private Quarternion xAxisQuarternion = new Quarternion(0.0, 1.0, 0.0, 0.0);;
    private Quarternion yAxisQuarternion = new Quarternion(0.0, 0.0, 1.0, 0.0);;
    private Quarternion zAxisQuarternion = new Quarternion(0.0, 0.0, 0.0, 1.0);;
    
    private Quarternion angularVelocityQuarternion; // rotation axis

    private List<List<Double>> spacecraftTopPoints;
    private List<List<Double>> spacecraftBottomPoints;
    private List<List<Double>> rodPoints;
    
    Logger logger = LoggerFactory.getLogger(AttitudeModelServiceImpl.class);

    public AttitudeModelServiceImpl() {
        spacecraftTopPoints = new ArrayList<>();
        spacecraftBottomPoints = new ArrayList<>();
        rodPoints = new ArrayList<>();
        
        for (int i = 0; i < NUM_SPACECRAFT_BORDER_POINTS; i++) {
            List<Double> coords = new ArrayList<>();
            coords.add(Math.cos(2.0*Math.PI*(double)i/(double)NUM_SPACECRAFT_BORDER_POINTS)*SPACECRAFT_VERTEX_RADIUS); // x
            coords.add(Math.sin(2.0*Math.PI*(double)i/(double)NUM_SPACECRAFT_BORDER_POINTS)*SPACECRAFT_VERTEX_RADIUS); // y
            coords.add(SPACECRAFT_HEIGHT/2.0); // z
            spacecraftTopPoints.add(coords);
            
            coords = new ArrayList<>();
            coords.add(Math.cos(2.0*Math.PI*(double)i/(double)NUM_SPACECRAFT_BORDER_POINTS)*SPACECRAFT_VERTEX_RADIUS); // x
            coords.add(Math.sin(2.0*Math.PI*(double)i/(double)NUM_SPACECRAFT_BORDER_POINTS)*SPACECRAFT_VERTEX_RADIUS); // y
            coords.add(-SPACECRAFT_HEIGHT/2.0); // z
            spacecraftBottomPoints.add(coords);
        }
        
        rodPoints.add(Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0));
        rodPoints.add(Arrays.asList(0.0, 0.0, (SPACECRAFT_HEIGHT/2.0)+SPACECRAFT_ROD_LENGTH));
        rodPoints.add(Arrays.asList(-1.0, 0.0, (SPACECRAFT_HEIGHT/2.0)+SPACECRAFT_ROD_LENGTH));
        rodPoints.add(Arrays.asList(0.0, 0.0, (SPACECRAFT_HEIGHT/2.0)+SPACECRAFT_ROD_LENGTH));
        
        angularVelocityQuarternion = new Quarternion(0.0, 0.0, 0.0, 0.0);
        
        radiansPerSecond = 0.0;
        
        logger.info("Finished AttitudeModelServiceImpl() constructor.");
    }

    @Override
    public List<List<List<Double>>> init(double momentOfInertia) { 
        this.momentOfInertia = momentOfInertia;
        
        return getVisible2DProjectedSpacecraftPolygons();
    }

    @Override
    public List<List<List<Double>>> step(double stepSeconds) {
        
        double radiansToRotate = radiansPerSecond/stepSeconds;
        
        if (radiansToRotate != 0) {
            for (int i = 0; i < NUM_SPACECRAFT_BORDER_POINTS; i++) {
                List<Double> coords = spacecraftTopPoints.get(i);
                Quarternion q = new Quarternion(0, coords.get(0), coords.get(1), coords.get(2));
                q = q.rotate(angularVelocityQuarternion, radiansToRotate);
                coords.set(0, q.getX());
                coords.set(1, q.getY());
                coords.set(2, q.getZ());
                spacecraftTopPoints.set(i, coords);
            }
    
            for (int i = 0; i < NUM_SPACECRAFT_BORDER_POINTS; i++) {
                List<Double> coords = spacecraftBottomPoints.get(i);
                Quarternion q = new Quarternion(0, coords.get(0), coords.get(1), coords.get(2));
                q = q.rotate(angularVelocityQuarternion, radiansToRotate);
                coords.set(0, q.getX());
                coords.set(1, q.getY());
                coords.set(2, q.getZ());
                spacecraftBottomPoints.set(i, coords);
            }

            for (int i = 0; i < rodPoints.size(); i++) {
                List<Double> coords = rodPoints.get(i);
                Quarternion q = new Quarternion(0, coords.get(0), coords.get(1), coords.get(2));
                q = q.rotate(angularVelocityQuarternion, radiansToRotate);
                coords.set(0, q.getX());
                coords.set(1, q.getY());
                coords.set(2, q.getZ());
                rodPoints.set(i, coords);
            }

            xAxisQuarternion = xAxisQuarternion.rotate(angularVelocityQuarternion, radiansToRotate);
            yAxisQuarternion = yAxisQuarternion.rotate(angularVelocityQuarternion, radiansToRotate);
            zAxisQuarternion = zAxisQuarternion.rotate(angularVelocityQuarternion, radiansToRotate);
        }

        return getVisible2DProjectedSpacecraftPolygons();
    }

    @Override
    public List<Double> applyTorque(Quarternion torqueQuarternion, double torqueNewtonMeters, double numSeconds) {
        double velocityRadians = radiansPerSecond;

        double angularAcceleration = torqueNewtonMeters / momentOfInertia; // radians per second per second
        double angularVelocityDelta = angularAcceleration * numSeconds;
        
        double norm = angularVelocityQuarternion.norm();
        final Quarternion normalizedAngularVelocityQuarternion;
        if (norm != 0) {
          normalizedAngularVelocityQuarternion = new Quarternion(0, angularVelocityQuarternion.getX()/norm, angularVelocityQuarternion.getY()/norm, angularVelocityQuarternion.getZ()/norm);
        } else {
            normalizedAngularVelocityQuarternion = new Quarternion(0.0, 0.0, 0.0, 0.0);            
        }
        Quarternion scaledAngularVelocityQuarternion =
          new Quarternion(0,
                          normalizedAngularVelocityQuarternion.getX()*velocityRadians,
                          normalizedAngularVelocityQuarternion.getY()*velocityRadians,
                          normalizedAngularVelocityQuarternion.getZ()*velocityRadians);
        
        logger.info("The initial scaledAngularVelocityQuarternion is:  {}", scaledAngularVelocityQuarternion);
        logger.info("For this torque, the torqueQuarternion is:  {}", torqueQuarternion);

        norm = torqueQuarternion.norm();
        Quarternion normalizedAngularAccelerationQuarternion =
          new Quarternion(0, torqueQuarternion.getX()/norm, torqueQuarternion.getY()/norm, torqueQuarternion.getZ()/norm);
        Quarternion scaledAngularVelocityDeltaQuarternion =
          new Quarternion(0,
                          normalizedAngularAccelerationQuarternion.getX()*angularVelocityDelta,
                          normalizedAngularAccelerationQuarternion.getY()*angularVelocityDelta,
                          normalizedAngularAccelerationQuarternion.getZ()*angularVelocityDelta);
        
        logger.info("For this torque, the scaledAngularVelocityDeltaQuarternion is:  {}", scaledAngularVelocityDeltaQuarternion);

        Quarternion resultVelocityQuarternion =
          new Quarternion(0,
                          scaledAngularVelocityQuarternion.getX()+scaledAngularVelocityDeltaQuarternion.getX(),
                          scaledAngularVelocityQuarternion.getY()+scaledAngularVelocityDeltaQuarternion.getY(),
                          scaledAngularVelocityQuarternion.getZ()+scaledAngularVelocityDeltaQuarternion.getZ()
                         );
 
        logger.info("The resultVelocityQuarternion is:  {}", resultVelocityQuarternion);

        radiansPerSecond = resultVelocityQuarternion.norm();
        if (Math.abs(radiansPerSecond) > ZERO_ROTATION_THRESHOLD) {
            angularVelocityQuarternion = new Quarternion(0, resultVelocityQuarternion.getX()/radiansPerSecond, resultVelocityQuarternion.getY()/radiansPerSecond, resultVelocityQuarternion.getZ()/radiansPerSecond);
        } else {
            radiansPerSecond = 0.0;
            angularVelocityQuarternion = new Quarternion(0.0, 0.0, 0.0, 0.0);
        }
        
        logger.info("After torque, the new angularVelocityQuarternion is:  {}", angularVelocityQuarternion);

        final List<Double> returnCoords = Arrays.asList(getZAxisQuarternion().getR(), getZAxisQuarternion().getX(), getZAxisQuarternion().getY(), getZAxisQuarternion().getZ());

        return returnCoords;
    }
    @Override
    public Quarternion getXAxisQuarternion() {
        return xAxisQuarternion;
    }
    @Override
    public Quarternion getYAxisQuarternion() {
        return yAxisQuarternion;
    }

    @Override
    public Quarternion getZAxisQuarternion() {
        return zAxisQuarternion;
    }
    
    @Override
    public List<List<Double>> getSpacecraftPoints() {
        List<List<Double>> spacecraftPoints = new ArrayList<>();
        
        spacecraftPoints.addAll(spacecraftTopPoints);
        spacecraftPoints.addAll(spacecraftBottomPoints);
        spacecraftPoints.addAll(rodPoints);
        
        return spacecraftPoints;
    }
    
    @Override
    public List<List<List<Double>>> getVisible2DProjectedSpacecraftPolygons() {
        List<List<List<Double>>> visibleSpacecraft2DProjectedPolygons = new ArrayList<>();
        final List<List<Double>> projectedSpacecraftTopPoints = spacecraftTopPoints.stream()
                .map(coords -> get2DProjectedSpacecraftCoords(coords))
                .collect(Collectors.toList());
        final List<List<Double>> projectedSpacecraftBottomPoints = spacecraftBottomPoints.stream()
                .map(coords -> get2DProjectedSpacecraftCoords(coords))
                .collect(Collectors.toList());
        final List<List<Double>> projectedRodPoints = rodPoints.stream()
                .map(coords -> get2DProjectedSpacecraftCoords(coords))
                .collect(Collectors.toList());
        
        List<Double> u = new ArrayList<>();
        u.add(projectedSpacecraftTopPoints.get(1).get(0) - projectedSpacecraftTopPoints.get(0).get(0));
        u.add(projectedSpacecraftTopPoints.get(1).get(1) - projectedSpacecraftTopPoints.get(0).get(1));
        u.add(projectedSpacecraftTopPoints.get(1).get(2) - projectedSpacecraftTopPoints.get(0).get(2));

        List<Double> v = new ArrayList<>();
        v.add(projectedSpacecraftTopPoints.get(2).get(0) - projectedSpacecraftTopPoints.get(1).get(0));
        v.add(projectedSpacecraftTopPoints.get(2).get(1) - projectedSpacecraftTopPoints.get(1).get(1));
        v.add(projectedSpacecraftTopPoints.get(2).get(2) - projectedSpacecraftTopPoints.get(1).get(2));
        
        if (LinearAlgebra.crossProduct3x3(u, v).get(2) > 0) { // if z > 0, cross product points toward the viewer from the screen
            visibleSpacecraft2DProjectedPolygons.add(projectedSpacecraftTopPoints);
        }

        u = new ArrayList<>();
        u.add(projectedSpacecraftBottomPoints.get(2).get(0) - projectedSpacecraftBottomPoints.get(1).get(0));
        u.add(projectedSpacecraftBottomPoints.get(2).get(1) - projectedSpacecraftBottomPoints.get(1).get(1));
        u.add(projectedSpacecraftBottomPoints.get(2).get(2) - projectedSpacecraftBottomPoints.get(1).get(2));

        v = new ArrayList<>();
        v.add(projectedSpacecraftBottomPoints.get(1).get(0) - projectedSpacecraftBottomPoints.get(0).get(0));
        v.add(projectedSpacecraftBottomPoints.get(1).get(1) - projectedSpacecraftBottomPoints.get(0).get(1));
        v.add(projectedSpacecraftBottomPoints.get(1).get(2) - projectedSpacecraftBottomPoints.get(0).get(2));
        
        if (LinearAlgebra.crossProduct3x3(u, v).get(2) > 0) { // if z > 0, cross product points toward the viewer from the screen
            visibleSpacecraft2DProjectedPolygons.add(projectedSpacecraftBottomPoints);
        }

        for (int i = 0; i < NUM_SPACECRAFT_BORDER_POINTS; i++) {
            if (i < NUM_SPACECRAFT_BORDER_POINTS-1) {
                u = new ArrayList<>();
                u.add(projectedSpacecraftBottomPoints.get(i+1).get(0) - projectedSpacecraftBottomPoints.get(i).get(0));
                u.add(projectedSpacecraftBottomPoints.get(i+1).get(1) - projectedSpacecraftBottomPoints.get(i).get(1));
                u.add(projectedSpacecraftBottomPoints.get(i+1).get(2) - projectedSpacecraftBottomPoints.get(i).get(2));
        
                v = new ArrayList<>();
                v.add(projectedSpacecraftTopPoints.get(i).get(0) - projectedSpacecraftBottomPoints.get(i).get(0));
                v.add(projectedSpacecraftTopPoints.get(i).get(1) - projectedSpacecraftBottomPoints.get(i).get(1));
                v.add(projectedSpacecraftTopPoints.get(i).get(2) - projectedSpacecraftBottomPoints.get(i).get(2));
            } else {
                u = new ArrayList<>();
                u.add(projectedSpacecraftBottomPoints.get(0).get(0) - projectedSpacecraftBottomPoints.get(i).get(0));
                u.add(projectedSpacecraftBottomPoints.get(0).get(1) - projectedSpacecraftBottomPoints.get(i).get(1));
                u.add(projectedSpacecraftBottomPoints.get(0).get(2) - projectedSpacecraftBottomPoints.get(i).get(2));
        
                v = new ArrayList<>();
                v.add(projectedSpacecraftTopPoints.get(i).get(0) - projectedSpacecraftBottomPoints.get(i).get(0));
                v.add(projectedSpacecraftTopPoints.get(i).get(1) - projectedSpacecraftBottomPoints.get(i).get(1));
                v.add(projectedSpacecraftTopPoints.get(i).get(2) - projectedSpacecraftBottomPoints.get(i).get(2));                
            }
            
            if (LinearAlgebra.crossProduct3x3(u, v).get(2) > 0) { // if z > 0, cross product points toward the viewer from the screen
                if (i < NUM_SPACECRAFT_BORDER_POINTS-1) {
                    List<List<Double>> spacecraftSidePolygon = new ArrayList<>();
                    spacecraftSidePolygon.add(projectedSpacecraftBottomPoints.get(i));
                    spacecraftSidePolygon.add(projectedSpacecraftBottomPoints.get(i+1));
                    spacecraftSidePolygon.add(projectedSpacecraftTopPoints.get(i+1));
                    spacecraftSidePolygon.add(projectedSpacecraftTopPoints.get(i));
                    visibleSpacecraft2DProjectedPolygons.add(spacecraftSidePolygon);
                } else {
                    List<List<Double>> spacecraftSidePolygon = new ArrayList<>();
                    spacecraftSidePolygon.add(projectedSpacecraftBottomPoints.get(i));
                    spacecraftSidePolygon.add(projectedSpacecraftBottomPoints.get(0));
                    spacecraftSidePolygon.add(projectedSpacecraftTopPoints.get(0));
                    spacecraftSidePolygon.add(projectedSpacecraftTopPoints.get(i));
                    visibleSpacecraft2DProjectedPolygons.add(spacecraftSidePolygon);                    
                }
            }
        }

        visibleSpacecraft2DProjectedPolygons.add(projectedRodPoints);
        
        return visibleSpacecraft2DProjectedPolygons;
    }
    
    private List<Double> get2DProjectedSpacecraftCoords(List<Double> coords) {
        Double x = coords.get(0);
        Double y = coords.get(1);
        Double z = coords.get(2);
        
        if (x != null && y != null && z != null) {
            double projectedX = x / (1 - (z/VIEWING_DISTANCE));
            double projectedY = y / (1 - (z/VIEWING_DISTANCE));          
            double projectedZ = z;
            
            List<Double> projectedCoords = new ArrayList<>();
            projectedCoords.add(projectedX);
            projectedCoords.add(projectedY);
            projectedCoords.add(projectedZ);

            return projectedCoords;
        }

        return coords;
    }
}
