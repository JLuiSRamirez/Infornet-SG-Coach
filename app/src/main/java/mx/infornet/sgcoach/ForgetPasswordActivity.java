package mx.infornet.sgcoach;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgetPasswordActivity extends AppCompatActivity {

    private TextInputEditText correo;
    private ProgressBar progressBar;
    private MaterialButton btn_forget;
    private RequestQueue queue;
    private StringRequest request;
    private Button btn_regresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_forget_password);

        correo = findViewById(R.id.email_forget);

        progressBar = findViewById(R.id.prog_bar_forget);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        btn_forget = findViewById(R.id.btn_forget_pass);
        btn_regresar = findViewById(R.id.regresar);

        queue = Volley.newRequestQueue(this);

        btn_regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
                    ForgetPasswordActivity.this.finish();
                }catch(Exception e) {
                    Toast.makeText(ForgetPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_regresar.setVisibility(View.INVISIBLE);

                String emailPattern = "^[a-zA-Z0-9\\._-]+@[a-zA-Z0-9-]{2,}[.][a-zA-Z]{2,4}$";

                final String email = correo.getText().toString();

                //se validan los campos
                if(TextUtils.isEmpty(email) || !email.matches(emailPattern)){
                    correo.setError("Ingresa un correo valido. Ej. example@mail.com");
                    correo.requestFocus();
                    btn_regresar.setVisibility(View.VISIBLE);
                    return;
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    request = new StringRequest(Request.Method.POST, Config.EMAIL_RESET_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            btn_regresar.setVisibility(View.VISIBLE);

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                if (jsonObject.has("error")){
                                    String error = jsonObject.getString("error");
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                                } else if(jsonObject.has("status")){
                                    String mensaje = jsonObject.getString("status");

                                    //Log.d("RES_SET_PASS", jsonObject.toString());

                                    new AlertDialog.Builder(ForgetPasswordActivity.this)
                                            .setTitle("Enviado !")
                                            .setMessage(mensaje)
                                            .setCancelable(false)
                                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                                                    startActivity(intent);

                                                    ForgetPasswordActivity.this.finish();
                                                }
                                            })
                                            .show();
                                }

                            } catch (JSONException e){
                                e.printStackTrace();
                                String err = e.toString();
                                Toast.makeText(getApplicationContext(), "Error " + err, Toast.LENGTH_LONG).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.GONE);
                            btn_regresar.setVisibility(View.VISIBLE);
                            try {
                                if (error.networkResponse.statusCode == 500) {
                                    Toast.makeText(getApplicationContext(), "No existe una cuenta registrada con este correo", Toast.LENGTH_SHORT).show();
                                    correo.setText("");
                                    correo.requestFocus();
                                } else {
                                    Toast.makeText(getApplicationContext(), "No se pudo establecer una conexion con el servidor. Comprueba tu conexion a internet", Toast.LENGTH_SHORT).show();
                                }
                            }catch (Exception e) {
                                btn_regresar.setVisibility(View.VISIBLE);
                                Toast.makeText(ForgetPasswordActivity.this, "Servicio no disponible por el momento, intentelo nuevamente más tarde", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(ForgetPasswordActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            return hashMap;
                        }
                    };
                    queue.add(request);
                }
            }
        });
    }
}