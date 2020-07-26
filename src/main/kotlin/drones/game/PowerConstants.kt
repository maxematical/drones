package drones.game

object PowerConstants {
    // All quantities are in kW (the units don't really matter, but the power mechanic seems cooler and more "realistic"
    // this way)
    const val MINING_LASER_START = 1.5
    const val MINING_LASER_PER_SECOND = 0.75

    /**
     * How many kW of power are used when applying 1 unit of acceleration over 1 second with the thrusters.
     *
     * The actual amount of kW used for thrusters is calculated as follows:
     * `THRUST_CONSTANT * length(Acceleration) * Seconds`
     */
    const val THRUST_CONSTANT = 2.5

    /**
     * How many kW of power needed to turn one degree.
     */
    const val ROTATION_CONSTANT = 0.2 / 360.0

    // In the future, power levels will not simply recharge after a period of time, but right now, there isn't any other
    // way to get more power if you run out.
    const val RECHARGE_DELAY = 2.0
    const val RECHARGE_PER_SECOND = 3.0
    const val SHUTDOWN_LENGTH = 5.0
}
