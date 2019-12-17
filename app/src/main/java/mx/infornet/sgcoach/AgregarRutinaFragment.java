package mx.infornet.sgcoach;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AgregarRutinaFragment extends Fragment {

    private View myView;

    private ProgressBar progressBar;

    private String token, token_type;
    private TextInputEditText tv_nombre, tv_descripcion;
    private TextView tv_regresar;
    private FloatingActionButton guardar;

    private RequestQueue queue;
    private StringRequest request, request_logout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_add_routines, container, false);

        guardar = myView.findViewById(R.id.btn_guardar_rutina);

        tv_nombre = myView.findViewById(R.id.nombre_rutina);
        tv_descripcion = myView.findViewById(R.id.descripcion_rutina);

        tv_regresar = myView.findViewById(R.id.tv_regresar_add_rutinas);

        guardar = myView.findViewById(R.id.btn_guardar_rutina);

        progressBar = myView.findViewById(R.id.progressbar_add_rutinas);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getActivity(), "coaches", null, 3);
        SQLiteDatabase db = conn.getWritableDatabase();

        try {
            String query = "SELECT * FROM coaches";

            Cursor cursor = db.rawQuery(query, null);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("token_type"));
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        try {

            tv_regresar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment rutinas = new HomeFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, rutinas);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            guardar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        final String nombre = tv_nombre.getText().toString();
                        final String descripcion = tv_descripcion.getText().toString();

                        queue = Volley.newRequestQueue(getContext());

                        //validamos que los campos no esten vacios
                        if (TextUtils.isEmpty(nombre)) {
                            tv_nombre.setError("Ingresa un nombre");
                            tv_nombre.requestFocus();
                        } else if (TextUtils.isEmpty(descripcion)) {
                            tv_descripcion.setError("Ingresa información en tu biografia");
                            tv_descripcion.requestFocus();
                        } else {

                            progressBar.setVisibility(View.VISIBLE);

                            request = new StringRequest(Request.Method.POST, Config.ADD_RUTINAS_URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    progressBar.setVisibility(View.GONE);
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.has("status")) {
                                            String mensaje = jsonObject.getString("status");
                                            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                                        } else if (jsonObject.has("message")) {

                                            Toast.makeText(getContext(), "Se ha guardado la rutina", Toast.LENGTH_SHORT).show();

                                            //Despues de actualizar los datos volvemos a cargar el fragmen del perfil
                                            Fragment rutinas = new RoutinesFragment();
                                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                            transaction.replace(R.id.fragment_container, rutinas);
                                            transaction.addToBackStack(null);
                                            transaction.commit();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        String err = e.toString();
                                        Toast.makeText(getContext(), "Error: " + err, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressBar.setVisibility(View.GONE);
                                    //Toast.makeText(getContext(), "Error del response: " + error.toString(), Toast.LENGTH_SHORT).show();
                                    NetworkResponse networkResponse = error.networkResponse;

                                        String jsonError = new String(networkResponse.data);
                                        try {
                                                JSONObject jsonObjectError = new JSONObject(jsonError);
                                                String message = jsonObjectError.getString("message");

                                                if (message.equals("The given data was invalid.")) {
                                                    JSONObject errors = jsonObjectError.getJSONObject("errors");
                                                    if (errors.has("nombre")) {
                                                        String errorNombre = errors.getString("nombre");
                                                        tv_nombre.setError(errorNombre);
                                                        tv_nombre.requestFocus();
                                                    } else if (errors.has("descripcion")) {
                                                        String errorBiografia = errors.getString("biografia");
                                                        tv_descripcion.setError(errorBiografia);
                                                        tv_descripcion.requestFocus();
                                                    }
                                                } else if (message.equals("Token is Expired")) {

                                                    Toast.makeText(getContext(), "Tu sesion expiro, vuelve a ingresar", Toast.LENGTH_SHORT).show();
                                                    queue = Volley.newRequestQueue(getContext());
                                                    request_logout = new StringRequest(Request.Method.POST, Config.LOGOUT_URL, new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            Log.d("res_logout", response);
                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            Log.e("err_res_logout", error.toString());
                                                        }
                                                    });

                                                    queue.add(request_logout);

                                                    ConexionSQLiteHelper connl = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
                                                    SQLiteDatabase dbl = connl.getWritableDatabase();
                                                    dbl.execSQL("DELETE FROM coaches");
                                                    startActivity(new Intent(getContext(), LoginActivity.class));

                                                }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            String err = e.toString();
                                            Toast.makeText(getContext(), "Error: " + err, Toast.LENGTH_SHORT).show();
                                        }

                                } //errorResponse
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("nombre", nombre);
                                    params.put("descripcion", descripcion);

                                    return params;
                                }

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    HashMap<String, String> headers = new HashMap();
                                    headers.put("Authorization", token_type + " " + token);
                                    return headers;
                                }
                            };

                            queue.add(request);

                        } //Si los campos no estan vacios

                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
                    }

                } //onClick
            }); //boton guardar

        }catch (Exception e) {
            Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        return myView;
    } //OncreateView
}
