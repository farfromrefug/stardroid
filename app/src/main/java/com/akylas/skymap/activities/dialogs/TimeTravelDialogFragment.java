package com.akylas.skymap.activities.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.akylas.skymap.activities.DynamicStarMapActivity;
import com.akylas.skymap.inject.HasComponent;
import com.akylas.skymap.util.MiscUtil;

import javax.inject.Inject;

/**
 * Time travel dialog fragment.
 * Created by johntaylor on 4/3/16.
 */
// TODO(jontayler): see if this crashes when backgrounded on older devices and use
// the fragment in this package if so.
public class TimeTravelDialogFragment extends DialogFragment {
  private static final String TAG = MiscUtil.getTag(TimeTravelDialogFragment.class);
  @Inject DynamicStarMapActivity parentActivity;

  public interface ActivityComponent {
    void inject(TimeTravelDialogFragment fragment);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Activities using this dialog MUST implement this interface.  Obviously.
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);

    return new TimeTravelDialog(parentActivity,
        parentActivity.getModel());
  }
}
