package com.akylas.skymap.renderer;

import android.content.res.Resources;

import com.akylas.skymap.math.Matrix4x4;
import com.akylas.skymap.math.Vector3;
import com.akylas.skymap.renderer.util.SkyRegionMap;

public interface RenderStateInterface {
  Vector3 getCameraPos();
  Vector3 getLookDir();
  Vector3 getUpDir();
  float getRadiusOfView();
  float getUpAngle();
  float getCosUpAngle();
  float getSinUpAngle();
  int getScreenWidth();
  int getScreenHeight();
  Matrix4x4 getTransformToDeviceMatrix();
  Matrix4x4 getTransformToScreenMatrix();
  Resources getResources();
  boolean getNightVisionMode();
  boolean getEInkVisionMode();
  SkyRegionMap.ActiveRegionData getActiveSkyRegions();
}
