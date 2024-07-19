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
