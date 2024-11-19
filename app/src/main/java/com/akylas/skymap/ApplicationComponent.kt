package com.akylas.skymap

import android.accounts.AccountManager
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.ConnectivityManager
import com.akylas.skymap.activities.EditSettingsActivity
import com.akylas.skymap.activities.ImageDisplayActivity
import com.akylas.skymap.activities.ImageGalleryActivity
import com.akylas.skymap.control.AstronomerModel
import com.akylas.skymap.control.MagneticDeclinationCalculator
import com.akylas.skymap.layers.LayerManager
import com.akylas.skymap.search.SearchTermsProvider
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger component.
 * Created by johntaylor on 3/26/16.
 */
@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
  // What we expose to dependent components
  fun provideStardroidApplication(): StardroidApplication
  fun provideSharedPreferences(): SharedPreferences
  fun provideSensorManager(): SensorManager?
  fun provideConnectivityManager(): ConnectivityManager?
  fun provideAstronomerModel(): AstronomerModel
  fun provideLocationManager(): LocationManager?
  fun provideLayerManager(): LayerManager
  fun provideAccountManager(): AccountManager

  @Named("zero")
  fun provideMagDec1(): MagneticDeclinationCalculator

  @Named("real")
  fun provideMagDec2(): MagneticDeclinationCalculator

  // Who can we inject
  fun inject(app: StardroidApplication)
  fun inject(activity: EditSettingsActivity)
  fun inject(activity: ImageDisplayActivity)
  fun inject(activity: ImageGalleryActivity)
  fun inject(provider: SearchTermsProvider)
}