package de.whs.studmap.client.core.snippets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class UserInfo{

	/**
	 * Blendet eine Dialog-/Messagebox ein mit dem Button 'OK' ein.
	 * @param context Context in dem die Benachrichtigung angezeigt werden soll
	 * @param title Titel des Dialogs
	 * @param message Nachricht, die dem Benutzer angezeigt werden soll
	 */
	public static void dialog(Context context, String title, String message){
		dialog(context, title, message, "OK");
	}
	
	/**
	 * Blendet eine Dialog-/Messagebox ein.
	 * @param context Context in dem die Benachrichtigung angezeigt werden soll
	 * @param title Titel des Dialogs
	 * @param message Nachricht, die dem Benutzer angezeigt werden soll
	 * @param positiveButton Beschriftung des Buttons unter dem Dialog
	 */
	public static void dialog(Context context, String title, String message, String positiveButton) {
		AlertDialog ad = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(message);
		ad.setTitle(title);
		ad.setButton(DialogInterface.BUTTON_POSITIVE, positiveButton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		ad.show();
	}
	
	/**
	 * Blendet eine Benachrichtigung am unteren Bildschirmrand ein.
	 * @param context Context in dem die Benachrichtigung angezeigt werden soll	
	 * @param message Nachricht, die dem Benutzer angezeigt werden soll
	 * @param length_short Anzeigedauer. true = kurz | false = lang
	 */
	public static void toast(Context context, String title, boolean length_short){
		int length = length_short ? 0 : 1;
		Toast.makeText(context, title, length).show();		
	}	
	
	public static void toastInUiThread(final Activity activity,
			final String title, final int length) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity.getApplicationContext(), title,
						length).show();
			}
		});
	}
	
	public static ProgressDialog StudmapProgressDialog(Context context){
		
		ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setCancelable(false);
		pDialog.setMessage("Bitte warten...");
		
		return pDialog;
	}
	
}
