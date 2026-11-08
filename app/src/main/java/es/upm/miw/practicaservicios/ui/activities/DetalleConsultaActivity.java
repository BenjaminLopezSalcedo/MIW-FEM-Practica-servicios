package es.upm.miw.practicaservicios.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.upm.miw.practicaservicios.R;
import es.upm.miw.practicaservicios.data.ConsultaInfo;
import es.upm.miw.practicaservicios.data.ConsultaUsgs;
import es.upm.miw.practicaservicios.data.Estadisticas;
import es.upm.miw.practicaservicios.data.Sismo;
import es.upm.miw.practicaservicios.ui.viewmodel.EarthquakeAdapter;

public class DetalleConsultaActivity extends AppCompatActivity {

    public static final String EXTRA_CONSULTA_ID = "consulta_id";
    private static final String LOG_TAG = "MiW";

    private TextView tvHeader, tvTotal, tvSaved, tvMaxMag, tvMaxMagLocation;
    private TextView tvRangesTitle, tvRanges, tvNoEarthquake;
    private EarthquakeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_detail);

        estableceInserciones();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvHeader = findViewById(R.id.tvDetailHeader);
        tvTotal = findViewById(R.id.tvDetailTotalEarthquakes);
        tvSaved = findViewById(R.id.tvDetailSavedEarthquakes);
        tvMaxMag = findViewById(R.id.tvDetailMaxMagnitude);
        tvMaxMagLocation = findViewById(R.id.tvDetailMaxMagnitudeLocation);
        tvRangesTitle = findViewById(R.id.tvDetailRangesTitle);
        tvRanges = findViewById(R.id.tvDetailRanges);
        tvNoEarthquake = findViewById(R.id.tvNoEarthquake);

        RecyclerView recycler = findViewById(R.id.recyclerEarthquake);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EarthquakeAdapter(this);
        recycler.setAdapter(adapter);

        String id = getIntent().getStringExtra(EXTRA_CONSULTA_ID);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (id == null || user == null) {
            finish();
            return;
        }
        cargarConsulta(user, id);
    }

    /** Lee de Firestore el documento users/{uid}/consultas/{id}. */
    private void cargarConsulta(FirebaseUser user, String id) {
        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .collection("consultas").document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    ConsultaUsgs consulta = doc.toObject(ConsultaUsgs.class,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                    if (consulta == null) {
                        finish();
                        return;
                    }
                    mostrarConsulta(consulta);
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error al leer la consulta", e);
                    Toast.makeText(this, R.string.query_error_detail, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /** Reparte los datos de la consulta entre las vistas de la pantalla. */
    private void mostrarConsulta(ConsultaUsgs consulta) {
        ConsultaInfo info = consulta.getInfo();
        Estadisticas est = consulta.getEstadisticas();

        // Cabecera: zona · fecha de la consulta
        if (info != null) {
            String zona = info.getZonaNombre() != null ? info.getZonaNombre() : "-";
            String fecha = info.getFechaConsulta() != null
                    ? new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(info.getFechaConsulta())
                    : "-";
            tvHeader.setText(zona + " · " + fecha);
        }

        // Resumen
        if (est != null) {
            tvTotal.setText(getString(R.string.detail_total_fmt, est.getNumTotalSismos()));
            tvSaved.setText(getString(R.string.detail_saved_fmt, est.getGuardados()));
            tvMaxMag.setText(getString(R.string.detail_max_mag_fmt,
                    est.getMagMax() != null
                            ? String.format(Locale.getDefault(), "%.1f", est.getMagMax()) : "-"));
            tvMaxMagLocation.setText(est.getEventoMaxLugar() != null
                    ? est.getEventoMaxLugar() : "");
            tvRangesTitle.setText(R.string.detail_ranges_title);
            tvRanges.setText(textoRangos(est.getRangos()));
        }

        // Lista de sismos
        List<Sismo> sismos = consulta.getSismos() != null
                ? consulta.getSismos() : new ArrayList<>();
        adapter.setSismos(sismos);
        tvNoEarthquake.setVisibility(sismos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Rangos en orden fijo, porque Firestore no conserva el orden de un mapa. */
    private String textoRangos(Map<String, Integer> rangos) {
        if (rangos == null) return "";
        String[] orden = {
                getString(R.string.range_micro), getString(R.string.range_minor),
                getString(R.string.range_light), getString(R.string.range_moderate),
                getString(R.string.range_strong)
        };
        StringBuilder sb = new StringBuilder();
        for (String clave : orden) {
            Integer n = rangos.get(clave);
            if (n == null) continue;
            if (sb.length() > 0) sb.append(" · ");
            sb.append(clave).append(": ").append(n);
        }
        return sb.toString();
    }

    // Establece las inserciones de recortes de pantalla
    private void estableceInserciones() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}