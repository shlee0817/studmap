package de.whs.studmap.web;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import de.whs.studmap.navigator.PositionActivity;

public class JavaScriptInterface {
    private Context context;

    public JavaScriptInterface(Context context) {
        this.context = context;
    }
   
    @JavascriptInterface
    public void punkt(int nodeId){
    	Intent intent = new Intent(context,PositionActivity.class);
    	intent.putExtra(PositionActivity.EXTRA_NODEID, nodeId);
		context.startActivity(intent);
   }

}
