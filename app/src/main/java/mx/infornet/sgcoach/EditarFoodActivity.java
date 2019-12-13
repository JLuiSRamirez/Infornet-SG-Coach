package mx.infornet.sgcoach;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditarFoodActivity extends AppCompatActivity {

    private TextInputEditText et_nombre, et_desc, et_categoria;
    private Button editar;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_food);

        Intent intent = getIntent();

        String nombre = intent.getStringExtra("nombre");
        final int id = intent.getIntExtra("id", 0);
        String descripcion = intent.getStringExtra("descripcion");
        String categoria = intent.getStringExtra("categoria");

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

        et_nombre = findViewById(R.id.nombre_alim_edit);
        et_nombre.setText(nombre);
        et_desc = findViewById(R.id.descripcion_alim_edit);
        et_desc.setText(descripcion);
        et_categoria = findViewById(R.id.categoria_alim_edit);
        et_categoria.setText(categoria);

        editar = findViewById(R.id.edit_alim);

        progressBar = findViewById(R.id.progressbar_edit_food);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nom = et_nombre.getText().toString();
                final String desc = et_desc.getText().toString();
                final String cate = et_categoria.getText().toString();

                if (TextUtils.isEmpty(nom)){

                    et_nombre.setError("Ingresa un nombre");
                    et_nombre.requestFocus();

                } else if(TextUtils.isEmpty(desc)){

                    et_desc.setError("Ingresa una descripcion");
                    et_desc.requestFocus();

                } else if(TextUtils.isEmpty(cate)){

                    et_categoria.setError("Ingresa una categoria");
                    et_categoria.requestFocus();

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    request = new StringRequest(Request.Method.PUT, Config.PUT_ALIM_URL + id, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Log.d("RES_PUT", jsonObject.toString());

                                if(jsonObject.has("message")){
                                    String mensj = jsonObject.getString("message");

                                    if (mensj.equals("Plan de alimentacion actualizado")){
                                        Toast.makeText(getApplicationContext(),mensj, Toast.LENGTH_LONG).show();

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
                            params.put("categoria", cate);

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
