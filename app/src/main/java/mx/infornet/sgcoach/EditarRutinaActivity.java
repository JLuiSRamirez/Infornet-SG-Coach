package mx.infornet.sgcoach;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class EditarRutinaActivity extends AppCompatActivity {

    private TextInputEditText et_nombre, et_desc;
    private FloatingActionButton editar;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type;
    private ProgressBar progressBar;
    TextView regresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_editar_routines);

        Intent intent = getIntent();

        final int id = intent.getIntExtra("id", 0);
        String nombre = intent.getStringExtra("nombre");
        String descripcion = intent.getStringExtra("descripcion");

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getApplicationContext(), "coaches", null, 3);
        SQLiteDatabase db = conn.getWritableDatabase();

        try {
            String query = "SELECT * FROM coaches";

            Cursor cursor = db.rawQuery(query, null);

            for(cursor.moveToFirst(); !cursor.isAfterLast();cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("token_type"));
            }

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        Log.d("token", token);

        db.close();

        queue = Volley.newRequestQueue(getApplicationContext());

        regresar = findViewById(R.id.tv_regresar_edit_rutinas);

        et_nombre = findViewById(R.id.edit_nombre_rutina);
        et_nombre.setText(nombre);
        et_desc = findViewById(R.id.edit_descripcion_rutina);
        et_desc.setText(descripcion);


        editar = findViewById(R.id.edit_btn_guardar_rutina);

        progressBar = findViewById(R.id.edit_progressbar_add_rutinas);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditarRutinaActivity.this.finish();
            }
        });



        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nom = et_nombre.getText().toString();
                final String desc = et_desc.getText().toString();

                if (TextUtils.isEmpty(nom)){

                    et_nombre.setError("Ingresa un nombre");
                    et_nombre.requestFocus();

                } else if(TextUtils.isEmpty(desc)){

                    et_desc.setError("Ingresa una descripcion");
                    et_desc.requestFocus();

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    request = new StringRequest(Request.Method.PUT, Config.PUT_RUTINAS_URL + id, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Log.d("RES_PUT", jsonObject.toString());

                                if(jsonObject.has("message")){
                                    String mensj = jsonObject.getString("message");

                                    if (mensj.equals("Rutina actualizada")){
                                        Toast.makeText(getApplicationContext(),mensj, Toast.LENGTH_LONG).show();
                                        //cargar el fragmen correspondiente=================>pendiente
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                }
                            } catch (JSONException e){
                                e.printStackTrace();
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
                                    Log.d("ERROR500", jsonObjectError.toString());

                                }catch (JSONException e){
                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("nombre", nom);
                            params.put("descripcion", desc);

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

                }
            }
        });


    }
}
