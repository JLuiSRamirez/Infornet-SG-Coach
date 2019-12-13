package mx.infornet.sgcoach;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodFragment extends Fragment {

    private View myView;
    private RecyclerView recyclerView;
    private List<Alimentacion> alimentacionList;
    private StringRequest request;
    private RequestQueue queue;
    private String token, token_type;
    private FloatingActionButton btn_add_;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_food, container, false);

        recyclerView = myView.findViewById(R.id.recycler_view_alimentacion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btn_add_ = myView.findViewById(R.id.btn_planes_alimentacion_add);
        progressBar = myView.findViewById(R.id.progressbar_alim);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        btn_add_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment editar = new AgregarFoodFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, editar);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        alimentacionList = new ArrayList<>();

        ConexionSQLiteHelper conn = new ConexionSQLiteHelper(getActivity(), "coaches", null, 3);
        SQLiteDatabase db = conn.getWritableDatabase();

        try {
            String query = "SELECT * FROM coaches";

            Cursor cursor = db.rawQuery(query, null);

            for(cursor.moveToFirst(); !cursor.isAfterLast();cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("token_type"));
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        Log.d("token", token);

        db.close();

        queue = Volley.newRequestQueue(getContext());

        request = new StringRequest(Request.Method.GET, Config.GET_ALIMENTACION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONArray array = new JSONArray(response);

                    Log.d("PLANES_ALIM", array.toString());

                    for (int i=0; i<array.length(); i++){

                        JSONObject rutina = array.getJSONObject(i);

                        alimentacionList.add(new Alimentacion(
                                rutina.getInt("id"),
                                rutina.getString("nombre"),
                                rutina.getString("descripcion"),
                                rutina.getString("categoria")
                        ));

                        AdapterAlimentacion adapterAlimentacion = new AdapterAlimentacion(getContext(), alimentacionList);

                        recyclerView.setAdapter(adapterAlimentacion);
                    }

                } catch (JSONException e){
                    Log.e("ERRORJSON", e.toString());
                }

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.has("status")){
                        String status = jsonObject.getString("status");

                        if (status.equals("Token is Expired")){
                            Toast.makeText(getContext(), status+". Favor de iniciar sesión nuevamente", Toast.LENGTH_LONG).show();

                            ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getContext(), "coaches", null, 3);
                            SQLiteDatabase db = conn.getWritableDatabase();
                            db.execSQL("DELETE FROM coaches");

                            startActivity(new Intent(getContext(), LoginActivity.class));
                            getActivity().finish();
                        }

                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR_RES", error.toString());
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

        return myView;
    }
}
