package mx.infornet.sgcoach;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShowPlanesActivity extends AppCompatActivity {

    private TextView tv_nombre, tv_precio, tv_servicios;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type;
    private JSONArray serviciosArray;
    private StringBuilder formateado;

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
        final double precio = intent.getDoubleExtra("precio",0);
        final String services = intent.getStringExtra("services");

        try {
             serviciosArray = new JSONArray(services);

                formateado = new StringBuilder();

                for (int i = 0; i < serviciosArray.length(); i++) {

                    JSONObject obj = serviciosArray.getJSONObject(i);

                    formateado.append(obj.getString("nombre"));
                    formateado.append("   $");
                    formateado.append(obj.getString("precio"));
                    formateado.append("\n");

            }
        }catch(JSONException e) {
            Toast.makeText(this, "ERROR: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        tv_nombre = findViewById(R.id.title_plan);
        tv_precio = findViewById(R.id.precio_plan);
        tv_servicios = findViewById(R.id.servicios_plan);


        queue = Volley.newRequestQueue(this);


        tv_nombre.setText(nombre);
        tv_precio.setText(String.valueOf(precio));
        tv_servicios.setText(formateado);

    }
}
