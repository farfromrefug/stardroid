package com.akylas.skymap.activities.dialogs;

import androidx.fragment.app.FragmentManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A dialog fragment that only shows itself if it's not already shown.  This prevents
 * a java.lang.IllegalStateException when the activity gets backgrounded.
 * Created by johntaylor on 4/11/16.
 */
public abstract class DialogFragment extends androidx.fragment.app.DialogFragment {

  @Override
  public void show(@NonNull androidx.fragment.app.FragmentManager manager, @Nullable String tag) {
    if (this.isAdded()) return;
    super.show(manager, tag);
  }
}
