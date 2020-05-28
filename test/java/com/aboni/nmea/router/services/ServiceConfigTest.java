package com.aboni.nmea.router.services;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ServiceConfigTest {

    private class MyServiceConfig implements ServiceConfig {

        private Map<String, String> map = new HashMap<>();

        void setValue(String key, String value) {
            map.put(key, value);
        }

        @Override
        public String getParameter(String pnamne) {
            return map.getOrDefault(pnamne, null);
        }

        @Override
        public String getParameter(String pnamne, String defaultValue) {
            return map.getOrDefault(pnamne, defaultValue);
        }

        @Override
        public String dump() {
            return null;
        }
    }

    private MyServiceConfig config;

    @Before
    public void setup() {
        config = new MyServiceConfig();
    }

    @Test
    public void testGetInteger() {
        config.setValue("p", "1");
        assertEquals(1, config.getInteger("p", 0));
    }

    @Test
    public void testGetIntegerDefault() {
        assertEquals(1, config.getInteger("p", 1));
    }

    @Test
    public void testGetIntegerMalformed() {
        config.setValue("p", "asdad333ererw");
        assertEquals(1, config.getInteger("p", 1));
    }

    @Test
    public void testGetDouble() {
        config.setValue("p", "1.1");
        assertEquals(1.1, config.getDouble("p", 0.0), 0.00001);
    }

    @Test
    public void testGetDoubleDefault() {
        assertEquals(1.1, config.getDouble("p", 1.1), 0.00001);
    }

    @Test
    public void testGetDoubleMalformed() {
        config.setValue("p", "ewrwerw1.1werwerre");
        assertEquals(1.0, config.getDouble("p", 1.0), 0.00001);
    }

    /*
    @Test
    public void testGetParamAsInstant() {
        config.setValue("p", "20200309");
        Instant i = LocalDateTime.parse("2020-03-09T00:00:00").atZone(ZoneId.systemDefault()).toInstant();
        assertEquals(i, config.getParamAsInstant("p", null, 0));
    }

    @Test
    public void testGetParamAsInstantOffset() {
        config.setValue("p", "20200309");
        Instant i = LocalDateTime.parse("2020-03-10T00:00:00").atZone(ZoneId.systemDefault()).toInstant();
        assertEquals(i, config.getParamAsInstant("p", null, 1));
    }
*/

    @Test
    public void testGetParamAsInstantWithTimeAndOffset1() {
        config.setValue("p", "20200308074629+0200");
        Instant i0 = config.getParamAsInstant("p", null, 0);
        Instant i = OffsetDateTime.parse("2020-03-08T07:46:29+02:00").toInstant();
        assertEquals(i, i0);
    }

    @Test
    public void testGetParamAsInstantWithTimeAndOffset2() {
        config.setValue("p", "20200308064629+0000");
        Instant i0 = config.getParamAsInstant("p", null, 0);
        Instant i = LocalDateTime.parse("2020-03-08T07:46:29").atZone(ZoneId.systemDefault()).toInstant();
        assertEquals(i, i0);
    }
}