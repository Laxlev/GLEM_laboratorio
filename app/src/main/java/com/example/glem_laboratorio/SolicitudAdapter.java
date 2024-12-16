package com.example.glem_laboratorio;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudesViewHolder> {

    private List<Solicitud> solicitudList;
    private Integer idPrestamo;
    public SolicitudAdapter(List<Solicitud> solicitudList) {
        this.solicitudList = solicitudList;
    }

    @Override
    public SolicitudesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater  .from(parent.getContext()).inflate(R.layout.cancelarsol_view, parent, false);
        return new SolicitudesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SolicitudesViewHolder holder, int position) {
        Solicitud solicitud = solicitudList.get(position);

        // Aquí asignas los datos a cada elemento con los IDs que definiste en el XML
        holder.noLabTextView.setText(solicitud.getLaboratorio());
        holder.fechaTextView.setText(solicitud.getFecha());
        holder.horaTextView.setText(solicitud.getHora());
        holder.estadoTextView.setText(solicitud.getEstado());

        // Si el estado es "C", hacer el botón invisible
        if ("C".equals(solicitud.getEstado()) || "F".equals(solicitud.getEstado()) || "D".equals(solicitud.getEstado()) ) {
            holder.eliminarButton.setVisibility(View.INVISIBLE);
        } else {
            holder.eliminarButton.setVisibility(View.VISIBLE);
        }

        // Agregar el click listener para el botón de eliminar
        holder.eliminarButton.setOnClickListener(v -> {
            String idPrestamo = solicitud.getIdprestamo();

            // Mostrar o usar el idPrestamo como desees
            Log.d("Solicitud", "ID de préstamo: " + idPrestamo);

            // Cambiar el estado a "C" (cancelado) y ocultar el botón
            solicitud.setEstado("C");
            holder.estadoTextView.setText("C");
            holder.eliminarButton.setVisibility(View.INVISIBLE);

            // Realizar la solicitud DELETE a la API
            new DeletePrestamoTask().execute(idPrestamo.toString(), "C");
        });
    }

    @Override
    public int getItemCount() {
        return solicitudList.size();
    }

    public static class SolicitudesViewHolder extends RecyclerView.ViewHolder {
        TextView noLabTextView, fechaTextView, horaTextView, estadoTextView;
        Button eliminarButton;

        public SolicitudesViewHolder(View itemView) {
            super(itemView);
            // Aquí enlazamos los IDs del layout con las vistas
            noLabTextView = itemView.findViewById(R.id.textViewNoLab);
            fechaTextView = itemView.findViewById(R.id.textViewFecha);
            horaTextView = itemView.findViewById(R.id.textViewHora);
            estadoTextView = itemView.findViewById(R.id.textViewEstado);
            eliminarButton = itemView.findViewById(R.id.buttonEliminar);
        }
    }

    private class DeletePrestamoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String idPrestamo = params[0];  // Primer parámetro: idprestamo
            String estado = params[1];      // Segundo parámetro: estado (en este caso "C")
            String response = "";

            try {
                // Construir la URL con los parámetros idprestamo y estado
                String urlString = "https://nq6pfh4p-4000.usw3.devtunnels.ms/prestamo/delete/" + idPrestamo + "/" + estado;
                URL url = new URL(urlString);

                // Abrir la conexión y configurar el método HTTP
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(5000);  // Timeout de conexión (5 segundos)
                conn.setReadTimeout(5000);     // Timeout de lectura (5 segundos)

                // Leer la respuesta de la solicitud
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();

                // Aquí puedes manejar la respuesta de la API, si es necesario
                Log.d("Solicitud", "Respuesta de la API: " + response);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Aquí puedes hacer algo con la respuesta, si es necesario
            Log.d("Solicitud", "Resultado de la solicitud DELETE: " + result);
        }
    }
}

