package com.akylas.skymap.activities.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.akylas.skymap.R;
import com.akylas.skymap.StardroidApplication;
import com.akylas.skymap.inject.HasComponent;
import com.akylas.skymap.util.MiscUtil;

import javax.inject.Inject;

/**
 * Created by johntaylor on 6/10/16.
 */
public class WhatsNewDialogFragment extends DialogFragment {
  private static final String TAG = MiscUtil.getTag(WhatsNewDialogFragment.class);
  @Inject Activity parentActivity;
  private CloseListener closeListener;

  public interface CloseListener {
    void dialogClosed();
  }

  public void setCloseListener(CloseListener closeListener) {
    this.closeListener = closeListener;
  }

  public interface ActivityComponent {
    void inject(WhatsNewDialogFragment fragment);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Activities using this dialog MUST implement this interface.  Obviously.
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);

    LayoutInflater inflater = parentActivity.getLayoutInflater();
    View view = inflater.inflate(R.layout.whatsnew_view, null);

    String whatsNewText = String.format(parentActivity.getString(R.string.whats_new_text), getVersionName());
    Spanned formattedWhatsNewText = Html.fromHtml(whatsNewText);
    TextView whatsNewTextView = (TextView) view.findViewById(R.id.whats_new_box_text);
    whatsNewTextView.setText(formattedWhatsNewText, TextView.BufferType.SPANNABLE);

    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(parentActivity)
        .setTitle(getString(R.string.whats_new_dialog_title))
        .setView(view)
        .setNegativeButton(R.string.dialog_ok_button,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                endItNow(dialog);
              }
            });
    return dialogBuilder.create();
  }

  private void endItNow(DialogInterface dialog) {
    if (closeListener != null) {
      closeListener.dialogClosed();
    }
    dialog.dismiss();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    endItNow(dialog);
  }

  private String getVersionName() {
    return ((StardroidApplication) parentActivity.getApplication()).getVersionName();
  }
}
