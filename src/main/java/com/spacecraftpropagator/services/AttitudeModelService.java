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

import java.util.List;

import com.spacecraftpropagator.model.Quarternion;

public interface AttitudeModelService {
    List<List<List<Double>>> init();
    List<List<List<Double>>> step(double stepSeconds);
    List<Double> applyTorque(Quarternion torqueQuarternion, double torqueNewtonMeters, double numSeconds);
    Quarternion getXAxisQuarternion();
    Quarternion getYAxisQuarternion();
    Quarternion getZAxisQuarternion();
    List<List<List<Double>>> getVisible2DProjectedSpacecraftPolygons();
}
