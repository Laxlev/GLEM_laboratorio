package com.example.glem_laboratorio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView usuariosLabel;
    Button entrar;
    EditText usernameEditText, passwordEditText; // Campos de entrada
    Boolean maestro = false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        // Leer el valor de 'maestro' desde SharedPreferences
        maestro = sharedPreferences.getBoolean("maestro", false); // Default es false si no se ha guardado

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        usuariosLabel = findViewById(R.id.usuariosLabel);
        entrar = findViewById(R.id.btnEntrar);
        usernameEditText = findViewById(R.id.username); // Campo usuario
        passwordEditText = findViewById(R.id.password); // Campo contraseña

        // Deshabilitar modo oscuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        // Asignar un único listener para ambos botones
        View.OnClickListener buttonClickListener = this::handleButtonClick;
        entrar.setOnClickListener(buttonClickListener);
    }

    // Manejar clics de botones
    private void handleButtonClick(View view) {
        if (view.getId() == R.id.btnEntrar) {
            String correo = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            Log.d("Login", "Correo: " + correo + ", Contraseña: " + password);
            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tus credenciales", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(correo, password);
            }
        }
    }

    private void loginUser(String correo, String password) {
        new Thread(() -> {
            try {
                // URL del endpoint
                String url = "https://nq6pfh4p-3000.usw3.devtunnels.ms/auth/login";

                // Crear el JSON para enviar en el cuerpo
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("correo", correo);
                jsonBody.put("password", password);
                Log.d("Login", "JSON enviado: " + jsonBody.toString());

                // Realizar la solicitud POST
                HttpRequest request = HttpRequest.post(url)
                        .contentType("application/json") // Especificar el tipo de contenido
                        .accept("application/json") // Aceptar respuesta JSON
                        .send(jsonBody.toString()); // Enviar el JSON como cadena

                // Obtener el código de respuesta
                int responseCode = request.code();
                Log.d("Login", "Código de respuesta: " + responseCode);

                if (responseCode == 200) {
                    // Leer el cuerpo de la respuesta como JSON
                    String responseBody = request.body();
                    Log.d("Login", "Respuesta del servidor: " + responseBody);

                    // Parsear la respuesta
                    JSONObject responseJson = new JSONObject(responseBody);

                    if (responseJson.getString("status").equals("success")) {
                        // Extraer el token y el tipo
                        String token = responseJson.getString("token");
                        JSONObject data = responseJson.getJSONObject("data");
                        String tipo = data.getString("tipo");
                        String nombre = data.getString("nombre");
                        Integer id = data.getInt("idusuario");
                        Log.d("Login", "Nombre: " + nombre);

                        Log.d("Login", "Token: " + token + ", Tipo: " + tipo);

                        // Guardar el token y tipo en SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.putString("tipo", tipo);
                        editor.putString("nombre", nombre);
                        editor.putInt("id", id);
                        editor.apply();

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, RegistrarSol.class); // Ejemplo para maestros
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Error en el login: " + responseJson.optString("message", "Desconocido"), Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    String errorResponse = request.body();
                    Log.e("LoginError", "Error del servidor: " + errorResponse);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                Log.e("LoginError", "Error al realizar el login", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
