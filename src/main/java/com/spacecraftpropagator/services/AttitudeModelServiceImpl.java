// Copyright (C) 2024, M. Yang 
// 
//     This program is free software: you can redistribute it and/or modify
//     it under the terms of the GNU General Public License as published by
//     the Free Software Foundation, either version 3 of the License, or
//     (at your option) any later version.
// 
//     This program is distributed in the hope that it will be useful, 
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
//     GNU General Public License for more details.
// 
//     For further details, please see the README.md file included
//     with this software, and/or the GNU General Public License html
//     file which is also included with this software.

package com.spacecraftpropagator.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spacecraftpropagator.model.LinearAlgebra;
import com.spacecraftpropagator.model.Quarternion;

@Service
public class AttitudeModelServiceImpl implements AttitudeModelService {

    private static final String SPACECRAFT_POLYGONS_DATA_JSON_FILENAME = "spacecraftPolygonsData.json";
    private static final String MOMENT_OF_INERTIA_VALUES_JSON_FILENAME = "momentOfInertiaValues.json";

    private static double VIEWING_DISTANCE = 100; // 100 m

    private double momentOfInertiaX; // kg-m^2
    private double momentOfInertiaY; // kg-m^2
    private double momentOfInertiaZ; // kg-m^2
    
    private double radiansPerSecondX = 0.0;
    private double radiansPerSecondY = 0.0;
    private double radiansPerSecondZ = 0.0;

    private Quarternion xAxisQuarternion = new Quarternion(0.0, 1.0, 0.0, 0.0);
    private Quarternion yAxisQuarternion = new Quarternion(0.0, 0.0, 1.0, 0.0);
    private Quarternion zAxisQuarternion = new Quarternion(0.0, 0.0, 0.0, 1.0);

    private List<List<List<Double>>> spacecraftPolygons;

    Logger logger = LoggerFactory.getLogger(AttitudeModelServiceImpl.class);

    @SuppressWarnings("unchecked")
    public AttitudeModelServiceImpl() {
        spacecraftPolygons = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        try (FileInputStream infileStream = new FileInputStream(SPACECRAFT_POLYGONS_DATA_JSON_FILENAME)) {
            spacecraftPolygons = objectMapper.readValue(infileStream, List.class);
            logger.info("spacecraftPolygons read in:  {}", spacecraftPolygons);
        } catch (IOException e) {
            logger.error("Error reading spacecraftPolygons filename {}:  ", SPACECRAFT_POLYGONS_DATA_JSON_FILENAME, e);
        }

        try (FileInputStream infileStream = new FileInputStream(MOMENT_OF_INERTIA_VALUES_JSON_FILENAME)) {
            final List<Double> momentOfInertiaValues = objectMapper.readValue(infileStream, List.class);
 
            this.momentOfInertiaX = momentOfInertiaValues.get(0);
            this.momentOfInertiaY = momentOfInertiaValues.get(1);
            this.momentOfInertiaZ = momentOfInertiaValues.get(2);

            logger.info("momentOfInertia initialization:  ({}, {}, {})", momentOfInertiaX, momentOfInertiaY, momentOfInertiaZ);

        } catch (IOException e) {
            logger.error("Error reading momentOfInertia values filename {}:  ", MOMENT_OF_INERTIA_VALUES_JSON_FILENAME, e);
        }
        
        logger.info("Finished AttitudeModelServiceImpl() constructor.");
    }

    @Override
    public List<List<List<Double>>> init() {        
        return getVisible2DProjectedSpacecraftPolygons();
    }

    @Override
    public synchronized List<List<List<Double>>> step(double stepSeconds) {
        
        double radiansToRotateX = radiansPerSecondX/stepSeconds;
        double radiansToRotateY = radiansPerSecondY/stepSeconds;
        double radiansToRotateZ = radiansPerSecondZ/stepSeconds;

        if (radiansToRotateX != 0 || radiansToRotateY != 0 || radiansToRotateZ != 0) {
            for (int i = 0; i < spacecraftPolygons.size(); i++) {
                List<List<Double>> spacecraftPolygon = spacecraftPolygons.get(i);
                for (int j = 0; j < spacecraftPolygon.size(); j++) {
                    List<Double> coords = spacecraftPolygon.get(j);
                    Quarternion q = new Quarternion(0, coords.get(0), coords.get(1), coords.get(2));
                    q = rotate(q, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
                    coords.set(0, q.getX());
                    coords.set(1, q.getY());
                    coords.set(2, q.getZ());
                    spacecraftPolygon.set(j, coords);
                }
            }

            xAxisQuarternion = rotate(xAxisQuarternion, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
            yAxisQuarternion = rotate(yAxisQuarternion, radiansToRotateX, radiansToRotateY, radiansToRotateZ);
            zAxisQuarternion = rotate(zAxisQuarternion, radiansToRotateX, radiansToRotateY, radiansToRotateZ);            
        }

        return getVisible2DProjectedSpacecraftPolygons();
    }

    @Override
    public synchronized List<Double> applyTorque(Quarternion torqueQuarternion, double torqueNewtonMeters, double numSeconds) {
        final List<Double> torqueAxis = Arrays.asList(torqueQuarternion.getX(), torqueQuarternion.getY(), torqueQuarternion.getZ());
        
        final List<Double> xAxis = Arrays.asList(xAxisQuarternion.getX(), xAxisQuarternion.getY(), xAxisQuarternion.getZ());
        final List<Double> yAxis = Arrays.asList(yAxisQuarternion.getX(), yAxisQuarternion.getY(), yAxisQuarternion.getZ());
        final List<Double> zAxis = Arrays.asList(zAxisQuarternion.getX(), zAxisQuarternion.getY(), zAxisQuarternion.getZ());
        
        final Quarternion momentOfInertiaQ = new Quarternion(0.0, momentOfInertiaX, momentOfInertiaY, momentOfInertiaZ);
        final Quarternion coordTransformedMomentOfInertiaQ =
                momentOfInertiaQ.coordinateTransform(xAxisQuarternion, yAxisQuarternion, zAxisQuarternion);
        final List<Double> coordTransformedMomentOfInertiaAxis = Arrays.asList(coordTransformedMomentOfInertiaQ.getX(), coordTransformedMomentOfInertiaQ.getY(), coordTransformedMomentOfInertiaQ.getZ());

        double momentOfInertia = Math.abs(LinearAlgebra.dotProduct3x3(torqueAxis, coordTransformedMomentOfInertiaAxis));
        logger.info("momentOfInertia about torque vector is:  {}", momentOfInertia);

        double angularAcceleration = torqueNewtonMeters / momentOfInertia; // radians per second per second
        double angularVelocityDelta = angularAcceleration * numSeconds;
        

        
        double xAxisTorque = LinearAlgebra.dotProduct3x3(torqueAxis, xAxis);
        double angularVelocityDeltaX = angularVelocityDelta*xAxisTorque;
        
        double yAxisTorque = LinearAlgebra.dotProduct3x3(torqueAxis, yAxis);
        double angularVelocityDeltaY = angularVelocityDelta*yAxisTorque;
        
        double zAxisTorque = LinearAlgebra.dotProduct3x3(torqueAxis, zAxis);
        double angularVelocityDeltaZ = angularVelocityDelta*zAxisTorque;

        radiansPerSecondX += angularVelocityDeltaX;
        radiansPerSecondY += angularVelocityDeltaY;
        radiansPerSecondZ += angularVelocityDeltaZ;

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
    public List<List<List<Double>>> getVisible2DProjectedSpacecraftPolygons() {
        List<List<List<Double>>> visibleSpacecraft2DProjectedPolygons = new ArrayList<>();
        final List<List<List<Double>>> projectedSpacecraftPolygons = spacecraftPolygons.stream()
                .map(polygon -> polygon.stream().map(coords -> get2DProjectedSpacecraftCoords(coords)).collect(Collectors.toList()))
                .collect(Collectors.toList());

//        final List<List<Double>> projectedAxisPolygons =
//                Arrays.asList(Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0),
//                              Arrays.asList(xAxisQuarternion.getX()*7, xAxisQuarternion.getY()*7, xAxisQuarternion.getZ()*7),
//                              Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0),
//                              Arrays.asList(yAxisQuarternion.getX()*7, yAxisQuarternion.getY()*7, yAxisQuarternion.getZ()*7),
//                              Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0),
//                              Arrays.asList(zAxisQuarternion.getX()*7, zAxisQuarternion.getY()*7, zAxisQuarternion.getZ()*7),
//                              Arrays.asList(0.0, 0.0, SPACECRAFT_HEIGHT/2.0)
//                             )
//                      .stream().map(coords -> get2DProjectedSpacecraftCoords(coords)).collect(Collectors.toList());

        for (List<List<Double>> projectedSpacecraftPolygon : projectedSpacecraftPolygons) {
            List<Double> u = new ArrayList<>();
            u.add(projectedSpacecraftPolygon.get(1).get(0) - projectedSpacecraftPolygon.get(0).get(0));
            u.add(projectedSpacecraftPolygon.get(1).get(1) - projectedSpacecraftPolygon.get(0).get(1));
            u.add(projectedSpacecraftPolygon.get(1).get(2) - projectedSpacecraftPolygon.get(0).get(2));
    
            List<Double> v = new ArrayList<>();
            v.add(projectedSpacecraftPolygon.get(2).get(0) - projectedSpacecraftPolygon.get(1).get(0));
            v.add(projectedSpacecraftPolygon.get(2).get(1) - projectedSpacecraftPolygon.get(1).get(1));
            v.add(projectedSpacecraftPolygon.get(2).get(2) - projectedSpacecraftPolygon.get(1).get(2));
            
            if (LinearAlgebra.crossProduct3x3(u, v).get(2) > 0) { // if z > 0, cross product points toward the viewer from the screen
                visibleSpacecraft2DProjectedPolygons.add(projectedSpacecraftPolygon);
            }    
        }

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
