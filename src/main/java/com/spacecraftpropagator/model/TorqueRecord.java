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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TorqueRecord {

    Logger logger = LoggerFactory.getLogger(TorqueRecord.class);

    private Quarternion torqueQuarternion;
    private double torqueNewtonMeters; 
    private double secondsToApplyTorque;

    public TorqueRecord(Quarternion spacecraftAttitudeQuarternion,
                        double torqueNewtonMeters,
                        double secondsToApplyTorque) {
        this.torqueQuarternion = spacecraftAttitudeQuarternion;
        this.torqueNewtonMeters = torqueNewtonMeters;
        this.secondsToApplyTorque = secondsToApplyTorque;
    }

    @Override
    public String toString() {
        return "TorqueRecord [torqueQuarternion=" + torqueQuarternion
                + ", torqueNewtonMeters=" + torqueNewtonMeters
                + ", secondsToApplyTorque=" + secondsToApplyTorque
                + "]";
    }
} 
