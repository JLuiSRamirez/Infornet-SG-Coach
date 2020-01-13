package mx.infornet.sgcoach;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class RoutinesFragment extends Fragment {
    private View myView;
    private ProgressBar progressBar;
    private Button btn_agregar;
    private TextView tv_titulo, tv_select_option, info_rutinas, anterior, siguiente, pagina_actual, tv_pagina;
    private RecyclerView recyclerView;
    private List<Rutinas> rutinasList;
    private List<Planes> planesList;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type, pag;
    private int actual, todas;
    private FloatingActionButton btn_rutinas_coach, btn_rutinas_gym, btn_planes_gym;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //------------- MOSTRAR RUTINAS DEL COAHC, GYM, PLANES DEL GYM -----------------------

        //return inflater.inflate(R.layout.fragment_routines, container, false);

        myView = inflater.inflate(R.layout.fragment_routines, container, false);

        recyclerView = myView.findViewById(R.id.recycler_view_rutinas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = myView.findViewById(R.id.progressbar_rutinas);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);


        info_rutinas = myView.findViewById(R.id.info_rutinas);
        info_rutinas.setVisibility(View.GONE);

        tv_pagina = myView.findViewById(R.id.tv_pagina);
        tv_pagina.setVisibility(View.GONE);

        pagina_actual = myView.findViewById(R.id.pagina_actual);
        pagina_actual.setVisibility(View.GONE);

        anterior = myView.findViewById(R.id.anterior);
        anterior.setVisibility(View.GONE);

        siguiente = myView.findViewById(R.id.siguiente);
        siguiente.setVisibility(View.GONE);

        tv_select_option = myView.findViewById(R.id.textView_selec_option);
        tv_select_option.setVisibility(View.VISIBLE);

        tv_titulo = myView.findViewById(R.id.tv_rutinas);
        tv_titulo.setVisibility(View.GONE);

        btn_rutinas_coach = myView.findViewById(R.id.btn_rutinas_coach);
        btn_rutinas_gym = myView.findViewById(R.id.btn_rutinas_gym);
        btn_planes_gym = myView.findViewById(R.id.btn_planes_gym);

        btn_agregar = myView.findViewById(R.id.btn_agregar_nueva_rutina);

        progressBar.setVisibility(View.GONE);


        btn_rutinas_coach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //etiquetas de informacion y cambio de pagina
                tv_pagina.setVisibility(View.GONE);
                pagina_actual.setVisibility(View.GONE);
                anterior.setVisibility(View.GONE);
                siguiente.setVisibility(View.GONE);
                info_rutinas.setVisibility(View.VISIBLE);
                tv_select_option.setVisibility(View.GONE);
                AdapterRutinas adapterRutinasgym = new AdapterRutinas(getContext(), null, 1);
                recyclerView.setAdapter(adapterRutinasgym);

                getRutinasCoach();

                Toast.makeText(getContext(), "tostada del boton mis rutinas", Toast.LENGTH_SHORT).show();
                tv_titulo.setVisibility(View.VISIBLE);
                tv_titulo.setText("MIS RUTINAS");
            }
        });


        //al darle click deshabiliamos el boton hasta que elija otra opcion===========================
        btn_rutinas_gym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_pagina.setVisibility(View.GONE);
                pagina_actual.setVisibility(View.GONE);
                anterior.setVisibility(View.GONE);
                siguiente.setVisibility(View.GONE);
                tv_select_option.setVisibility(View.GONE);
                info_rutinas.setVisibility(View.VISIBLE);
                AdapterRutinas adapterRutinasgym = new AdapterRutinas(getContext(), null, 0);
                recyclerView.setAdapter(adapterRutinasgym);

                //Toast.makeText(getContext(), "Tostada del boton rutinas gym", Toast.LENGTH_SHORT).show();
                tv_titulo.setVisibility(View.VISIBLE);
                tv_titulo.setText("TODAS LAS RUTINAS");

                getRutinasGym(1);

                actual = 1;
                pag = Integer.toString(actual);
                tv_pagina.setVisibility(View.VISIBLE);
                pagina_actual.setVisibility(View.VISIBLE);
                pagina_actual.setText(pag);

            }
        });

        //enlaces anterior y siguiente para lo de la paginacion


        anterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "Actual boton siguiente: " + actual, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "Todas boton sigfuiente: " + todas, Toast.LENGTH_SHORT).show();
                if(actual > 1 ){
                    actual = actual - 1;
                    //Toast.makeText(getContext(), "Pagina actual: "+ actual, Toast.LENGTH_SHORT).show();
                    getRutinasGym(actual);
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
                    getRutinasGym(actual);
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

        btn_planes_gym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tv_pagina.setVisibility(View.GONE);
                pagina_actual.setVisibility(View.GONE);
                anterior.setVisibility(View.GONE);
                siguiente.setVisibility(View.GONE);
                info_rutinas.setVisibility(View.VISIBLE);
                tv_select_option.setVisibility(View.GONE);

                AdapterRutinas adapterRutinasgym = new AdapterRutinas(getContext(), null,0);
                recyclerView.setAdapter(adapterRutinasgym);

                getPlanesGym(1);

                tv_select_option.setVisibility(View.GONE);
                info_rutinas.setVisibility(View.VISIBLE);
                ///Toast.makeText(getContext(), "Tostada del boton planes gym", Toast.LENGTH_SHORT).show();
                tv_titulo.setVisibility(View.VISIBLE);
                tv_titulo.setText("PLANES DE ENTRENAMIENTO");
            }
        });

        btn_agregar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tv_select_option.setVisibility(View.GONE);
                Fragment agregar = new AgregarRutinaFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, agregar);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return myView;

    }

    public void getRutinasCoach(){
        progressBar.setVisibility(View.VISIBLE);
        rutinasList = new ArrayList<>();

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
            Toast.makeText(getActivity(), "Error al cargar credenciales de identificacion, Inicia sesion nuevamente " + e.toString(), Toast.LENGTH_LONG).show();
            ConexionSQLiteHelper  con = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
            SQLiteDatabase dbase = con.getWritableDatabase();
            dbase.execSQL("DELETE FROM coaches");

            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }

        Log.d("token", token);

        db.close();

        queue = Volley.newRequestQueue(getContext());

        request = new StringRequest(Request.Method.GET, Config.GET_MIS_RUTINAS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONArray array = new JSONArray(response);

                        Log.d("RUTINAS DEL COACH", array.toString());

                        for (int i=0; i<array.length(); i++) {

                            JSONObject rutina = array.getJSONObject(i);

                            rutinasList.add(new Rutinas(
                                    rutina.getInt("id"),
                                    rutina.getString("nombre"),
                                    rutina.getString("descripcion")
                            ));

                            AdapterRutinas adapterRutinas = new AdapterRutinas(getContext(), rutinasList, 1);

                            recyclerView.setAdapter(adapterRutinas);
                        }


                } catch (JSONException e){
                    Log.e("ERROR_JSON: ", e.toString());
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
                progressBar.setVisibility(View.GONE);
                Log.e("ERROR_RESPONSE: ", error.toString());
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

    public void getRutinasGym(int page){
        progressBar.setVisibility(View.VISIBLE);
        rutinasList = new ArrayList<>();

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
            Toast.makeText(getActivity(), "Error al cargar credenciales de identificacion, Inicia sesion nuevamente " + e.toString(), Toast.LENGTH_LONG).show();
            ConexionSQLiteHelper  con = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
            SQLiteDatabase dbase = con.getWritableDatabase();
            dbase.execSQL("DELETE FROM coaches");

            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }

        Log.d("token", token);

        db.close();

        queue = Volley.newRequestQueue(getContext());

        request = new StringRequest(Request.Method.GET, Config.GET_RUTINAS_URL + "?page="+page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray datos = jsonObject.getJSONArray("data");
                    JSONObject pagination = jsonObject.getJSONObject("pagination");

                    todas = Integer.parseInt( pagination.getString("last_page"));
                    if (todas > 1 && (actual + 1 <= todas)){
                        siguiente.setVisibility(View.VISIBLE);
                    }

                    if (jsonObject.has("pagination")) {

                        JSONArray array = datos;

                        Log.d("RUTINAS DEL COACH", array.toString());

                        for (int i=0; i<array.length(); i++) {

                            JSONObject rutina = array.getJSONObject(i);

                            rutinasList.add(new Rutinas(
                                    rutina.getInt("id"),
                                    rutina.getString("nombre"),
                                    rutina.getString("descripcion")
                            ));

                            AdapterRutinas adapterRutinas = new AdapterRutinas(getContext(), rutinasList, 0);

                            recyclerView.setAdapter(adapterRutinas);
                        }
                    }else {
                        Toast.makeText(getContext(), "No hay nada que mostrar", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e){
                    Log.e("ERROR_JSON: ", e.toString());
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
                progressBar.setVisibility(View.GONE);
                Log.e("ERROR_RESPONSE: ", error.toString());
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

    public void getPlanesGym(int page){
        progressBar.setVisibility(View.VISIBLE);
        planesList = new ArrayList<>();

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
            Toast.makeText(getActivity(), "Error al cargar credenciales de identificacion, Inicia sesion nuevamente " + e.toString(), Toast.LENGTH_LONG).show();
            ConexionSQLiteHelper  con = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
            SQLiteDatabase dbase = con.getWritableDatabase();
            dbase.execSQL("DELETE FROM coaches");

            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }

        Log.d("token", token);

        db.close();

        queue = Volley.newRequestQueue(getContext());

        request = new StringRequest(Request.Method.GET, Config.GET_PLANES_URL + "?page="+page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray datos = jsonObject.getJSONArray("data");
                    JSONObject pagination = jsonObject.getJSONObject("pagination");

                    todas = Integer.parseInt( pagination.getString("last_page"));
                    if (todas > 1 && (actual + 1 <= todas)){
                        siguiente.setVisibility(View.VISIBLE);
                    }

                    if (jsonObject.has("pagination")) {

                        JSONArray array = datos;

                        Log.d("PLANES GYM", array.toString());

                        for (int i=0; i<array.length(); i++) {

                            JSONObject plan = array.getJSONObject(i);
                            JSONArray services = plan.getJSONArray("servicios");

                            planesList.add(new Planes(
                                    plan.getInt("id"),
                                    plan.getString("nombre"),
                                    plan.getDouble("precio"),
                                    plan.getString("servicios"),
                                    services
                            ));

                            AdapterPlanes adapterPlanes = new AdapterPlanes(getContext(), planesList);

                            recyclerView.setAdapter(adapterPlanes);
                        }
                    }else {
                        Toast.makeText(getContext(), "No hay nada que mostrar", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e){
                    Log.e("ERROR_JSON: ", e.toString());
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
                progressBar.setVisibility(View.GONE);
                Log.e("ERROR_RESPONSE: ", error.toString());
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
