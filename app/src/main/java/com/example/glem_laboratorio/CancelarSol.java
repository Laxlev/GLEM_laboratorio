package com.example.glem_laboratorio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CancelarSol extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView usernametxt;
    private String token, tipo, nombre;
    private RecyclerView recyclerView;
    private SolicitudAdapter solicitudesAdapter;
    private List<Solicitud> solicitudList;
    private Integer idusuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cancelarsol);

        // Configurar el botón del menú
        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showMenu);

        // Inicializamos RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crear una lista de solicitudes de ejemplo
        solicitudList = new ArrayList<>();

        // Configurar el Adapter
        solicitudesAdapter = new SolicitudAdapter(solicitudList);
        recyclerView.setAdapter(solicitudesAdapter);

        //SharedPreferences
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null); // null es el valor por defecto si no encuentra la clave
        tipo = sharedPreferences.getString("tipo", "ALUMNO"); // Valor por defecto: "ALUMNO"
        nombre = sharedPreferences.getString("nombre", null);
        Log.d("Login", "Token: " + token + ", Tipo: " + tipo + ", Nombre: " + nombre);
        usernametxt = findViewById(R.id.usernametxt);
        usernametxt.setText(nombre);

        loadUrls();
    }

    private void loadUrls() {
        idusuario = sharedPreferences.getInt("id", -1);
        String urlSol = "https://nq6pfh4p-4000.usw3.devtunnels.ms/prestamo/get/usuario/" + idusuario;
        new FetchSolicitudesTask().execute(urlSol);
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);

        // Crear menú dinámicamente
        popupMenu.getMenu().add(0, 1, 0, "Registrar Solicitudes");
        popupMenu.getMenu().add(0, 2, 1, "Solicitudes");
        popupMenu.getMenu().add(0, 3, 2, "Logout");

        // Acciones al hacer clic en las opciones del menú
        popupMenu.setOnMenuItemClickListener(item -> handleMenuItemClick(item));
        popupMenu.show();
    }

    private class FetchSolicitudesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                // Obtener el idusuario desde SharedPreferences
                int idUsuario = sharedPreferences.getInt("id", -1);

                // Crear la URL para la solicitud de préstamos
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Enviar el token en el encabezado (header)
                conn.setRequestProperty("x-access-token", token);

                // Obtener la respuesta
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            // Aquí procesas el JSON y filtras las solicitudes
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray data = jsonResponse.getJSONArray("data");

                // Limpiar la lista de solicitudes
                solicitudList.clear();

                // Filtrar las solicitudes que coinciden con el idusuario
                for (int i = 0; i < data.length(); i++) {
                    JSONObject solicitudJson = data.getJSONObject(i);
                    JSONObject laboratorio = solicitudJson.getJSONObject("laboratorio");
                    String numEd = laboratorio.getString("num_ed");

                    String fechaOriginal = solicitudJson.getString("fecha");

                        // Formatear la fecha
                        try {
                            String idprestamo = solicitudJson.getString("_id");
                            // Obtener y formatear la hora
                            String horaOriginal = solicitudJson.getString("horainicio");
                            SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm:ss");
                            Date horaDate = horaFormat.parse(horaOriginal);
                            SimpleDateFormat horaTargetFormat = new SimpleDateFormat("HH:mm");
                            String hora = horaTargetFormat.format(horaDate);
                            String estado = solicitudJson.getString("estado");
                            Log.d("Login", "Laboratorio: " + numEd + ", Fecha: " + fechaOriginal + ", Hora: " + hora + ", Estado: " + estado);

                            // Crear el objeto Solicitud y agregarlo a la lista
                            Solicitud solicitud = new Solicitud( numEd, fechaOriginal, hora, estado, idprestamo);
                            solicitudList.add(solicitud);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                // Actualizar el RecyclerView con las solicitudes filtradas
                solicitudesAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean handleMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Registrar Solicitudes
                Intent intent1 = new Intent(CancelarSol.this, RegistrarSol.class);
                startActivity(intent1);
                return true;

            case 2: // Cancelar Solicitud
                Intent intent3 = new Intent(CancelarSol.this, CancelarSol.class);
                startActivity(intent3);
                return true;

            case 3: // logout
                Intent intent5 = new Intent(CancelarSol.this, MainActivity.class);
                startActivity(intent5);
                return true;

            default:
                return false;
        }
    }
}
