package com.aboni.nmea.router;

import org.junit.Test;

import static org.junit.Assert.*;

public class GPSSatsRepositoryTest {


    @Test
    public void test() {
        assertNotNull(GPSSatsRepository.getSat(1));
    }

}