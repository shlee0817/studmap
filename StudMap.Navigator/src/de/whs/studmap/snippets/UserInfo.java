package de.whs.studmap.snippets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources.Theme;
import android.text.AlteredCharSequence;
import android.widget.Toast;

public class UserInfo{

	public static void dialog(Context context,String userName, String message){
	AlertDialog ad = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).create();  
	ad.setCancelable(false); // This blocks the 'BACK' button  
	ad.setMessage(message);
	ad.setTitle(userName);
	ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {  
	    @Override  
	    public void onClick(DialogInterface dialog, int which) {  
	        dialog.dismiss();                      
	    }  
	});  
	ad.show(); 
	}
	
	public static void toast(Context context, String message, boolean length_short){
		int length = length_short ? 0 : 1;
		Toast.makeText(context, message, length).show();		
	}
}
