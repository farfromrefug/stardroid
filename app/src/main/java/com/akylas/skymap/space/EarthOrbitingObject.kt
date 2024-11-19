package com.akylas.skymap.space

import com.akylas.skymap.ephemeris.SolarSystemBody

/**
 * An object that orbits Earth.
 */
abstract class EarthOrbitingObject(solarSystemBody : SolarSystemBody) : SolarSystemObject(solarSystemBody)