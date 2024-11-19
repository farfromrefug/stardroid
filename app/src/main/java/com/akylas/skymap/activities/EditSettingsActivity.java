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
package com.akylas.skymap.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import android.preference.PreferenceActivity;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.akylas.skymap.ApplicationConstants;
import com.akylas.skymap.R;
import com.akylas.skymap.StardroidApplication;
import com.akylas.skymap.activities.util.ActivityLightLevelChanger;
import com.akylas.skymap.activities.util.ActivityLightLevelManager;
import com.akylas.skymap.util.MiscUtil;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

/**
 * Edit the user's preferences.
 */
public class EditSettingsActivity extends AppCompatActivity {
  private MyPreferenceFragment preferenceFragment;
  
  static final public class MyPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preference_screen, rootKey);
    }


  }
  /**
   * These must match the keys in the preference_screen.xml file.
   */
  private static final String LONGITUDE = "longitude";
  private static final String LATITUDE = "latitude";
  private static final String LOCATION = "location";
  private static final String TAG = MiscUtil.getTag(EditSettingsActivity.class);
  private Geocoder geocoder;
  private ActivityLightLevelManager activityLightLevelManager;
  @Inject SharedPreferences sharedPreferences;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((StardroidApplication) getApplication()).getApplicationComponent().inject(this);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    activityLightLevelManager = new ActivityLightLevelManager(
        new ActivityLightLevelChanger(this, null),
        PreferenceManager.getDefaultSharedPreferences(this));
    geocoder = new Geocoder(this);
    preferenceFragment = new MyPreferenceFragment();
    getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
            preferenceFragment).commit();
    
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onStart() {
    super.onStart();
    final Preference locationPreference = preferenceFragment.findPreference(LOCATION);
    Preference latitudePreference = preferenceFragment.findPreference(LATITUDE);
    Preference longitudePreference = preferenceFragment.findPreference(LONGITUDE);
    locationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "Place to be updated to " + newValue);
        return setLatLongFromPlace(newValue.toString());
      }
    });
  
    latitudePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        ((EditTextPreference) locationPreference).setText("");
        return true;
      }
    });
  
    longitudePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        ((EditTextPreference) locationPreference).setText("");
        return true;
      }
    });

    Preference gyroPreference = preferenceFragment.findPreference(
        ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO);
    gyroPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

      public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "Toggling gyro preference " + newValue);
        enableNonGyroSensorPrefs(((Boolean) newValue));
        return true;
      }
    });

    enableNonGyroSensorPrefs(
        sharedPreferences.getBoolean(ApplicationConstants.SHARED_PREFERENCE_DISABLE_GYRO,
            false));
  }

  @Override
  public void onResume() {
    super.onResume();
    activityLightLevelManager.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    updatePreferences();
    activityLightLevelManager.onPause();
  }

//  @Override
//  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
//    // Instantiate the new Fragment.
//    final Bundle args = pref.getExtras();
//    final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
//            getClassLoader(),
//            pref.getFragment());
//    fragment.setArguments(args);
//    fragment.setTargetFragment(caller, 0);
//    // Replace the existing Fragment with the new Fragment.
//    getSupportFragmentManager().beginTransaction()
//            .replace(R.id.settings_container, fragment)
//            .addToBackStack(null)
//            .commit();
//    return true;
//  }

  private void enableNonGyroSensorPrefs(boolean enabled) {
    // These settings aren't compatible with the gyro.
    preferenceFragment.findPreference(
        ApplicationConstants.SENSOR_SPEED_PREF_KEY).setEnabled(enabled);
    preferenceFragment.findPreference(
        ApplicationConstants.SENSOR_DAMPING_PREF_KEY).setEnabled(enabled);
    preferenceFragment.findPreference(
        ApplicationConstants.REVERSE_MAGNETIC_Z_PREFKEY).setEnabled(enabled);
  }

  /**
   * Updates preferences on singletons, so we don't have to register
   * preference change listeners for them.
   */
  private void updatePreferences() {
    Log.d(TAG, "Updating preferences");
  }

  protected boolean setLatLongFromPlace(String place) {
    List<Address> addresses;
    try {
      addresses = geocoder.getFromLocationName(place, 1);
    } catch (IOException e) {
      Toast.makeText(this, getString(R.string.location_unable_to_geocode), Toast.LENGTH_SHORT).show();
      return false;
    }
    if (addresses.isEmpty()) {
      showNotFoundDialog(place);
      return false;
    }
    // TODO(johntaylor) let the user choose, but for now just pick the first.
    Address first = addresses.get(0);
    setLatLong(first.getLatitude(), first.getLongitude());
    return true;
  }

  private void setLatLong(double latitude, double longitude) {
    EditTextPreference latPreference = (EditTextPreference) preferenceFragment.findPreference(LATITUDE);
    EditTextPreference longPreference = (EditTextPreference) preferenceFragment.findPreference(LONGITUDE);
    latPreference.setText(Double.toString(latitude));
    longPreference.setText(Double.toString(longitude));
    String message = String.format(getString(R.string.location_place_found), latitude, longitude);
    Log.d(TAG, message);
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  private void showNotFoundDialog(String place) {
    String message = String.format(getString(R.string.location_not_found), place);
    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this)
        .setTitle(R.string.location_not_found_title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    dialog.show();
  }
}
