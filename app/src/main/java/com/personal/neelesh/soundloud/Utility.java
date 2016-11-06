package com.personal.neelesh.soundloud;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by neelesh on 21/9/16.
 */
public class Utility {
    public static void showToast (Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
