package com.akylas.skymap.activities.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.akylas.skymap.R;
import com.akylas.skymap.util.MiscUtil;

/**
 * Dialog explaining the need for the auto-location permission.
 * Created by johntaylor on 4/3/16.
 */
public class LocationPermissionRationaleFragment extends DialogFragment implements Dialog.OnClickListener {
  private static final String TAG = MiscUtil.getTag(EulaDialogFragment.class);
  private Callback resultListener;

  public interface Callback {
    void done();
  }

  public LocationPermissionRationaleFragment() {
  }

  public void setCallback(Callback resultListener) {
    this.resultListener = resultListener;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getActivity())
        .setTitle(R.string.location_rationale_title)
        .setMessage(R.string.location_rationale_text)
        .setNeutralButton(R.string.dialog_ok_button, LocationPermissionRationaleFragment.this);
    return dialogBuilder.create();
  }

  @Override
  public void onClick(DialogInterface ignore1, int ignore2) {
    if (resultListener != null) {
      resultListener.done();
    }
  }
}
