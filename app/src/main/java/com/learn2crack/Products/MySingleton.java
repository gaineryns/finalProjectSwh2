package com.learn2crack.Products;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by gaineryns on 28/05/2017.
 */

public class MySingleton {
    private static MySingleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    private MySingleton(Context context){
        mCtx=context;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {

        if(requestQueue ==null){
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized MySingleton getInstance(Context context){
        if(mInstance == null){
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }

    public<T> void  addtoRequest(Request<T> request ){
       requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
    }
}
