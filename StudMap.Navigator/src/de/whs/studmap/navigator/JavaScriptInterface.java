package de.whs.studmap.navigator;

import de.whs.studmap.snippets.UserInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    private Context context;

    public JavaScriptInterface(Context context) {
        this.context = context;
    }
   
    @JavascriptInterface
    public void punkt(String nodeId){
   	 UserInfo.positionDialog(context, Integer.parseInt(nodeId));
   }
    
    @JavascriptInterface
    public void test(){
    	
    }
}
