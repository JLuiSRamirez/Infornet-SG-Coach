package mx.infornet.sgcoach;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ShowAlimentacionActivity extends AppCompatActivity {

    private TextView nom, desc, cate;
    private FloatingActionButton btn_edit, btn_delete;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_alimentacion);



        Intent intent = getIntent();
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
        final String nombre = intent.getStringExtra("nombre");
        final String descripcion = intent.getStringExtra("descripcion");
        final int id = intent.getIntExtra("id", 0);
        final String categoria = intent.getStringExtra("categoria");

        nom = findViewById(R.id.title_alimentacion);
        desc = findViewById(R.id.descripcion_alim);
        cate = findViewById(R.id.categoria_alim);

        queue = Volley.newRequestQueue(this);

        btn_edit = findViewById(R.id.btn_edit_alim);
        btn_delete = findViewById(R.id.btn_delete_alim);

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ediIntent = new Intent(getApplicationContext(), EditarFoodActivity.class);
                ediIntent.putExtra("id", id);
                ediIntent.putExtra("nombre", nombre);
                ediIntent.putExtra("descripcion", descripcion);
                ediIntent.putExtra("categoria", categoria);

                startActivity(ediIntent);

            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request = new StringRequest(Request.Method.DELETE, Config.DELETE_ALIM_URL+id, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            Log.e("RESPONSE", jsonObject.toString());

                            if (jsonObject.has("status")) {

                                String status = jsonObject.getString("status");
                                if (status.equals("Token is expired")) {
                                    Toast.makeText(getApplicationContext(), "Token invalido. Favor de iniciar sesión nuevamente", Toast.LENGTH_LONG).show();
                                }
                            } else if(jsonObject.has("message")){
                                String mensaje = jsonObject.getString("message");

                                Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();

                            }

                        } catch (JSONException e){
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.toString());
                        String json = null;
                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 500:

                                    json = new String(response.data);
                                    System.out.println(json);
                                    break;
                            }
                            //Additional cases
                        }
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
        });

        nom.setText(nombre);
        desc.setText(descripcion);
        cate.setText(categoria);



    }
}
