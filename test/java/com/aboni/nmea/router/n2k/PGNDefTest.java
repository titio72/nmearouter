package com.aboni.nmea.router.n2k;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PGNDefTest {

    private String jsonWindDataDef = " {\n" +
            " \"PGN\":130306,\n" +
            " \"Id\":\"windData\",\n" +
            " \"Description\":\"Wind Data\",\n" +
            " \"Type\":\"Single\",\n" +
            " \"Complete\":true,\n" +
            " \"Length\":8,\n" +
            " \"RepeatingFields\":0,\n" +
            " \"Fields\":[\n" +
            " {\n" +
            " \"Order\":1,\n" +
            " \"Id\":\"sid\",\n" +
            " \"Name\":\"SID\",\n" +
            " \"BitLength\":8,\n" +
            " \"BitOffset\":0,\n" +
            " \"BitStart\":0,\n" +
            " \"Signed\":false},\n" +
            " {\n" +
            " \"Order\":2,\n" +
            " \"Id\":\"windSpeed\",\n" +
            " \"Name\":\"Wind Speed\",\n" +
            " \"BitLength\":16,\n" +
            " \"BitOffset\":8,\n" +
            " \"BitStart\":0,\n" +
            " \"Units\":\"m/s\",\n" +
            " \"Resolution\":\"0.01\",\n" +
            " \"Signed\":false},\n" +
            " {\n" +
            " \"Order\":3,\n" +
            " \"Id\":\"windAngle\",\n" +
            " \"Name\":\"Wind Angle\",\n" +
            " \"BitLength\":16,\n" +
            " \"BitOffset\":24,\n" +
            " \"BitStart\":0,\n" +
            " \"Units\":\"rad\",\n" +
            " \"Resolution\":\"0.0001\",\n" +
            " \"Signed\":false},\n" +
            " {\n" +
            " \"Order\":4,\n" +
            " \"Id\":\"reference\",\n" +
            " \"Name\":\"Reference\",\n" +
            " \"BitLength\":3,\n" +
            " \"BitOffset\":40,\n" +
            " \"BitStart\":0,\n" +
            " \"Type\":\"Lookup table\",\n" +
            " \"Signed\":false,\n" +
            " \"EnumValues\":[\n" +
            " {\"name\":\"True (ground referenced to North)\",\"value\":\"0\"},\n" +
            " {\"name\":\"Magnetic (ground referenced to Magnetic North)\",\"value\":\"1\"},\n" +
            " {\"name\":\"Apparent\",\"value\":\"2\"},\n" +
            " {\"name\":\"True (boat referenced)\",\"value\":\"3\"},\n" +
            " {\"name\":\"True (water referenced)\",\"value\":\"4\"}]},\n" +
            " {\n" +
            " \"Order\":5,\n" +
            " \"Id\":\"reserved\",\n" +
            " \"Name\":\"Reserved\",\n" +
            " \"BitLength\":21,\n" +
            " \"BitOffset\":43,\n" +
            " \"BitStart\":3,\n" +
            " \"Type\":\"Binary data\",\n" +
            " \"Signed\":false}]}";


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testWindData() {
        PGNDef windDef = new PGNDef(new JSONObject(jsonWindDataDef));
        assertEquals("windData", windDef.getId());
        assertEquals("Wind Data", windDef.getDescription());
        assertEquals(130306, windDef.getPgn());
        assertEquals(5, windDef.getFields().length);

        assertEquals("Reference", windDef.getFields()[3].getName());
        assertEquals("reference", windDef.getFields()[3].getId());
        assertEquals(3, windDef.getFields()[3].getBitLength());
        assertEquals(40, windDef.getFields()[3].getBitOffset());
        assertEquals(0, windDef.getFields()[3].getBitStart());
        assertEquals(PGNDef.PGNFieldType.ENUM, windDef.getFields()[3].getType());
        assertEquals(5, windDef.getFields()[3].getValues().length);

        assertEquals("Wind Speed", windDef.getFields()[1].getName());
        assertEquals("windSpeed", windDef.getFields()[1].getId());
        assertEquals("m/s", windDef.getFields()[1].getUnits());
        assertEquals(16, windDef.getFields()[1].getBitLength());
        assertEquals(8, windDef.getFields()[1].getBitOffset());
        assertEquals(0, windDef.getFields()[1].getBitStart());
        assertEquals(0.01, windDef.getFields()[1].getResolution(), 0.000001);
        assertFalse(windDef.getFields()[1].isSigned());
        assertEquals(PGNDef.PGNFieldType.VALUE, windDef.getFields()[1].getType());

    }

}