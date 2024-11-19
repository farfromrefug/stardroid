package com.akylas.skymap.activities;


import androidx.appcompat.app.AppCompatActivity;

import com.akylas.skymap.ApplicationComponent;
import com.akylas.skymap.StardroidApplication;

/**
 * Base class for all activities injected by Dagger.
 *
 * Created by johntaylor on 4/9/16.
 */
public abstract class InjectableActivity extends AppCompatActivity {
  protected ApplicationComponent getApplicationComponent() {
    return ((StardroidApplication) getApplication()).getApplicationComponent();
  }
}
