// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.akylas.skymap.layers

import android.content.res.Resources
import android.util.Log
import com.akylas.skymap.R
import com.akylas.skymap.base.TimeConstants
import com.akylas.skymap.control.AstronomerModel
import com.akylas.skymap.ephemeris.SolarSystemBody
import com.akylas.skymap.math.getGeocentricCoords
import com.akylas.skymap.renderer.RendererController
import com.akylas.skymap.search.SearchResult
import com.akylas.skymap.space.Universe
import com.akylas.skymap.util.MiscUtil
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

/**
 * If enabled, keeps the sky gradient up to date.
 *
 * @author John Taylor
 * @author Brent Bryan
 */
class SkyGradientLayer(private val model: AstronomerModel, resources: Resources) :
    Layer {
    private val rendererLock = ReentrantLock()
    private var renderer: RendererController? = null
    private var lastUpdateTimeMs = 0L
    override fun initialize() {}
    override fun registerWithRenderer(rendererController: RendererController) {
        renderer = rendererController
        redraw()
    }

    override fun setVisible(visible: Boolean) {
        Log.d(TAG, "Setting showSkyGradient $visible")
        if (visible) {
            redraw()
        } else {
            rendererLock.lock()
            try {
                renderer?.queueDisableSkyGradient()
            } finally {
                rendererLock.unlock()
            }
        }
    }

    /** Redraws the sky shading gradient using the model's current time.  */
    protected fun redraw() {
        val modelTime = model.time
        if (abs(modelTime.time - lastUpdateTimeMs) > UPDATE_FREQUENCY_MS) {
            lastUpdateTimeMs = modelTime.time
            val sunPosition = universe.solarSystemObjectFor(SolarSystemBody.Sun).getRaDec(modelTime)
            // Log.d(TAG, "Enabling sky gradient with sun position " + sunPosition);
            rendererLock.lock()
            try {
                renderer?.queueEnableSkyGradient(getGeocentricCoords(sunPosition))
            } finally {
                rendererLock.unlock()
            }
        }
    }

    override val layerDepthOrder = -10
    private val layerNameId = R.string.show_sky_gradient
    override val preferenceId = "source_provider.$layerNameId"
    override val layerName = resources.getString(layerNameId)

    override fun searchByObjectName(name: String): List<SearchResult> {
        return emptyList()
    }

    override fun getObjectNamesMatchingPrefix(prefix: String): Set<String> {
        return emptySet()
    }

    companion object {
        private val TAG = MiscUtil.getTag(SkyGradientLayer::class.java)
        private const val UPDATE_FREQUENCY_MS = 5L * TimeConstants.MILLISECONDS_PER_MINUTE
        val universe = Universe()
    }
}