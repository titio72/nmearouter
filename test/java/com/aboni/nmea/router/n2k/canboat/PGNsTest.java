package com.aboni.nmea.router.n2k.canboat;

import com.aboni.nmea.router.Constants;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PGNsTest {

    @Test
    public void testLoad() throws Exception {
        PGNs p = new PGNs(Constants.CONF_DIR + "/pgns.json", null);
        PGNDef windDef = p.getPGN(130306);
        assertNotNull(windDef);
    }
}