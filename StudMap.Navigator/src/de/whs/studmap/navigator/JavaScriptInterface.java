package de.whs.studmap.navigator;

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
   	 AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("punkt!!").show();
   }
}
