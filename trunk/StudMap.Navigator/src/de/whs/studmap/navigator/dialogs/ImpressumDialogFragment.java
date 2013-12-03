package de.whs.studmap.navigator.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import de.whs.studmap.navigator.R;

public class ImpressumDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.fragment_impressum_dialog, null);
		builder.setView(rootView);
		
		WebView impressumWebView = (WebView) rootView.findViewById(R.id.impressum_web_view);
		impressumWebView.loadUrl("file:///android_asset/impressum/impressum.html");
		
		return builder.create();
	}
}
