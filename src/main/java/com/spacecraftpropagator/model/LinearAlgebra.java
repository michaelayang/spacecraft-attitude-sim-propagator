package com.spacecraftpropagator.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinearAlgebra {

    Logger logger = LoggerFactory.getLogger(LinearAlgebra.class);

    public static double dotProduct(List<Double> u,
                                    List<Double> v) {
        return u.get(0)*v.get(0) + u.get(1)*v.get(1) + u.get(2)*v.get(2);
    }

    public static List<Double> crossProduct(List<Double> u,
                                            List<Double> v) {
        List<Double> result = new ArrayList<>();

        result.add(u.get(1)*v.get(2) - u.get(2)*v.get(1));
        result.add(u.get(2)*v.get(0) - u.get(0)*v.get(2));
        result.add(u.get(0)*v.get(1) - u.get(1)*v.get(0));

        return result;
    }

} 
