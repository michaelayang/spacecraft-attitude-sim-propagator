package com.spacecraftpropagator.controllers;

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
    public List<List<List<Double>>> init(@RequestBody Double momentOfInertia) {
        logger.info("momentOfInertia param is {}", momentOfInertia);
        List<List<List<Double>>> spacecraftPoints = attitudeModelService.init(momentOfInertia);
        if (spacecraftPoints == null || spacecraftPoints.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No attitude quarternion data returned");
        }
        return spacecraftPoints;
    }
    
    @RequestMapping(value = "/step", method = RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    public List<List<List<Double>>> step(@RequestBody Double stepSeconds) {
        //logger.info("stepSeconds param is {}", stepSeconds);
        List<List<List<Double>>> spacecraftPoints = attitudeModelService.step(stepSeconds);
        if (spacecraftPoints == null || spacecraftPoints.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No quarternion data returned");
        }
        return spacecraftPoints;
    }

    @RequestMapping(value = "/torque", method = RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    public List<Double> torque(@RequestBody TorqueRecord torqueRecord) {
        logger.info("******* torque torqueQuarternion is {} **********", torqueRecord.getTorqueQuarternion());
        logger.info("torque torqueNewtonMeters is {}", torqueRecord.getTorqueNewtonMeters());
        logger.info("spacecraft attitude is {}", attitudeModelService.getAttitude());

        final Quarternion coordTransformedTorqueQ = torqueRecord.getTorqueQuarternion().coordinateTransform(attitudeModelService.getInitialAttitude(), attitudeModelService.getAttitude());

        return attitudeModelService.applyTorque(coordTransformedTorqueQ,
                                                torqueRecord.getTorqueNewtonMeters(),
                                                torqueRecord.getSecondsToApplyTorque());
    }
}
