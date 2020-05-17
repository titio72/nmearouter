package com.aboni.sensors;

public interface CompassDataProvider {

    /**
     * Read pitch, roll and heading
     *
     * @return An array of double containing pitch, roll and heading in this order. Null on failure.
     */
    double[] read() throws SensorException;

    /**
     * Initialize the provider for use.
     * Using the the provider may fail if not initialized.
     */
    void init() throws SensorException;

    /**
     * Refresh the configuration (ex: can be used to force reloading of calibration values)
     */
    void refreshConfiguration();
}
