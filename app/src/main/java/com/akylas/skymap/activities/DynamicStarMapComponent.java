package com.akylas.skymap.activities;

import com.akylas.skymap.ApplicationComponent;
import com.akylas.skymap.activities.dialogs.EulaDialogFragment;
import com.akylas.skymap.activities.dialogs.HelpDialogFragment;
import com.akylas.skymap.activities.dialogs.MultipleSearchResultsDialogFragment;
import com.akylas.skymap.activities.dialogs.NoSearchResultsDialogFragment;
import com.akylas.skymap.activities.dialogs.NoSensorsDialogFragment;
import com.akylas.skymap.activities.dialogs.TimeTravelDialogFragment;
import com.akylas.skymap.inject.PerActivity;

import dagger.Component;

/**
 * Created by johntaylor on 3/29/16.
 */
@PerActivity
@Component(modules = DynamicStarMapModule.class, dependencies = ApplicationComponent.class)
public interface DynamicStarMapComponent extends EulaDialogFragment.ActivityComponent,
    TimeTravelDialogFragment.ActivityComponent, HelpDialogFragment.ActivityComponent,
    NoSearchResultsDialogFragment.ActivityComponent,
    MultipleSearchResultsDialogFragment.ActivityComponent,
    NoSensorsDialogFragment.ActivityComponent {
  void inject(DynamicStarMapActivity activity);
}

