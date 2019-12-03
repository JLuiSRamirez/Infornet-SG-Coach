package mx.infornet.sgcoach;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EditarPerfilFragment extends Fragment {

    private View myView;

    private ProgressBar progressBar;

    private String nombre, biografia, correo, horario, id_coach, token, token_type;
    private TextInputEditText tv_nombre, tv_biografia, tv_horario, tv_correo;
    private Button btn_actualizar;

    private RequestQueue queue;
    private StringRequest request;

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

        progressBar = myView.findViewById(R.id.prog_bar_edit_perfil);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

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
        tv_horario = myView.findViewById(R.id.edit_horario);

        tv_nombre.setText(nombre);
        tv_biografia.setText(biografia);
        tv_horario.setText(horario);
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
                    final String nuevoHorario = tv_horario.getText().toString();

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
                    } else if (TextUtils.isEmpty(nuevoHorario)) {
                        tv_horario.setError("Ingresa un horario de entrada y salida");
                        tv_horario.requestFocus();
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
                                        values.put("horarios", nuevoHorario);

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
                                                } else if (errors.has("horarios")){
                                                    String errorHorarios = errors.getString("horarios");
                                                    tv_horario.setError(errorHorarios);
                                                    tv_horario.requestFocus();
                                                } else if (errors.has("email")){
                                                    String errorEmail = errors.getString("email");
                                                    tv_correo.setError(errorEmail);
                                                    tv_correo.requestFocus();
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
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
                                params.put("horarios", nuevoHorario);

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



        return myView;
    }

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return  pattern.matcher(email).matches();
    }
}
