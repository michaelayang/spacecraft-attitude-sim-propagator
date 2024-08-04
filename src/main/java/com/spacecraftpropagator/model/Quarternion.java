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

package com.spacecraftpropagator.model;

import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Quarternion {

    Logger logger = LoggerFactory.getLogger(Quarternion.class);

    private double r;
    private double x;
    private double y;
    private double z;
   
    public Quarternion(double r,
                       double x,
                       double y,
                       double z) {
        this.r = r;
        this.x = x;
        this.y = y;
        this.z = z;        
    }
    
    public Quarternion rotate(Quarternion angularVelocityAxisQuarternion, double angleToRotate) {
        
//        double norm = angularVelocityAxisQuarternion.norm();
        
//        Quarternion unitAxisQuarternion = new Quarternion(angularVelocityAxisQuarternion.r/norm,
//                                                          angularVelocityAxisQuarternion.x/norm,
//                                                          angularVelocityAxisQuarternion.y/norm,
//                                                          angularVelocityAxisQuarternion.z/norm);

//        double startingQNorm = norm();

//        Quarternion q = new Quarternion(Math.cos(angleToRotate/2.0),
//                                        unitAxisQuarternion.x * Math.sin(angleToRotate/2.0),
//                                        unitAxisQuarternion.y * Math.sin(angleToRotate/2.0),
//                                        unitAxisQuarternion.z * Math.sin(angleToRotate/2.0));
        
      Quarternion q = new Quarternion(Math.cos(angleToRotate/2.0),
              angularVelocityAxisQuarternion.x * Math.sin(angleToRotate/2.0),
              angularVelocityAxisQuarternion.y * Math.sin(angleToRotate/2.0),
              angularVelocityAxisQuarternion.z * Math.sin(angleToRotate/2.0));

        Quarternion intermediateQ = qMultiply(q.r, q.x, q.y, q.z,
                                              r, x, y, z);

        Quarternion inverseQ = qInverse(q.r, q.x, q.y, q.z);

        Quarternion resultQ = qMultiply(intermediateQ.r, intermediateQ.x, intermediateQ.y, intermediateQ.z,
                                        inverseQ.r, inverseQ.x, inverseQ.y, inverseQ.z);

//        double endingQNorm = resultQ.norm();
        
//        resultQ = new Quarternion(resultQ.getR()*startingQNorm/endingQNorm,
//                                  resultQ.getX()*startingQNorm/endingQNorm,
//                                  resultQ.getY()*startingQNorm/endingQNorm,
//                                  resultQ.getZ()*startingQNorm/endingQNorm);

        return resultQ;
    }
    
    public double norm() {
        return Math.sqrt(r*r + x*x + y*y + z*z);
    }
    
    public Quarternion coordinateTransform(Quarternion xAxisQuarternion, Quarternion yAxisQuarternion, Quarternion zAxisQuarternion) {
        
        final List<Double> xBasisVector = Arrays.asList(xAxisQuarternion.getX(), xAxisQuarternion.getY(), xAxisQuarternion.getZ());
        final List<Double> yBasisVector = Arrays.asList(yAxisQuarternion.getX(), yAxisQuarternion.getY(), yAxisQuarternion.getZ());
        final List<Double> zBasisVector = Arrays.asList(zAxisQuarternion.getX(), zAxisQuarternion.getY(), zAxisQuarternion.getZ());
    
        final List<Double> inputVector = Arrays.asList(x, y, z);
        
        final List<Double> outputVector = LinearAlgebra.matrixVectorMult3x3(Arrays.asList(xBasisVector, yBasisVector, zBasisVector), inputVector);

        return new Quarternion(0.0, outputVector.get(0), outputVector.get(1), outputVector.get(2));
    }
    
    private Quarternion qMultiply(double r0,
                                  double x0,
                                  double y0,
                                  double z0,
                                  double r1,
                                  double x1,
                                  double y1,
                                  double z1) {
        return new Quarternion(r0*r1 - x0*x1 - y0*y1 - z0*z1,
                               r0*x1 + x0*r1 + y0*z1 - z0*y1,
                               r0*y1 - x0*z1 + y0*r1 + z0*x1,
                               r0*z1 + x0*y1 - y0*x1 + z0*r1
                              );
    }
    
    private Quarternion qInverse(double r, double x, double y, double z) {
        double normSquared = (r*r) + (x*x) + (y*y) + (z*z);
        return new Quarternion(r/normSquared, -x/normSquared, -y/normSquared, -z/normSquared);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Quarternion)) {
            return false;
        }

        Quarternion other = (Quarternion) o;
        if (Objects.equals(other.r, r)
                && Objects.equals(other.x, x)
                && Objects.equals(other.y, y)
                && Objects.equals(other.z, z)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "Quarternion [r=" + r + ", x=" + x
                + ", y=" + y + ", z=" + z
                + "]";
    }
} 
