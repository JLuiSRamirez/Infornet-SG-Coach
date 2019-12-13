package mx.infornet.sgcoach;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AgregarFoodFragment extends Fragment {

    private View myView;
    private TextInputEditText iet_nombre, iet_descripcion, iet_categoria;
    private Button agregar;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_agregar_food, container, false);

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getActivity(), "coaches", null, 3);
        SQLiteDatabase db = conn.getWritableDatabase();

        try {
            String query = "SELECT * FROM coaches";

            Cursor cursor = db.rawQuery(query, null);

            for(cursor.moveToFirst(); !cursor.isAfterLast();cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("token_type"));
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        Log.d("token", token);

        db.close();

        queue = Volley.newRequestQueue(getContext());

        iet_nombre = myView.findViewById(R.id.nombre_alim_add);
        iet_descripcion = myView.findViewById(R.id.descripcion_alim_add);
        iet_categoria = myView.findViewById(R.id.categoria_alim_add);

        agregar = myView.findViewById(R.id.store_alim);

        progressBar = myView.findViewById(R.id.progressbar_add_food);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);


        agregar.setOnClickListener(new View.OnClickListener() {
       // btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                final String nombre = iet_nombre.getText().toString();
                final String descripcion = iet_descripcion.getText().toString();
                final String categoria = iet_categoria.getText().toString();

                if (TextUtils.isEmpty(nombre)){
                    iet_nombre.setError("Ingresa un nombre válido");
                    iet_nombre.requestFocus();
                } else if(TextUtils.isEmpty(descripcion)){
                    iet_descripcion.setError("Ingresa una descripción");
                    iet_descripcion.requestFocus();
                } else if (TextUtils.isEmpty(categoria)){
                    iet_categoria.setError("Ingresa alguna categoria");
                    iet_categoria.requestFocus();
                } else{

                    request = new StringRequest(Request.Method.POST, Config.ADD_ALIM_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                Log.d("RES_ADD_ALM", jsonObject.toString());

                                if (jsonObject.has("message")){

                                    String mensaje = jsonObject.getString("message");

                                    if (mensaje.equals("Plan de alimentacion agregado")) {

                                        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();

                                        Fragment food = new FoodFragment();
                                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                        transaction.replace(R.id.fragment_container, food);
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    }
                                }
                            }catch (JSONException e){
                                Log.e("ERR_JSON", e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.GONE);

                            NetworkResponse networkResponse = error.networkResponse;

                            if(networkResponse != null && networkResponse.data != null){
                                String jsonError = new String(networkResponse.data);

                                try {
                                    JSONObject jsonObjectError = new JSONObject(jsonError);
                                    Log.e("error_logn", jsonObjectError.toString());

                                    if (jsonObjectError.has("message")){
                                        String message = jsonObjectError.getString("message");


                                        if (message.equals("The given data was invalid.")){

                                            JSONObject errors = jsonObjectError.getJSONObject("errors");

                                            if (errors.has("nombre")){

                                                JSONArray err_pas = errors.getJSONArray("nombre");

                                                StringBuilder sb_nom = new StringBuilder();

                                                for (int i=0; i<err_pas.length(); i++){
                                                    String valor = err_pas.getString(i);
                                                    sb_nom.append(valor+"\n");
                                                }

                                                new AlertDialog.Builder(getContext())
                                                        .setTitle("Error!")
                                                        .setMessage(sb_nom)
                                                        .setPositiveButton("Ok", null)
                                                        .show();
                                                iet_nombre.requestFocus();
                                            }

                                        }
                                    }

                                } catch (JSONException e){
                                    Log.e("ERR_RES", e.toString());
                                }
                            }

                        }
                    }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap();
                            headers.put("Authorization", token_type + " " + token);
                            return headers;
                        }

                        @Override
                        protected Map<String, String> getParams() {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("nombre", nombre);
                            hashMap.put("descripcion", descripcion);
                            hashMap.put("categoria", categoria);
                            return hashMap;
                        }
                    };

                    queue.add(request);
                }
            }
        });


        return myView;
    }

}
