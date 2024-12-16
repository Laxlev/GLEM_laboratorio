package com.example.glem_laboratorio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConsultarSol extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView usernametxt;
    private String token, tipo, nombre;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consultarsol);

        // Configurar el botón del menú
        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showMenu);
        //SharedPreferences
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null); // null es el valor por defecto si no encuentra la clave
        tipo = sharedPreferences.getString("tipo", "ALUMNO"); // Valor por defecto: "ALUMNO"
        nombre = sharedPreferences.getString("nombre", null);
        Log.d("Login", "Token: " + token + ", Tipo: " + tipo + ", Nombre: " + nombre);
        usernametxt = findViewById(R.id.usernametxt);
        usernametxt.setText(nombre);
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);

        // Crear menú dinámicamente
        popupMenu.getMenu().add(0, 1, 0, "Registrar Solicitudes");
        popupMenu.getMenu().add(0, 2, 1, "Consultar Solicitudes");
        popupMenu.getMenu().add(0, 3, 2, "Cancelar Solicitud");
        popupMenu.getMenu().add(0, 4, 3, "Logout");

        // Acciones al hacer clic en las opciones del menú
        popupMenu.setOnMenuItemClickListener(item -> handleMenuItemClick(item));

        popupMenu.show();
    }

    private boolean handleMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Registrar Solicitudes
                Intent intent1 = new Intent(ConsultarSol.this, RegistrarSol.class);  // Use ConsultarSol.this
                startActivity(intent1);
                return true;

            case 2: // Consultar Solicitudes
                Intent intent2 = new Intent(ConsultarSol.this, ConsultarSol.class);  // Use ConsultarSol.this
                startActivity(intent2);
                return true;

            case 3: // Cancelar Solicitud
                Intent intent3 = new Intent(ConsultarSol.this, CancelarSol.class);  // Use ConsultarSol.this
                startActivity(intent3);
                return true;

            case 4: // Logout
                Intent intent5 = new Intent(ConsultarSol.this, MainActivity.class);  // Use ConsultarSol.this
                startActivity(intent5);
                return true;

            default:
                return false;
        }
    }
}
