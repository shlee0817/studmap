package de.whs.studmap.client.core.snippets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class UserInfo{

	/**
	 * Blendet eine Dialog-/Messagebox ein.
	 * @param context Context in dem die Benachrichtigung angezeigt werden soll
	 * @param userName Benutzernamen zur persönlichen Ansprache
	 * @param message Nachricht, die dem Benutzer angezeigt werden soll
	 */
	public static void dialog(Context context, String userName, String message){
		dialog(context, userName, message, "OK");
	}
	
	/**
	 * Blendet eine Dialog-/Messagebox ein.
	 * @param context Context in dem die Benachrichtigung angezeigt werden soll
	 * @param userName Benutzernamen zur persönlichen Ansprache
	 * @param message Nachricht, die dem Benutzer angezeigt werden soll
	 */
	public static void dialog(Context context, String userName, String message, String positiveButton) {
		AlertDialog ad = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(message);
		ad.setTitle(userName);
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
	public static void toast(Context context, String message, boolean length_short){
		int length = length_short ? 0 : 1;
		Toast.makeText(context, message, length).show();		
	}	
	
}
