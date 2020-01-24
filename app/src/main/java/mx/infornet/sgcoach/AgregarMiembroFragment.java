package mx.infornet.sgcoach;


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
import java.util.regex.Pattern;

public class AgregarMiembroFragment extends Fragment {

    private View myView;
    private ProgressBar progressBar;
    private String token, token_type;
    private TextInputEditText nombre_miembro, apellidos_miembro, sexo_miembro, objetivo_miembro, fecha_miembro, telefono_miembro, correo_miembro, meses_miembro, monto_miembro;
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

        myView = inflater.inflate(R.layout.fragment_agregar_miembro, container, false);

        guardar = myView.findViewById(R.id.btn_registrar_miembro);

        tv_regresar = myView.findViewById(R.id.miembro_regresar);

        nombre_miembro = myView.findViewById(R.id.nombre_miembro);
        apellidos_miembro = myView.findViewById(R.id.apellidos_miembro);
        sexo_miembro = myView.findViewById(R.id.sexo_miembro);
        objetivo_miembro = myView.findViewById(R.id.objetivo_miembro);
        fecha_miembro = myView.findViewById(R.id.fecha_miembro);
        telefono_miembro = myView.findViewById(R.id.telefono_miembro);
        correo_miembro = myView.findViewById(R.id.correo_miembro);
        meses_miembro = myView.findViewById(R.id.meses_miembro);
        monto_miembro = myView.findViewById(R.id.monto_miembro);

        guardar = myView.findViewById(R.id.btn_registrar_miembro);

        progressBar = myView.findViewById(R.id.progressbar_registrar_miembro);
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
                    Fragment miembros = new MembersFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, miembros);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });


            guardar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {

                        final String nombre_m = nombre_miembro.getText().toString();
                        final String apellidos_m = apellidos_miembro.getText().toString();
                        final String sexo_m = sexo_miembro.getText().toString();
                        final String objetivo_m = objetivo_miembro.getText().toString();
                        final String fecha_m = fecha_miembro.getText().toString();
                        final String telefono_m = telefono_miembro.getText().toString();
                        final String correo_m = correo_miembro.getText().toString();
                        final String meses_m = meses_miembro.getText().toString();
                        final String monto_m = monto_miembro.getText().toString();

                        queue = Volley.newRequestQueue(getContext());

                        //validamos que los campos no esten vacios
                        if (TextUtils.isEmpty(nombre_m)) {
                            nombre_miembro.setError("Ingresa un nombre");
                            nombre_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(apellidos_m)) {
                            apellidos_miembro.setError("Ingresa los apellidos");
                            apellidos_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(sexo_m)) {
                            sexo_miembro.setError("Ingresa si es hombre o mujer");
                            sexo_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(objetivo_m)) {
                            objetivo_miembro.setError("Ingresa el objetivo del miembro");
                            objetivo_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(fecha_m)) {
                            fecha_miembro.setError("Ingresa la fecha de nacimiento del nuevo miembro");
                            fecha_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(telefono_m)) {
                            telefono_miembro.setError("Ingresa el telefono del miembro");
                            telefono_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(correo_m) || !validarEmail(correo_m)) {
                            correo_miembro.setError("Ingresa un correo valido");
                            correo_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(meses_m)) {
                            meses_miembro.setError("Ingresa los meses del plan");
                            meses_miembro.requestFocus();
                        } else if (TextUtils.isEmpty(monto_m)) {
                            monto_miembro.setError("Ingresa el pago total");
                            monto_miembro.requestFocus();
                        } else {

                            progressBar.setVisibility(View.VISIBLE);

                            request = new StringRequest(Request.Method.POST, Config.ADD_MIEMBROS_URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    progressBar.setVisibility(View.GONE);
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.has("status")) {
                                            String mensaje = jsonObject.getString("status");
                                            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                                        } else if (jsonObject.has("message")) {

                                            Toast.makeText(getContext(), "Se ha registrado al miembro correctamente", Toast.LENGTH_SHORT).show();

                                            //Despues de actualizar los datos volvemos a cargar el fragmen del perfil
                                            Fragment miembros = new MembersFragment();
                                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                            transaction.replace(R.id.fragment_container, miembros);
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

                                        try {
                                                NetworkResponse networkResponse = error.networkResponse;
                                           // if(networkResponse != null && networkResponse.data != null) {
                                                String jsonError = new String(networkResponse.data);
                                                JSONObject jsonObjectError = new JSONObject(jsonError);
                                                String message = jsonObjectError.getString("message");

                                                if (message.equals("The given data was invalid.")) {
                                                    JSONObject errors = jsonObjectError.getJSONObject("errors");
                                                    if (errors.has("nombre")) {
                                                        String errorNombre = errors.getString("nombre");
                                                        nombre_miembro.setError(errorNombre);
                                                        nombre_miembro.requestFocus();
                                                    } else if (errors.has("apellidos")) {
                                                        String errorApellidos = errors.getString("apellidos");
                                                        apellidos_miembro.setError(errorApellidos);
                                                        apellidos_miembro.requestFocus();
                                                    } else if (errors.has("sexo")) {
                                                        String errorSexo = errors.getString("sexo");
                                                        sexo_miembro.setError(errorSexo);
                                                        sexo_miembro.requestFocus();
                                                    } else if (errors.has("objetivo")) {
                                                        String errorObjetivo = errors.getString("objetivo");
                                                        objetivo_miembro.setError(errorObjetivo);
                                                        objetivo_miembro.requestFocus();
                                                    } else if (errors.has("fecha_nacimiento")) {
                                                        String errorFecha = errors.getString("fecha_nacimiento");
                                                        fecha_miembro.setError(errorFecha);
                                                        fecha_miembro.requestFocus();
                                                    } else if (errors.has("telfono")) {
                                                        String errorTelefono = errors.getString("telefono");
                                                        telefono_miembro.setError(errorTelefono);
                                                        telefono_miembro.requestFocus();
                                                    } else if (errors.has("email")) {
                                                        String errorEmail = errors.getString("email");
                                                        correo_miembro.setError(errorEmail);
                                                        correo_miembro.requestFocus();
                                                    } else if (errors.has("meses")) {
                                                        String errorMeses = errors.getString("meses");
                                                        meses_miembro.setError(errorMeses);
                                                        meses_miembro.requestFocus();
                                                    } else if (errors.has("monto")) {
                                                        String errorMonto = errors.getString("monto");
                                                        monto_miembro.setError(errorMonto);
                                                        monto_miembro.requestFocus();
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
                                         //   }

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
                                    params.put("nombre", nombre_m);
                                    params.put("apellidos", apellidos_m);
                                    params.put("sexo", sexo_m);
                                    params.put("objetivo", objetivo_m);
                                    params.put("fecha_nacimiento", fecha_m);
                                    params.put("telefono", telefono_m);
                                    params.put("email", correo_m);
                                    params.put("meses", meses_m);
                                    params.put("monto", monto_m);

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

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return  pattern.matcher(email).matches();
    }
}
