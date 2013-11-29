package de.whs.fia.studmap.collector.fragments;

import de.whs.studmap.client.core.web.JavaScriptInterface;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavaScriptInterfaceImpl implements JavaScriptInterface {
    private Context context;

    public JavaScriptInterfaceImpl(Context context) {
        this.context = context;
    }
   
    @JavascriptInterface
    public void punkt(String nodeId){
    	Toast.makeText(context, "Punkt berührt: " + nodeId, 1).show();
//    	Intent intent = new Intent(context,PositionActivity.class);
//    	intent.putExtra(PositionActivity.EXTRA_NODEID, nodeId);
//		context.startActivity(intent);
   }
    
   @JavascriptInterface
   public void onFinish(){
	   Log.e("", "JavaScriptInterface onFinish");
//	   if(MainActivity.mSelectedNode != null){
//		   MainActivity.mJScriptService.sendTarget(MainActivity.mSelectedNode.getNodeID());
//		   MainActivity.mSelectedNode = null;		   
//	   }
   }
}
