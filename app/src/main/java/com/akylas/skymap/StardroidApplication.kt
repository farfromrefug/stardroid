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
package com.akylas.skymap

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import com.akylas.skymap.layers.LayerManager
import com.akylas.skymap.util.MiscUtil.getTag
import java.util.*
import javax.inject.Inject

/**
 * The main Stardroid Application class.
 *
 * @author John Taylor
 */
class StardroidApplication : Application() {
  @Inject
  lateinit var preferences: SharedPreferences

  // We keep a reference to this just to start it initializing.
  @Inject
  lateinit var layerManager: LayerManager


  @Inject
  @JvmField
  var sensorManager: SensorManager? = null

  val applicationComponent: ApplicationComponent = DaggerApplicationComponent.builder()
    .applicationModule(ApplicationModule(this))
    .build()

  override fun onCreate() {
    Log.d(TAG, "StardroidApplication: onCreate")
    super.onCreate()
    applicationComponent.inject(this)
    Log.i(
      TAG, "OS Version: " + Build.VERSION.RELEASE
              + "(" + Build.VERSION.SDK_INT + ")"
    )
    val versionName = versionName
    Log.i(TAG, "Sky Map version $versionName build $version")

    // This populates the default values from the preferences XML file. See
    // {@link DefaultValues} for more details.
    PreferenceManager.setDefaultValues(this, R.xml.preference_screen, false)
//    setUpAnalytics(versionName)
    performFeatureCheck()
    Log.d(TAG, "StardroidApplication: -onCreate")
  }

  override fun onTerminate() {
    super.onTerminate()
  }// TODO(jontayler): update to use the info created by gradle.

  /**
   * Returns the version string for Sky Map.
   */
  val versionName: String
    get() {
      // TODO(jontayler): update to use the info created by gradle.
      val packageManager = packageManager
      return try {
          val info = packageManager.getPackageInfo(this.packageName, 0)
          info.versionName
      } catch (e: PackageManager.NameNotFoundException) {
          Log.e(TAG, "Unable to obtain package info")
          "Unknown"
      }.toString()
    }

  /**
   * Returns the build number for Sky Map.
   */
  val version: Long
    get() {
      val packageManager = packageManager
      return try {
        val info = packageManager.getPackageInfo(this.packageName, 0)
        PackageInfoCompat.getLongVersionCode(info)
      } catch (e: PackageManager.NameNotFoundException) {
        Log.e(TAG, "Unable to obtain package info")
        -1
      }
    }

  /**
   * Check what features are available to this phone and report back to analytics
   * so we can judge when to add/drop support.
   */
  private fun performFeatureCheck() {
    if (sensorManager == null) {
      Log.e(TAG, "No sensor manager")
      return
    }

    // Check for a particularly strange combo - it would be weird to have a rotation sensor
    // but no accelerometer or magnetic field sensor
    var hasRotationSensor = false
    if (hasDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)) {
      if (hasDefaultSensor(Sensor.TYPE_ACCELEROMETER) && hasDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        && hasDefaultSensor(Sensor.TYPE_GYROSCOPE)
      ) {
        hasRotationSensor = true
      } else if (hasDefaultSensor(Sensor.TYPE_ACCELEROMETER) && hasDefaultSensor(
          Sensor.TYPE_MAGNETIC_FIELD
        )
      ) {
        // Even though it allegedly has the rotation vector sensor too many gyro-less phones
        // lie about this, so put these devices on the 'classic' sensor code for now.
        hasRotationSensor = false
      }
    }

    // Enable Gyro if available and user hasn't already disabled it.
    if (!preferences.contains(ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO)) {
      preferences.edit().putBoolean(
        ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO, !hasRotationSensor
      ).apply()
    }

    // Lastly a dump of all the sensors.
    Log.d(TAG, "All sensors:")
    val allSensors = sensorManager?.getSensorList(Sensor.TYPE_ALL)
    val sensorTypes: MutableSet<String> = HashSet()
    for (sensor in allSensors ?: emptyList()) {
      Log.i(TAG, sensor.name)
      sensorTypes.add(getSafeNameForSensor(sensor))
    }
    Log.d(TAG, "All sensors summary:")
    for (sensorType in sensorTypes) {
      Log.i(TAG, sensorType)
    }
  }

  private fun hasDefaultSensor(sensorType: Int): Boolean {
    val sensor = sensorManager?.getDefaultSensor(sensorType) ?: return false
    val dummy: SensorEventListener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        // Nothing
      }

      override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Nothing
      }
    }
    val success = sensorManager?.registerListener(
      dummy, sensor, SensorManager.SENSOR_DELAY_UI
    ) ?: false
    sensorManager?.unregisterListener(dummy)
    return success
  }

  companion object {
    private val TAG = getTag(StardroidApplication::class.java)
    private const val PREVIOUS_APP_VERSION_PREF = "previous_app_version"
    private const val NONE = "Clean install"
    private const val UNKNOWN = "Unknown previous version"

    /**
     * Returns either the name of the sensor or a string version of the sensor type id, depending
     * on the supported OS level along with some context.
     */
    fun getSafeNameForSensor(sensor: Sensor) = "Sensor type: ${sensor.stringType}: ${sensor.type}"
  }
}