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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinearAlgebra {

    Logger logger = LoggerFactory.getLogger(LinearAlgebra.class);

    public static double dotProduct3x3(List<Double> u,
                                       List<Double> v) {
        return u.get(0)*v.get(0) + u.get(1)*v.get(1) + u.get(2)*v.get(2);
    }

    public static List<Double> crossProduct3x3(List<Double> u,
                                               List<Double> v) {
        List<Double> result = new ArrayList<>();

        result.add(u.get(1)*v.get(2) - u.get(2)*v.get(1));
        result.add(u.get(2)*v.get(0) - u.get(0)*v.get(2));
        result.add(u.get(0)*v.get(1) - u.get(1)*v.get(0));

        return result;
    }

    public static List<Double> matrixVectorMult3x3(List<List<Double>> matrixA, List<Double> inputVector) {
        double a11 = matrixA.get(0).get(0);
        double a12 = matrixA.get(1).get(0);
        double a13 = matrixA.get(2).get(0);
        
        double a21 = matrixA.get(0).get(1);
        double a22 = matrixA.get(1).get(1);
        double a23 = matrixA.get(2).get(1);
        
        double a31 = matrixA.get(0).get(2);
        double a32 = matrixA.get(1).get(2);
        double a33 = matrixA.get(2).get(2);

        double x = inputVector.get(0);
        double y = inputVector.get(1);
        double z = inputVector.get(2);
        
        double outputX = x*a11 + y*a12 + z*a13;
                
        double outputY = x*a21 + y*a22 + z*a23;
        
        double outputZ = x*a31 + y*a32 + z*a33;

        return Arrays.asList(outputX, outputY, outputZ);
    }
} 
