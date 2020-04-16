package com.halcyonwaves.apps.energize.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import com.halcyonwaves.apps.energize.R;

public class AboutDialog extends DialogFragment {

	private final static String TAG = "AboutDialog";

	private String getApplicationVersion() {
		try {
			final PackageInfo packageInfo = this.getActivity().getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0);
			return packageInfo.versionName;
		} catch (final NameNotFoundException e) {
			return "";
		}
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.pref_about_dialog_title, getApplicationVersion()))
				.setNegativeButton(R.string.pref_about_dialog_button_negative,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}
				);

		View inflatedView = getActivity().getLayoutInflater().inflate(R.layout.fragment_about_dialog, null);
		alertDialogBuilder.setView(inflatedView);

		return alertDialogBuilder.create();
	}
}