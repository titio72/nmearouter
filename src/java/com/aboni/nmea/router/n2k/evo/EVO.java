package com.aboni.nmea.router.n2k.evo;

import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.PilotMode;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class EVO {

    private final TimestampProvider tp;
    private final int src;

    @Inject
    public EVO(@NotNull TimestampProvider tp, int src) {
        this.tp = tp;
        this.src = src;
    }

    public N2KMessage getAUTOMessage() {
        try {
            return new N2K126208RequestPilotMode(src, Instant.ofEpochMilli(tp.getNow()), PilotMode.AUTO);
        } catch (PGNDataParseException ignored) {
            return null;
        }
    }

    public N2KMessage getSTDBYMessage() {
        try {
            return new N2K126208RequestPilotMode(src, Instant.ofEpochMilli(tp.getNow()), PilotMode.STANDBY);
        } catch (PGNDataParseException ignored) {
            return null;
        }
    }

    public N2KMessage getLockHeadingMessage(double heading) {
        return new N2K126208RequestLockedHeading(src, Instant.ofEpochMilli(tp.getNow()), heading);
    }

    public N2KMessage getWindDatumMessage(double windAngle) {
        return new N2K126208RequestWindDatum(src, Instant.ofEpochMilli(tp.getNow()), windAngle);
    }

    public N2KMessage getVANEMessage() {
        try {
            return new N2K126208RequestPilotMode(src, Instant.ofEpochMilli(tp.getNow()), PilotMode.VANE);
        } catch (PGNDataParseException ignored) {
            return null;
        }
    }
}
