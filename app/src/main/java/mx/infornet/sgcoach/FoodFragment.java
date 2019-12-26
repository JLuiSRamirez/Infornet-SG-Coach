package mx.infornet.sgcoach;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodFragment extends Fragment {

    private View myView;
    private RecyclerView recyclerView;
    private List<Alimentacion> alimentacionList;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type, pag;
    private FloatingActionButton btn_add_;
    private ProgressBar progressBar;
    private TextView anterior, pagina_actual, tv_pagina, siguiente, info_alimentacion;
    private int actual, todas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_food, container, false);

        recyclerView = myView.findViewById(R.id.recycler_view_alimentacion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btn_add_ = myView.findViewById(R.id.btn_planes_alimentacion_add);

        progressBar = myView.findViewById(R.id.progressbar_alim);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        info_alimentacion = myView.findViewById(R.id.info_alimentacion);
        info_alimentacion.setVisibility(View.GONE);

        tv_pagina = myView.findViewById(R.id.tv_pagina);
        tv_pagina.setVisibility(View.GONE);

        pagina_actual = myView.findViewById(R.id.pagina_actual);
        pagina_actual.setVisibility(View.GONE);

        anterior = myView.findViewById(R.id.anterior);
        anterior.setVisibility(View.GONE);

        siguiente = myView.findViewById(R.id.siguiente);
        siguiente.setVisibility(View.GONE);


        getPlanesAlimentacion(1);
        actual = 1;
        pag = Integer.toString(actual);
        tv_pagina.setVisibility(View.VISIBLE);
        pagina_actual.setVisibility(View.VISIBLE);
        pagina_actual.setText(pag);


        //enlaces anterior y siguiente para lo de la paginacion
        anterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "Actual boton siguiente: " + actual, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "Todas boton sigfuiente: " + todas, Toast.LENGTH_SHORT).show();
                if(actual > 1 ){
                    actual = actual - 1;
                    //Toast.makeText(getContext(), "Pagina actual: "+ actual, Toast.LENGTH_SHORT).show();
                    getPlanesAlimentacion(actual);
                    tv_pagina.setVisibility(View.VISIBLE);
                    pagina_actual.setVisibility(View.VISIBLE);
                    pag = Integer.toString(actual);
                    pagina_actual.setText(pag);
                    if ((actual) == 1){ //si ya no habra mas datos ocultamos el enlace al anterior
                        anterior.setVisibility(View.GONE);
                    }
                }

                if ((actual + 1) <= todas){ //si ya no habra mas datos ocultamos el enlace al siguiente
                    siguiente.setVisibility(View.VISIBLE);
                }
                //getRutinasCoach(2);
            }
        });



        siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((actual + 1) == todas){ //si ya no habra mas datos ocultamos el enlace al siguiente
                    siguiente.setVisibility(View.GONE);
                }
                //Toast.makeText(getContext(), "Actual boton siguiente: " + actual, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "Todas boton sigfuiente: " + todas, Toast.LENGTH_SHORT).show();
                if(actual < todas){
                    actual = actual + 1;
                    //Toast.makeText(getContext(), "Pagina actual: "+ actual, Toast.LENGTH_SHORT).show();
                    getPlanesAlimentacion(actual);
                    tv_pagina.setVisibility(View.VISIBLE);
                    pagina_actual.setVisibility(View.VISIBLE);
                    pag = Integer.toString(actual);
                    pagina_actual.setText(pag);
                    if (actual > 1){ //si ya paso la de pagina entonces ponemos el enlace a la pagina anterior
                        anterior.setVisibility(View.VISIBLE);
                    }
                }else if (actual == todas){
                    Toast.makeText(getContext(), "Ya no hay mas rutinas por mostrar", Toast.LENGTH_SHORT).show();
                    siguiente.setVisibility(View.GONE);
                }

            }
        });

        //--- fin enlaces anterior y siguiente de la paginacion


        btn_add_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment editar = new AgregarFoodFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, editar);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        return myView;
    }

    public void getPlanesAlimentacion (int page) {
        progressBar.setVisibility(View.VISIBLE);
        alimentacionList = new ArrayList<>();

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
            Toast.makeText(getActivity(), "Error, no se pudieron cargar las credenciales de identificación" + e.toString(), Toast.LENGTH_SHORT).show();
            ConexionSQLiteHelper  con = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
            SQLiteDatabase dbase = con.getWritableDatabase();
            dbase.execSQL("DELETE FROM coaches");

            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }

        Log.d("token", token);

        db.close();

        queue = Volley.newRequestQueue(getContext());

        request = new StringRequest(Request.Method.GET, Config.GET_ALIMENTACION_URL + "?page=" + page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                info_alimentacion.setVisibility(View.VISIBLE);
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray datos = jsonObject.getJSONArray("data");
                    JSONObject pagination = jsonObject.getJSONObject("pagination");

                    todas = Integer.parseInt( pagination.getString("last_page"));

                    //JSONArray array = new JSONArray(response);

                    if (todas > 1 && (actual + 1 <= todas)){
                        siguiente.setVisibility(View.VISIBLE);
                    }

                    if (jsonObject.has("pagination")) {

                        JSONArray array = datos;

                        Log.d("PLANES_ALIM", array.toString());

                        for (int i=0; i<array.length(); i++){

                            JSONObject comida = array.getJSONObject(i);

                            alimentacionList.add(new Alimentacion(
                                    comida.getInt("id"),
                                    comida.getString("nombre"),
                                    comida.getString("descripcion"),
                                    comida.getString("categoria")
                            ));

                            AdapterAlimentacion adapterAlimentacion = new AdapterAlimentacion(getContext(), alimentacionList);

                            recyclerView.setAdapter(adapterAlimentacion);
                        }

                    } else {
                        Toast.makeText(getContext(), "No hay nada que mostrar", Toast.LENGTH_SHORT).show();
                    }



                } catch (JSONException e){
                    Log.e("ERRORJSON", e.toString());
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.has("status")){
                        String status = jsonObject.getString("status");

                        if (status.equals("Token is Expired")){
                            Toast.makeText(getContext(), status+". Favor de iniciar sesión nuevamente", Toast.LENGTH_LONG).show();

                            ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
                            SQLiteDatabase db = conn.getWritableDatabase();
                            db.execSQL("DELETE FROM coaches");

                            startActivity(new Intent(getContext(), LoginActivity.class));
                            getActivity().finish();
                        }

                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR_RES", error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
                headers.put("Authorization", token_type + " " + token);
                return headers;
            }
        };

        queue.add(request);


    }
}
