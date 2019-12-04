package mx.infornet.sgcoach;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EditarPerfilFragment extends Fragment {

    private View myView;

    private ProgressBar progressBar, progressBarPass;

    private String nombre, biografia, correo, horario, id_coach, token, token_type;
    private TextInputEditText tv_nombre, tv_biografia, tv_correo, current, nueva, nuevaConfirm;
    private Button btn_actualizar, btn_edit_pass;

    private RequestQueue queue;
    private StringRequest request, request_logout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_editar_perfil, container, false);

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getActivity(), "coaches", null, 3);
        SQLiteDatabase db = conn.getWritableDatabase();

        btn_actualizar = myView.findViewById(R.id.btn_actualizar_perfil);
        btn_edit_pass = myView.findViewById(R.id.btn_edit_pass);

        progressBar = myView.findViewById(R.id.prog_bar_edit_perfil);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        progressBarPass = myView.findViewById(R.id.prog_bar_edit_pass);
        progressBarPass.setIndeterminate(true);
        progressBarPass.setVisibility(View.GONE);

        //cargamos los datos actuales para mostrarlos
        try {
            String query = "SELECT * FROM coaches";

            Cursor cursor = db.rawQuery(query, null);

            for(cursor.moveToFirst(); !cursor.isAfterLast();cursor.moveToNext()) {
                id_coach = cursor.getString(cursor.getColumnIndex("idCoach"));
                nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                biografia = cursor.getString(cursor.getColumnIndex("biografia"));
                correo = cursor.getString(cursor.getColumnIndex("email"));
                horario = cursor.getString(cursor.getColumnIndex("horarios"));
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("token_type"));
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        tv_nombre = myView.findViewById(R.id.edit_nombre);
        tv_biografia = myView.findViewById(R.id.edit_biografia);
        tv_correo = myView.findViewById(R.id.edit_correo);

        current = myView.findViewById(R.id.current_password);
        nueva = myView.findViewById(R.id.new_password);
        nuevaConfirm = myView.findViewById(R.id.confirm_password);

        tv_nombre.setText(nombre);
        tv_biografia.setText(biografia);
        tv_correo.setText(correo);

        db.close();

        btn_actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Actualizamos los datos
                try {

                    final String nuevoNombre= tv_nombre.getText().toString();
                    final String nuevoBiografia = tv_biografia.getText().toString();
                    final String nuevoCorreo = tv_correo.getText().toString();

                    queue = Volley.newRequestQueue(getContext());

                    //validamos que los campos no esten vacios
                    if(TextUtils.isEmpty(nuevoNombre)){
                        tv_nombre.setError("Ingresa un nombre");
                        tv_nombre.requestFocus();
                    } else if (TextUtils.isEmpty(nuevoBiografia)) {
                        tv_biografia.setError("Ingresa información en tu biografia");
                        tv_biografia.requestFocus();
                    } else if (TextUtils.isEmpty(nuevoCorreo) || !validarEmail(nuevoCorreo)) {
                        tv_correo.setError("Ingresa un correo valido");
                        tv_correo.requestFocus();
                    } else {

                        progressBar.setVisibility(View.VISIBLE);

                        request = new StringRequest(Request.Method.POST, Config.EDIT_PERFIL_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.has("status")) {
                                        String mensaje = jsonObject.getString("status");
                                        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                                    } else if (jsonObject.has("message")) {
                                        ConexionSQLiteHelper con = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
                                        SQLiteDatabase dbup = con.getWritableDatabase();

                                        ContentValues values = new ContentValues();

                                        values.put("nombre", nuevoNombre);
                                        values.put("biografia", nuevoBiografia);
                                        values.put("email", nuevoCorreo);

                                        dbup.update("coaches", values, "idCoach=" + id_coach, null);
                                        dbup.close();

                                        String mensajeOK = jsonObject.getString("message");
                                        Toast.makeText(getContext(), mensajeOK, Toast.LENGTH_LONG).show();

                                        //Despues de actualizar los datos volvemos a cargar el fragmen del perfil
                                        Fragment profile = new PerfilFragment();
                                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                        transaction.replace(R.id.fragment_container, profile);
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
                                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                                NetworkResponse networkResponse = error.networkResponse;

                                if(networkResponse != null && networkResponse.data != null) {
                                    String jsonError = new String(networkResponse.data);
                                    try {
                                        JSONObject jsonObjectError = new JSONObject(jsonError);

                                        if (jsonObjectError.has("message")){
                                            String message = jsonObjectError.getString("message");

                                            if (message.equals("The given data was invalid.")){
                                                JSONObject errors = jsonObjectError.getJSONObject("errors");

                                                if (errors.has("nombre")){
                                                    String errorNombre = errors.getString("nombre");
                                                    tv_nombre.setError(errorNombre);
                                                    tv_nombre.requestFocus();
                                                } else if (errors.has("biografia")){
                                                    String errorBiografia = errors.getString("boigrafia");
                                                    tv_biografia.setError(errorBiografia);
                                                    tv_biografia.requestFocus();
                                                } else if (errors.has("email")){
                                                    String errorEmail = errors.getString("email");
                                                    tv_correo.setError(errorEmail);
                                                    tv_correo.requestFocus();
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        String err = e.toString();
                                        Toast.makeText(getContext(), "Error: " + err, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("nombre", nuevoNombre);
                                params.put("biografia", nuevoBiografia);
                                params.put("email", nuevoCorreo);

                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap();
                                headers.put("Authorization", token_type+" "+token);
                                return headers;
                            }
                        };
                        queue.add(request);

                    }

                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });


        //editar password
        btn_edit_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queue = Volley.newRequestQueue(getContext());

                btn_edit_pass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditarPassword();
                    }
                });

            }
        });



        return myView;
    }


    private void EditarPassword() {

        final String contra_actual = current.getText().toString();
        final String contra_nueva = nueva.getText().toString();
        final String contra_nueva_confirm = nuevaConfirm.getText().toString();


        if (TextUtils.isEmpty(contra_actual) || contra_actual.length()<8 || contra_actual.length()>10) {
            current.setError("Ingresa una contraseña entre 8 y 10 caracteres", null);
            current.requestFocus();
        } else if (TextUtils.isEmpty(contra_nueva) || contra_nueva.length()<8 || contra_nueva.length()>10) {
            nueva.setError("Ingresa una contraseña entre 8 y 10 caracteres");
            nueva.requestFocus();
        } else if (TextUtils.isEmpty(contra_nueva_confirm) || contra_nueva_confirm.length()<8 || contra_nueva_confirm.length()>10) {
            nuevaConfirm.setError("La contraseña debe tener entre 8 y 10 caracteres");
            nuevaConfirm.requestFocus();
        } else {
            progressBarPass.setVisibility(View.VISIBLE);

            request = new StringRequest(Request.Method.POST, Config.EDIT_PASSWORD_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressBarPass.setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.has("status")) {
                            String status = jsonObject.getString("status");
                            if (status.equals("Token is Expired")) {
                                Toast.makeText(getContext(), "La autorizacion expiró, inicia sesion nuevamente", Toast.LENGTH_LONG).show();
                                //Cerrar sesion y eliminar los datos de la BD local

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

                                ConexionSQLiteHelper  connl = new ConexionSQLiteHelper(getActivity(), "coaches", null, 3);
                                SQLiteDatabase dbl = connl.getWritableDatabase();
                                dbl.execSQL("DELETE FROM coaches");
                                startActivity(new Intent(getContext(), LoginActivity.class));
                                getActivity().finish();


                            }
                        } else if (jsonObject.has("message")) {
                            String messageok = jsonObject.getString("message");
                            Toast.makeText(getContext(), messageok, Toast.LENGTH_LONG).show();

                            //Despues de actualizar la contraseña volvemos a cargar el fragmen del perfil
                            Fragment profile = new PerfilFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, profile);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        String err = e.toString();
                        Toast.makeText(getContext(), "Error " + err, Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBarPass.setVisibility(View.GONE);

                    NetworkResponse networkResponse = error.networkResponse;

                    if(networkResponse != null && networkResponse.data != null){
                        String jsonError = new String(networkResponse.data);
                        try {
                            JSONObject jsonObjectError = new JSONObject(jsonError);

                            if (jsonObjectError.has("message")){
                                String message = jsonObjectError.getString("message");
                                if (message.equals("The given data was invalid.")){
                                    JSONObject errors = jsonObjectError.getJSONObject("errors");
                                    if (errors.has("current_password")){
                                        String sb_curr_pas = errors.getString("current_password");
                                        current.setError(sb_curr_pas, null);
                                        current.requestFocus();
                                    } else if (errors.has("password")){
                                        JSONArray err_pas = errors.getJSONArray("password");

                                        StringBuilder sb_pas = new StringBuilder();
                                        for (int i=0; i<err_pas.length(); i++){
                                            String valor = err_pas.getString(i);
                                            sb_pas.append(valor+"\n");
                                        }

                                        nueva.setError(sb_pas, null);
                                        nueva.requestFocus();

                                    } else if (errors.has("password_confirmation")){

                                        JSONArray err_pas_con = errors.getJSONArray("password_confirmation");

                                        StringBuilder sb_pas_con = new StringBuilder();

                                        for (int i=0; i<err_pas_con.length(); i++){
                                            String valor = err_pas_con.getString(i);
                                            sb_pas_con.append(valor+"\n");
                                        }

                                        nuevaConfirm.setError(sb_pas_con, null);
                                        nuevaConfirm.requestFocus();
                                    }
                                }
                            }

                        }catch (JSONException e){
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("current_password", contra_actual);
                    params.put("password", contra_nueva);
                    params.put("password_confirmation", contra_nueva_confirm);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", token_type+" "+token);
                    return headers;
                }
            };
            queue.add(request);
        }
    }

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return  pattern.matcher(email).matches();
    }
}
