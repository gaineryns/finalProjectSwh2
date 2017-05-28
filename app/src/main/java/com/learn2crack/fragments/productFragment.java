package com.learn2crack.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.learn2crack.R;
import com.learn2crack.ReadActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class productFragment extends Fragment {

    private TextView mtIntitule, mtDesc , mtRef, mtEmpl, mtQte;

    public productFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Bundle bundle = this.getArguments();
        String myValue = bundle.getString("ref_product");
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    private void initViews(View v) {

        mtIntitule = (TextView) v.findViewById(R.id.tIntitule);
        mtDesc = (TextView) v.findViewById(R.id.tDesc);
        mtRef = (TextView) v.findViewById(R.id.tRef);
        mtEmpl = (TextView) v.findViewById(R.id.tEmpl);
        mtQte = (TextView) v.findViewById(R.id.tQte);

    }




    public void loadProductData(String reference){
        String json_url = "https://randomuser.me/api/?results=1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, json_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("results");

                            for(int i = 0; i < array.length(); i++){

                                JSONObject o = array.getJSONObject(i);

                                mtIntitule.setText(o.getJSONObject("name").getString("first"));
                                mtDesc.setText(o.getJSONObject("name").getString("last"));
                                mtRef.setText(o.getJSONObject("name").getString("first"));
                                mtEmpl.setText(o.getJSONObject("name").getString("last"));
                                mtQte.setText(o.getJSONObject("name").getString("first"));


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

}
