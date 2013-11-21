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
    public void punkt(){
   	 UserInfo.positionDialog(context);
   }
    
    @JavascriptInterface
    public void test(){
    	
    }
}
