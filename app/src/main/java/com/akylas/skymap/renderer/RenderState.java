package com.akylas.skymap.renderer;

import android.content.res.Resources;

import com.akylas.skymap.math.MathUtils;
import com.akylas.skymap.math.Matrix4x4;
import com.akylas.skymap.math.Vector3;
import com.akylas.skymap.renderer.util.SkyRegionMap;

// TODO(jpowell): RenderState is a bad name.  This class is a grab-bag of
// general state which is set once per-frame, and which individual managers
// may need to render the frame.  Come up with a better name for this.
public class RenderState implements RenderStateInterface {
  public Vector3 getCameraPos() { return mCameraPos; }
  public Vector3 getLookDir() { return mLookDir; }
  public Vector3 getUpDir() { return mUpDir; }
  public float getRadiusOfView() { return mRadiusOfView; }
  public float getUpAngle() { return mUpAngle; }
  public float getCosUpAngle() { return mCosUpAngle; }
  public float getSinUpAngle() { return mSinUpAngle; }
  public int getScreenWidth() { return mScreenWidth; }
  public int getScreenHeight() { return mScreenHeight; }
  public Matrix4x4 getTransformToDeviceMatrix() { return mTransformToDevice; }
  public Matrix4x4 getTransformToScreenMatrix() { return mTransformToScreen; }
  public Resources getResources() { return mRes; }
  public boolean getNightVisionMode() { return mNightVisionMode; }
  public boolean getEInkVisionMode() { return mEInkVisionMode; }
  public SkyRegionMap.ActiveRegionData getActiveSkyRegions() { return mActiveSkyRegionSet; }

  public void setCameraPos(Vector3 pos) { mCameraPos = pos.copyForJ(); }
  public void setLookDir(Vector3 dir) { mLookDir = dir.copyForJ(); }
  public void setUpDir(Vector3 dir) { mUpDir = dir.copyForJ(); }
  public void setRadiusOfView(float radius) { mRadiusOfView = radius; }
  public void setUpAngle(float angle) {
    mUpAngle = angle;
    mCosUpAngle = MathUtils.cos(angle);
    mSinUpAngle = MathUtils.sin(angle);
  }
  public void setScreenSize(int width, int height) {
    mScreenWidth = width;
    mScreenHeight = height;
  }
  public void setTransformationMatrices(Matrix4x4 transformToDevice,
                                        Matrix4x4 transformToScreen) {
    mTransformToDevice = transformToDevice;
    mTransformToScreen = transformToScreen;
  }
  public void setResources(Resources res) { mRes = res; }
  public void setNightVisionMode(boolean enabled) { mNightVisionMode = enabled; }
  public void setEInkVisionMode(boolean enabled) { mEInkVisionMode = enabled; }
  public void setActiveSkyRegions(SkyRegionMap.ActiveRegionData set) {
    mActiveSkyRegionSet = set;
  }

  private Vector3 mCameraPos = new Vector3(0, 0, 0);
  private Vector3 mLookDir = new Vector3(1, 0, 0);
  private Vector3 mUpDir = new Vector3(0, 1, 0);
  private float mRadiusOfView = 45;  // in degrees
  private float mUpAngle = 0;
  private float mCosUpAngle = 1;
  private float mSinUpAngle = 0;
  private int mScreenWidth = 100;
  private int mScreenHeight = 100;
  private Matrix4x4 mTransformToDevice = Matrix4x4.createIdentity();
  private Matrix4x4 mTransformToScreen = Matrix4x4.createIdentity();
  private Resources mRes;
  private boolean mNightVisionMode = false;
  private boolean mEInkVisionMode = true;
  private SkyRegionMap.ActiveRegionData mActiveSkyRegionSet = null;
}
