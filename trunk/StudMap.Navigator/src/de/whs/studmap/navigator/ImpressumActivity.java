package de.whs.studmap.navigator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class ImpressumActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_impressum);
		
		WebView impressumWebView = (WebView) findViewById(R.id.impressum_web_view);
		impressumWebView.loadUrl("file:///android_asset/impressum/impressum.html");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}

}
