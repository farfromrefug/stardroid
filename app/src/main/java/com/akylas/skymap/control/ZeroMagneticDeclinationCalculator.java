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

import com.akylas.skymap.math.LatLong;

/**
 * A trivial calculator that returns zero magnetic declination.  Used when
 * the user does not want magnetic correction.
 *
 * @author John Taylor
 */
public final class ZeroMagneticDeclinationCalculator implements MagneticDeclinationCalculator {

  @Override
  public float getDeclination() {
    return 0;
  }

  @Override
  public void setLocationAndTime(LatLong location, long timeInMills) {
    // Do nothing.
  }

  @Override
  public String toString() {
    return "Zero Magnetic Correction";
  }
}
