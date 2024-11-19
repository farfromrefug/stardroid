package com.akylas.skymap.activities;

import com.akylas.skymap.ApplicationComponent;
import com.akylas.skymap.inject.PerActivity;

import dagger.Component;

/**
 * Created by johntaylor on 4/15/16.
 */
@PerActivity
@Component(modules = DiagnosticActivityModule.class, dependencies = ApplicationComponent.class)
public interface DiagnosticActivityComponent {
  void inject(DiagnosticActivity activity);
}
