
package com.github.cneira.emokit;


import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import static com.github.cneira.emokit.Packet.Sensor;

/**
 * Parses unencrypted byte packets from an Emotiv EPOC into domain objects.
 * <p/>
 * Packets are stateful (depending on previous values), so this class is
 * not thread safe and must be called for sequential byte packets.
 *
 * @author Sam Halliday
 */
@NotThreadSafe
@Log
public class EmotivParser {

    @Getter
    private byte lastCounter = -1;

    private int battery;

    private final Map<Packet.Sensor, Integer> quality = Maps.newEnumMap(Sensor.class);

    public Packet parse(long timestamp, byte[] decrypted) {
        // the counter is used to mixin battery and quality levels
        byte counter = decrypted[0];
        if (counter != lastCounter + 1 && lastCounter != 127)
           // log.config("missed a packet");

        if (counter < 0) {
            lastCounter = -1;
            battery = 0xFF & counter;
        } else {
            lastCounter = counter;
        }

        Sensor channel = getQualityChannel(counter);
        if (channel != null) {
            int reading = Sensor.QUALITY.apply(decrypted);
            quality.put(channel, reading);
        }

        //return new Packet(timestamp, battery, decrypted, Maps.newEnumMap(quality));
        return new Packet();
    }

    private Sensor getQualityChannel(byte counter) {
        if (64 <= counter && counter <= 75) {
            counter = (byte) (counter - 64);
        }
        // TODO: https://github.com/fommil/emokit-java/issues/3
//        else if (76 <= counter) {
//            counter = (byte) ((counter - 76) % 4 + 15);
//        }
        switch (counter) {
            case 0:
                return Sensor.F3;
            case 1:
                return Sensor.FC5;
            case 2:
                return Sensor.AF3;
            case 3:
                return Sensor.F7;
            case 4:
                return Sensor.T7;
            case 5:
                return Sensor.P7;
            case 6:
                return Sensor.O1;
            case 7:
                return Sensor.O2;
            case 8:
                return Sensor.P8;
            case 9:
                return Sensor.T8;
            case 10:
                return Sensor.F8;
            case 11:
                return Sensor.AF4;
            case 12:
                return Sensor.FC6;
            case 13:
                return Sensor.F4;
            case 14:
                return Sensor.F8;
            case 15:
                return Sensor.AF4;
            default:
                return null;
        }
    }

}