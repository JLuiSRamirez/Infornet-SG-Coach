package mx.infornet.sgcoach;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;



public class MainActivity extends AppCompatActivity {

    private StringRequest request;
    private RequestQueue queue;
    TextView about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        BottomAppBar bottomAppBar = findViewById(R.id.options);

        /*
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_exit:
                        queue = Volley.newRequestQueue(MainActivity.this);
                        request = new StringRequest(Request.Method.POST, Config.LOGOUT_URL, new Response.Listener<String>() {
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

                        queue.add(request);

                        ConexionSQLiteHelper  connl = new ConexionSQLiteHelper(getApplicationContext(), "coaches", null, 3);
                        SQLiteDatabase dbl = connl.getWritableDatabase();
                        dbl.execSQL("DELETE FROM coaches");
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                        break;
                    case R.id.nav_info:
                        Toast.makeText(MainActivity.this, "Presionaste el boton acerca", Toast.LENGTH_SHORT).show();
                        //Despues de actualizar los datos volvemos a cargar el fragmen del perfil
                        Fragment Home = new HomeFragment();
                        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, Home);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case R.id.nav_problems:
                        Toast.makeText(MainActivity.this, "Presionaste el boton reportar un problema", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        */


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_perfil:
                            selectedFragment = new PerfilFragment();
                            break;
                        case R.id.nav_members:
                            selectedFragment = new MembersFragment();
                            break;
                        case R.id.nav_routines:
                            selectedFragment = new RoutinesFragment();
                            break;
                        case R.id.nav_food:
                            selectedFragment = new FoodFragment();
                            break;
                        case R.id.nav_info:
                            selectedFragment = new AboutFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

}