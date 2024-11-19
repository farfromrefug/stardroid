package com.akylas.skymap.activities;

import com.akylas.skymap.ApplicationComponent;
import com.akylas.skymap.inject.PerActivity;

import dagger.Component;

/**
 * Created by johntaylor on 4/24/16.
 */
@PerActivity
@Component(modules = CompassCalibrationModule.class, dependencies = ApplicationComponent.class)
public interface CompassCalibrationComponent {
  void inject(CompassCalibrationActivity activity);
}

