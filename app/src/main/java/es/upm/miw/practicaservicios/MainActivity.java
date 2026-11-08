package es.upm.miw.practicaservicios;

import static es.upm.miw.practicaservicios.domain.logic.ConvertidorConsulta.extraerEstadisticas;
import static es.upm.miw.practicaservicios.domain.logic.ConvertidorConsulta.extraerSismos;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

// Firebase
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.miw.practicaservicios.api.UsgsApiService;
import es.upm.miw.practicaservicios.api.models.Feature;
import es.upm.miw.practicaservicios.api.models.Properties;
import es.upm.miw.practicaservicios.api.models.UsgsResponse;
import es.upm.miw.practicaservicios.data.ConsultaInfo;
import es.upm.miw.practicaservicios.data.ConsultaUsgs;
import es.upm.miw.practicaservicios.data.Estadisticas;
import es.upm.miw.practicaservicios.data.Sismo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    final static String LOG_TAG = "MiW";

    private static final String API_BASE_URL = "https://earthquake.usgs.gov/";

    private static final int LIMIT_API = 500;     // máximo de eventos que pedimos a USGS
    private static final int MAX_SISMOS = 100;    // máximo de sismos que guardamos en la lista

    private UsgsApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        estableceInserciones();

        launchSignIn();
    }

    private void launchSignIn(){
        // Choose Firebase Authentication providers
        List<AuthUI.IdpConfig> authProviders = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build()
        );

        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.auth_custom_layout)
                .setGoogleButtonId(R.id.btn_google)
                .setEmailButtonId(R.id.btn_email)
                .setAnonymousButtonId(R.id.btn_anonymous)
                .build();

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setCredentialManagerEnabled(false)
                .setAvailableProviders(authProviders)
                .setTheme(R.style.AppTheme)
                .setLogo(R.drawable.user_pin_24)
                .setAuthMethodPickerLayout(customLayout)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    Log.i(LOG_TAG, "onActivityResult() " + result.toString());
                    onSignInResult(result);
                }
            }
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse identityProvider_response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // user is signed in
                String name = user.getDisplayName();
                CharSequence email = user.getEmail();

                Toast.makeText(MainActivity.this, getString(R.string.firebase_user_fmt, name, email), Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, "onSignInResult() " + getString(R.string.firebase_user_fmt, name, email));
                ((TextView) findViewById(R.id.textView)).setText(getString(R.string.firebase_user_fmt, name, email));

                guardarConsultaUltimaHora(user);
            }
        } else {
            // Sign in failed. If IdentityProvider_response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // IdentityProvider_response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, R.string.signed_cancelled, Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "onSignInResult() CANCELED:" + getString(R.string.signed_cancelled));
            if (identityProvider_response != null && identityProvider_response.getError() != null) {
                Log.e(LOG_TAG, "ERROR: " + identityProvider_response.getError().getMessage());
            }
            finish();
        }
    }
    private void guardarConsultaUltimaHora(FirebaseUser user) {
        // Periodo: última hora, en UTC (USGS interpreta las fechas sin zona como UTC)
        DateTimeFormatter fmt = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC);
        Instant ahora = Instant.now();
        final String desde = fmt.format(ahora.minus(1, ChronoUnit.HOURS));
        final String hasta = fmt.format(ahora);
        final Context ctx = getApplicationContext();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsgsApiService api = retrofit.create(UsgsApiService.class);

        Call<UsgsResponse> call_async = api.getEarthquakes(
                "geojson", desde, hasta, null, null, null, null, "time", LIMIT_API);

        call_async.enqueue(new Callback<UsgsResponse>() {
            @Override
            public void onResponse(Call<UsgsResponse> call, Response<UsgsResponse> response) {
                UsgsResponse usgsResponse = response.body();
                if (usgsResponse == null) {
                    Log.w(LOG_TAG, "Respuesta no válida: " + response.code());
                    return;
                }

                // 1) Convertir cada evento a Sismo y acumular datos para el resumen
                List<Sismo> todos = extraerSismos(usgsResponse);
                Estadisticas estadisticas = extraerEstadisticas(ctx,todos);
                estadisticas.setTruncado(usgsResponse.getFeatures().size() >= LIMIT_API);

                todos.sort((a, b) -> Double.compare(b.getMag(), a.getMag()));
                List<Sismo> guardar = todos.size() > MAX_SISMOS
                        ? new ArrayList<>(todos.subList(0, MAX_SISMOS)) : todos;
                estadisticas.setGuardados(guardar.size());

                ConsultaInfo info = new ConsultaInfo();
                info.setZonaNombre("Global");
                info.setArea(null);
                info.setFechaInicio(desde);
                info.setFechaFin(hasta);
                info.setMagMin(null);

                ConsultaUsgs consulta = new ConsultaUsgs();
                consulta.setInfo(info);
                consulta.setSismos(guardar);
                consulta.setEstadisticas(estadisticas);

                FirebaseFirestore.getInstance()
                        .collection("users").document(user.getUid())
                        .collection("consultas").add(consulta)
                        .addOnSuccessListener(ref ->
                                Log.i(LOG_TAG, "Consulta guardada: " + ref.getId()
                                        + " (total=" + estadisticas.getNumTotalSismos() + ")"))
                        .addOnFailureListener(e -> Log.e(LOG_TAG, "Error al guardar", e));
            }

            @Override
            public void onFailure(Call<UsgsResponse> call, Throwable t) {
                Log.e(LOG_TAG, "Error de red: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.opcLogout) {
            logout();
            return true;
        } else if (id == R.id.opcDeleteUser) {
            confirmDeleteUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Log.w(LOG_TAG, getString(R.string.signed_out));
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // Sign out of all social identity providers
                    }
                });
        launchSignIn();
    }

    private void confirmDeleteUser() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Se borrará tu cuenta de forma permanente. ¿Continuar?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteUser())
                .setNegativeButton("Cancelar", null)
                .show();
    }
    public void deleteUser() {
        Log.w(LOG_TAG, getString(R.string.deleted_user));
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        launchSignIn();
    }

    // Establece las inserciones de recortes de pantalla
    private void estableceInserciones() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}