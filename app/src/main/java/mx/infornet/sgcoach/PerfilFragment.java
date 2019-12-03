package mx.infornet.sgcoach;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PerfilFragment extends Fragment {

    private View myView;

    private ProgressBar progressBar;

    private String nombre, biografia, horario, correo, id_gimnasio, gimnasio, token, token_type;
    private TextView tv_nombre, tv_biografia, tv_horario, tv_gimnasio, tv_correo;
    private Button btn_edit;

    private RequestQueue queue;
    private StringRequest request;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_perfil, container, false);

        progressBar = myView.findViewById(R.id.prog_bar_perfil);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getActivity(), "coaches", null, 3);
        SQLiteDatabase db = conn.getWritableDatabase();

        queue = Volley.newRequestQueue(getContext());

        btn_edit = myView.findViewById(R.id.btn_editar);

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment editar = new EditarPerfilFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, editar);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        try {
            String query = "SELECT * FROM coaches";

            Cursor cursor = db.rawQuery(query, null);

            for(cursor.moveToFirst(); !cursor.isAfterLast();cursor.moveToNext()) {
                nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                biografia = cursor.getString(cursor.getColumnIndex("biografia"));
                correo = cursor.getString(cursor.getColumnIndex("email"));
                horario = cursor.getString(cursor.getColumnIndex("horarios"));
                id_gimnasio = cursor.getString(cursor.getColumnIndex("gimnasio"));
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("token_type"));

            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        db.close();
        progressBar.setVisibility(View.VISIBLE);
        request = new StringRequest(Request.Method.GET, Config.PERFIL_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject gimInfo = jsonObject.getJSONObject("gimnasio");
                    gimnasio = gimInfo.getString("nombre"); //guardarlo en la BD local??
                   // Toast.makeText(getContext(), "Nombre del gimnasio obtenido " + gimnasio, Toast.LENGTH_SHORT).show();
                    tv_nombre = myView.findViewById(R.id.nombre_coach);
                    tv_biografia = myView.findViewById(R.id.biografia);
                    tv_gimnasio = myView.findViewById(R.id.gimnasio);
                    tv_horario = myView.findViewById(R.id.horario);
                    tv_correo = myView.findViewById(R.id.correo);

                    tv_nombre.setText(nombre);
                    tv_biografia.setText(biografia);
                    tv_gimnasio.setText(gimnasio);
                    tv_horario.setText(horario);
                    tv_correo.setText(correo);

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
                        if (jsonObjectError.has("status")) {
                            String status = jsonObjectError.getString("status");
                            if (status.equals("Token is Expired")) {
                                Toast.makeText(getContext(), "Caduco la sesion, vuelve a ingresar a la aplicación", Toast.LENGTH_SHORT).show();
                                //eliminar todos los datos de la base de datos local para guardar los nuevos datos
                                //PENDIENTE
                                //---------
                                //---------
                                //...
                                //---------
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap();
                headers.put("Authorization", token_type + " " + token);
                return headers;
            }
        };

        queue.add(request);

        return myView;
    }
}
