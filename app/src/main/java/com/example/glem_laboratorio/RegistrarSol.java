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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.kevinsawicki.http.HttpRequest;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RegistrarSol extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner selectLabSpinner;
    private ArrayList<String> laboratorios = new ArrayList<>();
    private ArrayList<String> laboratoriosIds = new ArrayList<>();
    private ArrayList<LatLng> laboratorioLocations = new ArrayList<>();
    private int laboratorioIndex = 0;
    private SharedPreferences sharedPreferences;
    private TextView usernametxt;
    private String token, tipo, nombre, hora;
    private String idlaboratorio;
    private Integer idusuario;
    private Button ButtonSubmit;
    private EditText materialEditText, dateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrarsol);

        // Configurar el botón del menú
        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showMenu);

        // Configurar DatePicker
        dateEditText = findViewById(R.id.dateEditText);
        dateEditText.setOnClickListener(v -> showDatePicker(dateEditText));

        Spinner timeSpinner = findViewById(R.id.timeSpinner);
        setupTimeSpinner(timeSpinner);


        //SharedPreferences
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null); // null es el valor por defecto si no encuentra la clave
        tipo = sharedPreferences.getString("tipo", "ALUMNO"); // Valor por defecto: "ALUMNO"
        nombre = sharedPreferences.getString("nombre", null);
        idusuario = sharedPreferences.getInt("id", -1);
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

        //Configurar el botón de enviar
        ButtonSubmit= findViewById(R.id.buttonSubmit);
        materialEditText = findViewById(R.id.materialEditText);
        View.OnClickListener buttonClickListener = this::handleButtonClick;
        ButtonSubmit.setOnClickListener(buttonClickListener);
    }


    private void setupTimeSpinner(Spinner timeSpinner) {
        // Lista de horas permitidas en formato HH:MM
        List<String> allowedHours = new ArrayList<>();
        for (int hour = 8; hour <= 20; hour += 1) {
            allowedHours.add(String.format("%02d:00", hour));
        }

        // Crear un ArrayAdapter para el Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                allowedHours
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);

        // Configurar el listener para manejar la selección
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener la hora seleccionada
                String selectedHour = allowedHours.get(position);
                hora = selectedHour;
                // Aquí puedes realizar acciones con la hora seleccionada
                Toast.makeText(getApplicationContext(), "Hora seleccionada: " + selectedHour, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no se selecciona nada
            }
        });
    }


    // Manejar clics de botones
    private void handleButtonClick(View view) {
        if (view.getId() == R.id.buttonSubmit) {
            submitSolicitudTask();
        }
    }

    private void loadLaboratorios() {
        String url = "https://nq6pfh4p-4000.usw3.devtunnels.ms/laboratorio/get";
        new FetchLaboratoriosTask().execute(url);
    }
    private void logout() {
        String url = "https://nq6pfh4p-4000.usw3.devtunnels.ms/auth/logout";
        new LogoutTask().execute(url);
    }

    private void submitSolicitudTask() {
         new Thread(() -> {
            try {
                String url = "https://nq6pfh4p-4000.usw3.devtunnels.ms/prestamo/create";
                // Crear el JSON para enviar en el cuerpo
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("idlaboratorio", laboratoriosIds.get(laboratorioIndex));
                jsonBody.put("idusuario",idusuario);
                String fechadate = dateEditText.getText().toString();
                // Definir el formato de entrada y salida
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate;
                try {
                    // Parsear la fecha y reformatearla
                    Date date = inputFormat.parse(fechadate);
                    formattedDate = outputFormat.format(date);
                    Integer duracion = 1;
                    jsonBody.put("fecha",formattedDate);
                    jsonBody.put("horainicio",hora + ":00");
                    jsonBody.put("duracion",duracion);
                    if(!materialEditText.getText().toString().isEmpty()){
                        jsonBody.put("observaciones",materialEditText.getText().toString());
                    }
                    Log.d("body", "fecha"+ formattedDate);
                    Log.d("idUsr", "idusuario"+ idusuario);
                    Log.i("body", "horainicio"+ hora + ":00");
                    // Imprimir la fecha formateada
                    System.out.println("Fecha formateada: " + formattedDate);
                } catch (ParseException e) {
                    System.err.println("Error al formatear la fecha: " + e.getMessage());
                }


                // Realizar la solicitud POST
                HttpRequest request = HttpRequest.post(url)
                    .contentType("application/json") // Especificar el tipo de contenido
                    .accept("application/json") // Aceptar respuesta JSON
                    .send(jsonBody.toString()); // Enviar el JSON como cadena

                // Obtener el código de respuesta
                int responseCode = request.code();
                Log.d("Login", "Código de respuesta: " + responseCode);
            }catch (Exception e) {
            Log.e("LoginError", "Error al realizar el login", e);
            runOnUiThread(() ->{
                Toast.makeText(RegistrarSol.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
            });
            }
        }).start();
    }

    private class LogoutTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                // Crear la URL
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Configurar la solicitud GET
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
            // Aquí puedes manejar la respuesta, pero también necesitas limpiar las preferencias
            try {
                // Limpiar SharedPreferences (eliminando el token)
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("token");
                editor.remove("tipo");
                editor.remove("nombre");
                editor.apply();

                // Redirigir al usuario a la pantalla de login o pantalla principal
                Intent intent = new Intent(RegistrarSol.this, MainActivity.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void updateSpinner() {
        Log.d("Spinner", "Actualizando Spinner");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, laboratorios);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectLabSpinner.setAdapter(adapter);
        selectLabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                changeMarker(position);
                laboratorioIndex = position;
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
        popupMenu.getMenu().add(0, 2, 1, "Solicitudes");
        popupMenu.getMenu().add(0, 3, 2, "Logout");

        // Acciones al hacer clic en las opciones del menú
        popupMenu.setOnMenuItemClickListener(this::handleMenuItemClick);

        popupMenu.show();
    }

    private boolean handleMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Registrar Solicitudes
                startActivity(new Intent(RegistrarSol.this, RegistrarSol.class));
                return true;
            case 2: // Cancelar Solicitud
                startActivity(new Intent(RegistrarSol.this, CancelarSol.class));
                return true;
            case 3: // Logout
                logout(); // Llamar al método logout
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
                Log.d("length" , String.valueOf(data.length()));
                for (int i = 0; i < data.length(); i++) {
                    Log.d("length" , i + " ");
                    JSONObject lab = data.getJSONObject(i);
                    Log.d("length" , lab.toString());
                    String aula = lab.optString("aula", "null") == "null" ? "" : lab.getString("aula");
                    String plantel = lab.getString("plantel") == "null" ? "" : lab.getString("plantel");
                    String departamento = lab.getString("departamento") == "null" ? "" : lab.getString("departamento");
                    String edificio = lab.getString("num_ed") == "null" ? "" : lab.getString("num_ed");
                    String nombre = plantel + " - " + "Edificio " + edificio + aula + " (" + departamento + ")";
                    laboratorios.add(nombre);
                    idlaboratorio = lab.getString("_id");
                    laboratoriosIds.add(idlaboratorio);
                    // Supongamos que los valores de latitud y longitud se obtienen como cadenas (String).
                    String latitudeString = lab.getString("latitude");
                    String longitudeString = lab.getString("longitude");

                    // Convierte las cadenas a valores de tipo double.
                    double latitude = Double.parseDouble(latitudeString);
                    double longitude = Double.parseDouble(longitudeString);
                    laboratorioLocations.add(new LatLng(latitude, longitude));
                }
                updateSpinner();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
