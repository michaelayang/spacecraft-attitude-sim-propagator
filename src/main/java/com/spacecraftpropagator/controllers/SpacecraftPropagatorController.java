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

package com.spacecraftpropagator.controllers;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.spacecraftpropagator.model.LinearAlgebra;
import com.spacecraftpropagator.model.Quarternion;
import com.spacecraftpropagator.model.TorqueRecord;
import com.spacecraftpropagator.services.AttitudeModelService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class SpacecraftPropagatorController {

    Logger logger = LoggerFactory.getLogger(SpacecraftPropagatorController.class);

    @Autowired
    private AttitudeModelService attitudeModelService;

    @RequestMapping(value = "/init", method = RequestMethod.PUT, produces=MediaType.APPLICATION_JSON_VALUE)
    public List<List<List<Double>>> init() {
        List<List<List<Double>>> spacecraftPoints = attitudeModelService.init();
        if (spacecraftPoints == null || spacecraftPoints.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No attitude quarternion data returned");
        }
        return spacecraftPoints;
    }
    
    @RequestMapping(value = "/step", method = RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    public synchronized List<List<List<Double>>> step(@RequestBody Double stepSeconds) {
        //logger.info("stepSeconds param is {}", stepSeconds);
        List<List<List<Double>>> spacecraftPoints = attitudeModelService.step(stepSeconds);
        if (spacecraftPoints == null || spacecraftPoints.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No quarternion data returned");
        }
        return spacecraftPoints;
    }

    @RequestMapping(value = "/torque", method = RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    public synchronized List<Double> torque(@RequestBody TorqueRecord torqueRecord) {
        logger.info("******* torque torqueQuarternion is {} **********", torqueRecord.getTorqueQuarternion());
        logger.info("torque torqueNewtonMeters is {}", torqueRecord.getTorqueNewtonMeters());
        logger.info("spacecraft xAxis is {}, yAxis is {}, zAxis is {}", attitudeModelService.getXAxisQuarternion(), attitudeModelService.getYAxisQuarternion(), attitudeModelService.getZAxisQuarternion());
        logger.info("spacecraft xAxis norm is {}, yAxis norm is {}, zAxis norm is {}", attitudeModelService.getXAxisQuarternion().norm(), attitudeModelService.getYAxisQuarternion().norm(), attitudeModelService.getZAxisQuarternion().norm());

        final Quarternion coordTransformedTorqueQ = torqueRecord.getTorqueQuarternion().coordinateTransform(attitudeModelService.getXAxisQuarternion(), attitudeModelService.getYAxisQuarternion(), attitudeModelService.getZAxisQuarternion());

        return attitudeModelService.applyTorque(coordTransformedTorqueQ,
                                                torqueRecord.getTorqueNewtonMeters(),
                                                torqueRecord.getSecondsToApplyTorque());
    }

    @RequestMapping(value = "/getSunSensorValue", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    public synchronized double getSunSensorValue() {
        final List<Double> xAxisCoords = Arrays.asList(-attitudeModelService.getXAxisQuarternion().getX(),
                                                       attitudeModelService.getXAxisQuarternion().getY(),
                                                       attitudeModelService.getXAxisQuarternion().getZ());
        final List<Double> sunVector = Arrays.asList(-1.0, 0.0, 0.0);
        double sunSensorValue = LinearAlgebra.dotProduct3x3(xAxisCoords, sunVector); // will return cosine of angle between spacecraft -X axis and sun vector towards left side of screen
        
        logger.info("+++++++++++++++ xAxis is {}, spacecraft sun sensor value is {} +++++++++++",
                    attitudeModelService.getXAxisQuarternion(), sunSensorValue);

        return sunSensorValue;
    }

    @RequestMapping(value = "/getIRValue", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    public synchronized double getIRValue() {
        final List<Double> yAxisCoords = Arrays.asList(attitudeModelService.getYAxisQuarternion().getX(),
                                                       -attitudeModelService.getYAxisQuarternion().getY(),
                                                       attitudeModelService.getYAxisQuarternion().getZ());
        final List<Double> irVector = Arrays.asList(0.0, -1.0, 0.0);
        double irSensorValue = LinearAlgebra.dotProduct3x3(yAxisCoords, irVector); // will return cosine of angle between spacecraft -Y axis and IR vector towards bottom of screen, representing IR emissions from Earth
        
        logger.info("+++++++++++++++ yAxis is {}, spacecraft IR value is {} +++++++++++",
                    attitudeModelService.getYAxisQuarternion(), irSensorValue);

        return irSensorValue;
    }
}
