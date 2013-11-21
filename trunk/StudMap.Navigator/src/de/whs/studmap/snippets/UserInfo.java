package de.whs.studmap.snippets;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import de.whs.studmap.navigator.MainActivity;
import de.whs.studmap.navigator.R;

public class UserInfo{

	/**
	 * Blendet eine Dialog-/Messagebox ein.
	 * @param context Context in dem die Benachrichtigung angezeigt werden soll
	 * @param userName Benutzernamen zur persönlichen Ansprache
	 * @param message Nachricht, die dem Benutzer angezeigt werden soll
	 */
	public static void dialog(Context context,String userName, String message){
	AlertDialog ad = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).create();  
	ad.setCancelable(false); // This blocks the 'BACK' button  
	ad.setMessage(message);
	ad.setTitle(userName);
	ad.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.positiveButton),
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
	
	public static void positionDialog(final Context context, final int nodeId){
		Builder builder = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);  
		builder.setCancelable(true); // This blocks the 'BACK' button 
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View view = inflater.inflate(R.layout.set_position_dialog, null);
	    builder.setView(view);
	    builder.setNegativeButton(context.getString(R.string.negativeButton), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   dialog.dismiss();
	               }
	           });      
		List<String> items = new ArrayList<String>();
		items.add(context.getString(R.string.navigationStart));
		items.add(context.getString(R.string.navigationDestination));
		
		ListView mListView = (ListView) view.findViewById(R.id.positionList);
		ArrayAdapter<String> mListAdapter = new ArrayAdapter<String>(context,
				R.layout.simple_list_item_white, items);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					MainActivity.mJScriptService.sendStart(nodeId);
					break;

				case 1:
					MainActivity.mJScriptService.sendDestination(nodeId);
					break;
				}
			}			
		});		
		builder.create().show();
	}
}
