package de.whs.studmap.web;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import de.whs.studmap.client.core.web.JavaScriptInterface;
import de.whs.studmap.navigator.MainActivity;
import de.whs.studmap.navigator.PositionActivity;

public class JavaScriptInterfaceImpl implements JavaScriptInterface {
    private Context context;

    public JavaScriptInterfaceImpl(Context context) {
        this.context = context;
    }
   
    @JavascriptInterface
    public void punkt(String nodeId){
    	Intent intent = new Intent(context,PositionActivity.class);
    	intent.putExtra(PositionActivity.EXTRA_NODEID, nodeId);
		context.startActivity(intent);
   }
    
   @JavascriptInterface
   public void onFinish(){
	   if(MainActivity.mSelectedNode != null){
		   MainActivity.mJScriptService.sendTarget(MainActivity.mSelectedNode.getNodeID());
		   MainActivity.mSelectedNode = null;		   
	   }
   }
}
