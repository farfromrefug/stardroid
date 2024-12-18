package com.akylas.skymap.activities.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.akylas.skymap.R;
import com.akylas.skymap.activities.DynamicStarMapActivity;
import com.akylas.skymap.inject.HasComponent;
import com.akylas.skymap.search.SearchResult;
import com.akylas.skymap.util.MiscUtil;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * End User License agreement dialog.
 * Created by johntaylor on 4/3/16.
 */
public class MultipleSearchResultsDialogFragment extends DialogFragment {
  private static final String TAG = MiscUtil.getTag(MultipleSearchResultsDialogFragment.class);
  @Inject DynamicStarMapActivity parentActivity;

  private ArrayAdapter<SearchResult> multipleSearchResultsAdaptor;

  public interface ActivityComponent {
    void inject(MultipleSearchResultsDialogFragment fragment);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Activities using this dialog MUST implement this interface.  Obviously.
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);

    // TODO(jontayler): inject
    multipleSearchResultsAdaptor = new ArrayAdapter<>(
        parentActivity, android.R.layout.simple_list_item_1, new ArrayList<SearchResult>());


    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        if (whichButton == Dialog.BUTTON_NEGATIVE) {
          Log.d(TAG, "Many search results Dialog closed with cancel");
        } else {
          final SearchResult item = multipleSearchResultsAdaptor.getItem(whichButton);
          parentActivity.activateSearchTarget(item.coords(), item.getCapitalizedName());
        }
        dialog.dismiss();
      }
    };

    return new MaterialAlertDialogBuilder(parentActivity)
        .setTitle(R.string.many_search_results_title)
        .setNegativeButton(android.R.string.cancel, onClickListener)
        .setAdapter(multipleSearchResultsAdaptor, onClickListener)
        .create();
  }

  public void clearResults() {
    multipleSearchResultsAdaptor.clear();
  }

  public void add(SearchResult result) {
    multipleSearchResultsAdaptor.add(result);
  }
}
