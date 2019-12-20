package mx.infornet.sgcoach;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ShowPlanesActivity extends AppCompatActivity {

    private TextView tv_nombre, tv_precio, tv_servicios;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_show_planes);


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
        final String servicios = intent.getStringExtra("servicios");
        final double precio = intent.getDoubleExtra("precio",0);
        final int id = intent.getIntExtra("id", 0);

        tv_nombre = findViewById(R.id.title_plan);
        tv_precio = findViewById(R.id.precio_plan);
        tv_servicios = findViewById(R.id.servicios_plan);


        queue = Volley.newRequestQueue(this);


        tv_nombre.setText(nombre);
        tv_precio.setText(String.valueOf(precio));
        tv_servicios.setText(servicios);

    }
}
