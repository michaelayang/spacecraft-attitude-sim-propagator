package com.spacecraftpropagator.model;

import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spacecraftpropagator.services.AttitudeModelServiceImpl;

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
        
        final double norm = angularVelocityAxisQuarternion.norm();
        
        Quarternion unitAxisQuarternion = new Quarternion(angularVelocityAxisQuarternion.r/norm,
                                                          angularVelocityAxisQuarternion.x/norm,
                                                          angularVelocityAxisQuarternion.y/norm,
                                                          angularVelocityAxisQuarternion.z/norm);
        
        Quarternion q = new Quarternion(Math.cos(angleToRotate/2.0),
                                        unitAxisQuarternion.x * Math.sin(angleToRotate/2.0),
                                        unitAxisQuarternion.y * Math.sin(angleToRotate/2.0),
                                        unitAxisQuarternion.z * Math.sin(angleToRotate/2.0));
        //norm = norm(q);
        //q = new Quarternion(q.r/norm, q.x/norm, q.y/norm, q.z/norm);

        Quarternion intermediateQ = qMultiply(q.r, q.x, q.y, q.z,
                                              r, x, y, z);
        //norm = norm(intermediateQ);
        //intermediateQ = new Quarternion(intermediateQ.r/norm, intermediateQ.x/norm, intermediateQ.y/norm, intermediateQ.z/norm);
        
        Quarternion inverseQ = qInverse(q.r, q.x, q.y, q.z);
        //norm = norm(inverseQ);
        //inverseQ = new Quarternion(inverseQ.r/norm, inverseQ.x/norm, inverseQ.y/norm, inverseQ.z/norm);
        
        Quarternion resultQ = qMultiply(intermediateQ.r, intermediateQ.x, intermediateQ.y, intermediateQ.z,
                                  inverseQ.r, inverseQ.x, inverseQ.y, inverseQ.z);
        //norm = norm(resultQ);
        //resultQ = new Quarternion(resultQ.r/norm, resultQ.x/norm, resultQ.y/norm, resultQ.z/norm);
        resultQ = new Quarternion(resultQ.r, resultQ.x, resultQ.y, resultQ.z);

        //logger.info("resultQ:  {}", resultQ);
        
        return resultQ;
    }
    
    public double norm() {
        return Math.sqrt(r*r + x*x + y*y + z*z);
    }
    
    public Quarternion coordinateTransform(Quarternion oldBasisQuarternion, Quarternion newBasisQuarternion) {
        final List<Double> oldBasisVector = Arrays.asList(oldBasisQuarternion.getX(), oldBasisQuarternion.getY(), oldBasisQuarternion.getZ());
        final List<Double> newBasisVector = Arrays.asList(newBasisQuarternion.getX(), newBasisQuarternion.getY(), newBasisQuarternion.getZ());
        
        final List<Double> axisVector = LinearAlgebra.crossProduct(oldBasisVector, newBasisVector);
        final double axisNorm = Math.sqrt(axisVector.get(0)*axisVector.get(0)
                                          + axisVector.get(1)*axisVector.get(1)
                                          + axisVector.get(2)*axisVector.get(2));
        final Quarternion axisQuarternion;
        if (axisNorm != 0) {
            axisQuarternion = new Quarternion(0, axisVector.get(0)/axisNorm, axisVector.get(1)/axisNorm, axisVector.get(2)/axisNorm);
        } else {
            axisQuarternion = new Quarternion(oldBasisQuarternion.getR(), oldBasisQuarternion.getX(), oldBasisQuarternion.getY(), oldBasisQuarternion.getZ());
        }
        final double angleToRotate = Math.acos(LinearAlgebra.dotProduct(oldBasisVector, newBasisVector)); // angle between two basis vectors here

        return rotate(axisQuarternion, angleToRotate);  
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
    public String toString() {
        return "Quarternion [r=" + r + ", x=" + x
                + ", y=" + y + ", z=" + z
                + "]";
    }
} 
