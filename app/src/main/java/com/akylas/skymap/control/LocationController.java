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

package com.akylas.skymap.control;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.akylas.skymap.ApplicationConstants;
import com.akylas.skymap.R;
import com.akylas.skymap.math.LatLong;
import com.akylas.skymap.util.MiscUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Sets the AstronomerModel's (and thus the user's) position using one of the
 * network, GPS or user-set preferences.
 *
 * @author John Taylor
 */
public class LocationController extends AbstractController implements LocationListener {
  private static final String FORCE_GPS = "force_gps";
  private static final int MINIMUM_DISTANCE_BEFORE_UPDATE_METRES = 2000;
  private static final int LOCATION_UPDATE_TIME_MILLISECONDS = 600000;
  private static final String TAG = MiscUtil.getTag(LocationController.class);
  private static final float MIN_DIST_TO_SHOW_TOAST_DEGS = 0.01f;
  private Context context;
  private LocationManager locationManager;

  private boolean requestingLocation = false;

  public boolean isRequestingLocation() {
    return requestingLocation;
  }

  @Inject
  public LocationController(Context context, @Nullable LocationManager locationManager) {
    this.context = context;
    if (locationManager != null) {
      Log.d(TAG, "Got location Manager");
    } else {
      Log.d(TAG, "Didn't get location manager");
    }
    this.locationManager = locationManager;
  }

  @Override
  public void start() {
    Log.d(TAG, "LocationController start");
    boolean noAutoLocate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            ApplicationConstants.NO_AUTO_LOCATE, false);

    if (noAutoLocate) {
      Log.d(TAG, "User has elected to set location manually.");
      setLocationFromPrefs();
      Log.d(TAG, "LocationController -start");
      return;
    }

    try {
      if (locationManager == null) {
        // TODO(johntaylor): find out under what circumstances this can happen.
        Log.e(TAG, "Location manager was null - using preferences");
        setLocationFromPrefs();
        return;
      }

      Criteria locationCriteria = getCriteria();

      String locationProvider = locationManager.getBestProvider(locationCriteria, true);
      if (locationProvider == null) {
        Log.w(TAG, "No location provider is enabled");
        String possiblelocationProvider = locationManager.getBestProvider(locationCriteria, false);
        if (possiblelocationProvider == null) {
          Log.i(TAG, "No location provider is even available");
          // TODO(johntaylor): should we make this a dialog?
          Toast.makeText(context, R.string.location_no_auto, Toast.LENGTH_LONG).show();
          setLocationFromPrefs();
          return;
        }

        MaterialAlertDialogBuilder alertDialog = getSwitchOnGPSDialog();
        alertDialog.show();
        return;
      }


      Location location = locationManager.getLastKnownLocation(locationProvider);
      if (location != null) {
        LatLong myLocation = new LatLong(location.getLatitude(), location.getLongitude());
        setLocationInModel(myLocation, location.getProvider());
      } else {
        requestLocation();
      }

    } catch (SecurityException securityException) {
      Log.d(TAG, "Caught " + securityException);
      Log.d(TAG, "Most likely user has not enabled this permission");
    }

    Log.d(TAG, "LocationController -start");
  }

  Criteria getCriteria() {
    boolean forceGps = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FORCE_GPS,
            false);
    Criteria locationCriteria = new Criteria();
    locationCriteria.setAccuracy(forceGps ? Criteria.ACCURACY_FINE : Criteria.ACCURACY_COARSE);
    locationCriteria.setAltitudeRequired(false);
    locationCriteria.setBearingRequired(false);
    locationCriteria.setCostAllowed(true);
    locationCriteria.setSpeedRequired(false);
    locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
    return locationCriteria;
  }
  @SuppressLint("MissingPermission")
  public void requestLocation() {
    if (requestingLocation) {
      return;
    }
    requestingLocation = true;
    String locationProvider = locationManager.getBestProvider(getCriteria(), true);
    locationManager.requestLocationUpdates(locationProvider, LOCATION_UPDATE_TIME_MILLISECONDS,
            MINIMUM_DISTANCE_BEFORE_UPDATE_METRES,
            this);
  }

  public void cancelRequestLocation() {
    locationManager.removeUpdates(this);
    requestingLocation = false;
  }

  private void setLocationInModel(LatLong location, String provider) {
    LatLong oldLocation = model.getLocation();
    if (location.distanceFrom(oldLocation) > MIN_DIST_TO_SHOW_TOAST_DEGS) {
      Log.d(TAG, "Informing user of change of location");
      showLocationToUser(location, provider);
    } else {
      Log.d(TAG, "Location not changed sufficiently to tell the user");
    }
    currentProvider = provider;
    model.setLocation(location);
  }

  /**
   * Last known provider;
   */
  private String currentProvider = "unknown";

  public String getCurrentProvider() {
    return currentProvider;
  }

  public LatLong getCurrentLocation() {
    return model.getLocation();
  }

  private MaterialAlertDialogBuilder getSwitchOnGPSDialog() {
    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
    dialog.setTitle(R.string.location_offer_to_enable_gps_title);
    dialog.setMessage(R.string.location_offer_to_enable);
    dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "Sending to editor location prefs page");
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
      }
    });
    dialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "User doesn't want to enable location.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putBoolean(ApplicationConstants.NO_AUTO_LOCATE, true);
        editor.commit();
        setLocationFromPrefs();
      }
    });
    return dialog;
  }

  private void setLocationFromPrefs() {
    Log.d(TAG, "Setting location from preferences");
    String longitude_s = PreferenceManager.getDefaultSharedPreferences(context)
                                          .getString("longitude", "0");
    String latitude_s = PreferenceManager.getDefaultSharedPreferences(context)
                                         .getString("latitude", "0");

    float longitude = 0, latitude = 0;
    try {
      longitude = Float.parseFloat(longitude_s);
      latitude = Float.parseFloat(latitude_s);
    } catch (NumberFormatException nfe) {
      Log.e(TAG, "Error parsing latitude or longitude preference");
      Toast.makeText(context, R.string.malformed_loc_error, Toast.LENGTH_SHORT).show();
    }

    Location location = new Location(context.getString(R.string.preferences));
    location.setLatitude(latitude);
    location.setLongitude(longitude);

    Log.d(TAG, "Latitude " + longitude);
    Log.d(TAG, "Longitude " + latitude);
    LatLong myPosition = new LatLong(latitude, longitude);
    setLocationInModel(myPosition, context.getString(R.string.preferences));
  }

  @Override
  public void stop() {
    Log.d(TAG, "LocationController stop");

    if (locationManager == null) {
      return;
    }
    locationManager.removeUpdates(this);

    Log.d(TAG, "LocationController -stop");
  }

  @Override
  public void onLocationChanged(Location location) {
    Log.d(TAG, "LocationController onLocationChanged");

    if (location == null) {
      Log.e(TAG, "Didn't get location even though onLocationChanged called");
      setLocationFromPrefs();
      return;
    }

    LatLong newLocation = new LatLong(location.getLatitude(), location.getLongitude());

    Log.d(TAG, "Latitude " + newLocation.getLatitude());
    Log.d(TAG, "Longitude " + newLocation.getLongitude());
    setLocationInModel(newLocation, location.getProvider());

    // Only need get the location once.
    locationManager.removeUpdates(this);
    requestingLocation = false;

    Log.d(TAG, "LocationController -onLocationChanged");
  }

  @SuppressLint("StringFormatMatches")
  private void showLocationToUser(LatLong location, String provider) {
    // TODO(johntaylor): move this notification to a separate thread)
    Log.d(TAG, "Reverse geocoding location");
    Geocoder geoCoder = new Geocoder(context);
    List<Address> addresses = new ArrayList<Address>();
    String place = "Unknown";
    try {
      addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
    } catch (IOException e) {
      Log.e(TAG, "Unable to reverse geocode location " + location);
    }

    if (addresses == null || addresses.isEmpty()) {
      Log.d(TAG, "No addresses returned");
      place = String.format(context.getString(R.string.location_long_lat), location.getLongitude(),
              location.getLatitude());
    } else {
      place = getSummaryOfPlace(location, addresses.get(0));
    }

    Log.d(TAG, "Location set to " + place);

    String messageTemplate = context.getString(R.string.location_set_auto);
    String message = String.format(messageTemplate, provider, place);
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

  private String getSummaryOfPlace(LatLong location, Address address) {
    String template = context.getString(R.string.location_long_lat);
    @SuppressLint("StringFormatMatches") String longLat = String.format(template, location.getLongitude(), location.getLatitude());
    if (address == null) {
      return longLat;
    }
    String place = address.getLocality();
    if (place == null) {
      place = address.getSubAdminArea();
    }
    if (place == null) {
      place = address.getAdminArea();
    }
    if (place == null) {
      place = longLat;
    }
    return place;
  }

  @Override
  public void onProviderDisabled(String provider) {
    // No action.
  }

  @Override
  public void onProviderEnabled(String provider) {
    // No action.
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // No action.
  }
}
