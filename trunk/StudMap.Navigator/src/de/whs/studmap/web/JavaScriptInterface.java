package de.whs.studmap.web;

import android.content.Context;
import android.webkit.JavascriptInterface;
import de.whs.studmap.snippets.UserInfo;

public class JavaScriptInterface {
    private Context context;

    public JavaScriptInterface(Context context) {
        this.context = context;
    }
   
    @JavascriptInterface
    public void punkt(int nodeId){
   	 UserInfo.positionDialog(context, nodeId);
   }

}
