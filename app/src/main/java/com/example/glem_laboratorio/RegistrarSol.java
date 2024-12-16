package com.example.glem_laboratorio;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class RegistrarSol extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner selectLabSpinner;
    private ArrayList<String> laboratorios = new ArrayList<>();
    private ArrayList<LatLng> laboratorioLocations = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private TextView usernametxt;
    private String token, tipo, nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrarsol);

        // Configurar el botón del menú
        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showMenu);

        // Configurar DatePicker
        EditText dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setOnClickListener(v -> showDatePicker(dateEditText));

        // Configurar TimePicker
        EditText timeEditText = findViewById(R.id.timeEditText);
        timeEditText.setOnClickListener(v -> showTimePicker(timeEditText));

        //SharedPreferences
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null); // null es el valor por defecto si no encuentra la clave
        tipo = sharedPreferences.getString("tipo", "ALUMNO"); // Valor por defecto: "ALUMNO"
        nombre = sharedPreferences.getString("nombre", null);
        Log.d("Login", "Token: " + token + ", Tipo: " + tipo + ", Nombre: " + nombre);
        usernametxt = findViewById(R.id.usernametxt);
        usernametxt.setText(nombre);

        // Configurar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar el Spinner
        selectLabSpinner = findViewById(R.id.selectLab);
        loadLaboratorios(); // Cargar datos desde la API
    }

    private void loadLaboratorios() {
        String url = "https://nq6pfh4p-3000.usw3.devtunnels.ms/laboratorio/get";
        new FetchLaboratoriosTask().execute(url);
    }

    private void updateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, laboratorios);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectLabSpinner.setAdapter(adapter);
        selectLabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                changeMarker(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No hacer nada si no se selecciona nada
            }
        });
    }

    private void changeMarker(int position) {
        if (mMap == null || position < 0 || position >= laboratorioLocations.size()) return;

        LatLng selectedLabLocation = laboratorioLocations.get(position);
        String selectedLabTitle = laboratorios.get(position);

        // Agregar marcador en el mapa
        mMap.clear(); // Elimina cualquier marcador previo
        mMap.addMarker(new MarkerOptions().position(selectedLabLocation).title(selectedLabTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLabLocation, 16));
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
            editText.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
            editText.setText(time);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);

        // Crear menú dinámicamente
        popupMenu.getMenu().add(0, 1, 0, "Registrar Solicitudes");
        popupMenu.getMenu().add(0, 2, 1, "Consultar Solicitudes");
        popupMenu.getMenu().add(0, 3, 2, "Cancelar Solicitud");
        if (tipo.equals("MAESTRO")) {
            popupMenu.getMenu().add(0, 4, 3, "Cambio de Horario");
        }
        popupMenu.getMenu().add(0, 5, 4, "Logout");

        // Acciones al hacer clic en las opciones del menú
        popupMenu.setOnMenuItemClickListener(this::handleMenuItemClick);

        popupMenu.show();
    }

    private boolean handleMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Registrar Solicitudes
                startActivity(new Intent(RegistrarSol.this, RegistrarSol.class));
                return true;

            case 2: // Consultar Solicitudes
                startActivity(new Intent(RegistrarSol.this, ConsultarSol.class));
                return true;

            case 3: // Cancelar Solicitud
                startActivity(new Intent(RegistrarSol.this, CancelarSol.class));
                return true;

            case 4: // Cambio de Horario
                startActivity(new Intent(RegistrarSol.this, CancelarSol.class));
                return true;

            case 5: // Logout
                startActivity(new Intent(RegistrarSol.this, MainActivity.class));
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Configuración inicial del mapa si es necesario
    }

    private class FetchLaboratoriosTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
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
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray data = jsonResponse.getJSONArray("data");

                laboratorios.clear();
                laboratorioLocations.clear();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject lab = data.getJSONObject(i);
                    String aula = lab.getString("aula") == "null" ? "" : lab.getString("aula");
                    String nombre = lab.getString("plantel") + " - " +
                            "Edificio " + lab.getInt("num_ed") + aula +
                            " (" + lab.getString("departamento") + ")";
                    laboratorios.add(nombre);

                    double latitude = lab.getDouble("latitude");
                    double longitude = lab.getDouble("longitude");
                    laboratorioLocations.add(new LatLng(latitude, longitude));
                }
                updateSpinner();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
