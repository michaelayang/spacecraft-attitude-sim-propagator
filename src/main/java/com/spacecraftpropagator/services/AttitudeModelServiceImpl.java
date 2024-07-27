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

    private double momentOfInertia; // kg-m^2
    
    private double radiansPerSecondX = 0.0;
    private double radiansPerSecondY = 0.0;
    private double radiansPerSecondZ = 0.0;

    private Quarternion xAxisQuarternion = new Quarternion(0.0, 1.0, 0.0, 0.0);
    private Quarternion yAxisQuarternion = new Quarternion(0.0, 0.0, 1.0, 0.0);
    private Quarternion zAxisQuarternion = new Quarternion(0.0, 0.0, 0.0, 1.0);

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
        
        logger.info("Finished AttitudeModelServiceImpl() constructor.");
    }

    @Override
    public List<List<List<Double>>> init(double momentOfInertia) { 
        this.momentOfInertia = momentOfInertia;
        
        return getVisible2DProjectedSpacecraftPolygons();
    }

    @Override
    public synchronized List<List<List<Double>>> step(double stepSeconds) {
        
        double radiansToRotateX = radiansPerSecondX/stepSeconds;
        double radiansToRotateY = radiansPerSecondY/stepSeconds;
        double radiansToRotateZ = radiansPerSecondZ/stepSeconds;

        if (radiansToRotateX != 0 || radiansToRotateY != 0 || radiansToRotateZ != 0) {
            for (int i = 0; i < NUM_SPACECRAFT_BORDER_POINTS; i++) {
                List<Double> coords = spacecraftTopPoints.get(i);
                Quarternion q = new Quarternion(0, coords.get(0), coords.get(1), coords.get(2));
                q = rotate(q, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
                coords.set(0, q.getX());
                coords.set(1, q.getY());
                coords.set(2, q.getZ());
                spacecraftTopPoints.set(i, coords);
            }
    
            for (int i = 0; i < NUM_SPACECRAFT_BORDER_POINTS; i++) {
                List<Double> coords = spacecraftBottomPoints.get(i);
                Quarternion q = new Quarternion(0, coords.get(0), coords.get(1), coords.get(2));
                q = rotate(q, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
                coords.set(0, q.getX());
                coords.set(1, q.getY());
                coords.set(2, q.getZ());
                spacecraftBottomPoints.set(i, coords);
            }

            for (int i = 0; i < rodPoints.size(); i++) {
                List<Double> coords = rodPoints.get(i);
                Quarternion q = new Quarternion(0, coords.get(0), coords.get(1), coords.get(2));
                q = rotate(q, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
                coords.set(0, q.getX());
                coords.set(1, q.getY());
                coords.set(2, q.getZ());
                rodPoints.set(i, coords);
            }

            xAxisQuarternion = rotate(xAxisQuarternion, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
            yAxisQuarternion = rotate(yAxisQuarternion, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
            zAxisQuarternion = rotate(zAxisQuarternion, radiansToRotateX, radiansToRotateY, radiansToRotateZ);            
        }

        return getVisible2DProjectedSpacecraftPolygons();
    }

    @Override
    public synchronized List<Double> applyTorque(Quarternion torqueQuarternion, double torqueNewtonMeters, double numSeconds) {
        double angularAcceleration = torqueNewtonMeters / momentOfInertia; // radians per second per second
        double angularVelocityDelta = angularAcceleration * numSeconds;
        
        List<Double> torqueAxis = Arrays.asList(torqueQuarternion.getX(), torqueQuarternion.getY(), torqueQuarternion.getZ());
        
        List<Double> xAxis = Arrays.asList(xAxisQuarternion.getX(), xAxisQuarternion.getY(), xAxisQuarternion.getZ());
        List<Double> yAxis = Arrays.asList(yAxisQuarternion.getX(), yAxisQuarternion.getY(), yAxisQuarternion.getZ());
        List<Double> zAxis = Arrays.asList(zAxisQuarternion.getX(), zAxisQuarternion.getY(), zAxisQuarternion.getZ());
        
        double xAxisTorque = LinearAlgebra.dotProduct3x3(torqueAxis, xAxis);
        double angularVelocityDeltaX = angularVelocityDelta*xAxisTorque;
        
        double yAxisTorque = LinearAlgebra.dotProduct3x3(torqueAxis, yAxis);
        double angularVelocityDeltaY = angularVelocityDelta*yAxisTorque;
        
        double zAxisTorque = LinearAlgebra.dotProduct3x3(torqueAxis, zAxis);
        double angularVelocityDeltaZ = angularVelocityDelta*zAxisTorque;

        radiansPerSecondX += angularVelocityDeltaX;
        radiansPerSecondY += angularVelocityDeltaY;
        radiansPerSecondZ += angularVelocityDeltaZ;

//        logger.info("angularVelocityDelta is {}", angularVelocityDelta);
//        logger.info("xAxisTorque is {}, yAxisTorque is {}, zAxisTorque is {}",
//                    xAxisTorque, yAxisTorque, zAxisTorque);
//        logger.info("angularVelocityDeltaX is {}, angularVelocityDeltaY is {}, angularVelocityDeltaZ is {}",
//                angularVelocityDeltaX, angularVelocityDeltaY, angularVelocityDeltaZ);
//        logger.info("radiansPerSecondX is {}, radiansPerSecondY is {}, radiansPerSecondZ is {}",
//                    radiansPerSecondX, radiansPerSecondY, radiansPerSecondZ);

        return Arrays.asList(zAxisQuarternion.getX(), zAxisQuarternion.getY(), zAxisQuarternion.getZ());
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
        final List<List<Double>> projectedAxisPoints =
                Arrays.asList(Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0),
                              Arrays.asList(xAxisQuarternion.getX()*7, xAxisQuarternion.getY()*7, xAxisQuarternion.getZ()*7),
                              Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0),
                              Arrays.asList(yAxisQuarternion.getX()*7, yAxisQuarternion.getY()*7, yAxisQuarternion.getZ()*7),
                              Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0),
                              Arrays.asList(zAxisQuarternion.getX()*7, zAxisQuarternion.getY()*7, zAxisQuarternion.getZ()*7),
                              Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0)
                             )
                      .stream().map(coords -> get2DProjectedSpacecraftCoords(coords)).collect(Collectors.toList());
        
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

        //visibleSpacecraft2DProjectedPolygons.add(projectedAxisPoints);
 
        return visibleSpacecraft2DProjectedPolygons;
    }

    private Quarternion rotate(Quarternion q, double radiansToRotateX, double radiansToRotateY, double radiansToRotateZ) {
        Quarternion q1 = new Quarternion(q.getR(), q.getX(), q.getY(), q.getZ());
        if (q.getX() != xAxisQuarternion.getX() || q.getY() != xAxisQuarternion.getY() || q.getZ() != xAxisQuarternion.getZ()) {
            q1 = q.rotate(xAxisQuarternion, radiansToRotateX);
        }

        Quarternion q2 = new Quarternion(q1.getR(), q1.getX(), q1.getY(), q1.getZ());
        if (q.getX() != yAxisQuarternion.getX() || q.getY() != yAxisQuarternion.getY() || q.getZ() != yAxisQuarternion.getZ()) {
            q2 = q1.rotate(yAxisQuarternion, radiansToRotateY);
        }

        Quarternion q3 = new Quarternion(q2.getR(), q2.getX(), q2.getY(), q2.getZ());
        if (q.getX() != zAxisQuarternion.getX() || q.getY() != zAxisQuarternion.getY() || q.getZ() != zAxisQuarternion.getZ()) {
            q3 = q2.rotate(zAxisQuarternion, radiansToRotateZ);
        }

        return q3;
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
