package com.akylas.skymap.space

import com.akylas.skymap.ephemeris.SolarSystemBody
import com.akylas.skymap.math.Vector3
import java.util.*

/**
 * The Sun is special as it's at the center of the solar system.
 *
 * It's a sort of trivial sun-orbiting object.
 */
class Sun : SunOrbitingObject(SolarSystemBody.Sun) {
    override val bodySize = -0.83f

    override fun getMyHeliocentricCoordinates(date: Date) =
        Vector3(0.0f, 0.0f, 0.0f)

    // TODO(serafini): For now, return semi-reasonable values for the Sun and
    // Moon. We shouldn't call this method for those bodies, but we want to do
    // something sane if we do.
    override fun getMagnitude(time: Date) = -27.0f
}