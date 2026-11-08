package es.upm.miw.practicaservicios.ui.activities;

import static es.upm.miw.practicaservicios.domain.logic.ConvertidorConsulta.extractEstadisticas;
import static es.upm.miw.practicaservicios.domain.logic.ConvertidorConsulta.extractSismos;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.upm.miw.practicaservicios.R;
import es.upm.miw.practicaservicios.api.UsgsApiService;
import es.upm.miw.practicaservicios.api.models.UsgsResponse;
import es.upm.miw.practicaservicios.data.ConsultaInfo;
import es.upm.miw.practicaservicios.data.ConsultaUsgs;
import es.upm.miw.practicaservicios.data.Estadisticas;
import es.upm.miw.practicaservicios.data.Sismo;
import es.upm.miw.practicaservicios.domain.logic.ConsultaService;
import es.upm.miw.practicaservicios.domain.repository.ConsultaRepository;
import es.upm.miw.practicaservicios.ui.fragments.DeleteUserAlertDialog;
import es.upm.miw.practicaservicios.ui.viewmodel.ConsultaAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements DeleteUserAlertDialog.OnDeleteUserConfirmedListener {
    final static String LOG_TAG = "MiW";
    private static final String API_BASE_URL = "https://earthquake.usgs.gov/";

    private static final int LIMIT_API = 500;     // máximo de eventos que pedimos a USGS
    private static final int MAX_SISMOS = 100;    // máximo de sismos que guardamos en la lista

    private UsgsApiService api;

    private ConsultaAdapter adapter;
    private TextView tvEmpty;

    private FloatingActionButton fab;

    private ConsultaService consultaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        estableceInserciones();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConsultaAdapter(this);
        recyclerView.setAdapter(adapter);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(View.VISIBLE);

        adapter.setOnConsultaClickListener(c -> {
            Intent intent = new Intent(this, DetalleConsultaActivity.class);
            intent.putExtra(DetalleConsultaActivity.EXTRA_CONSULTA_ID, c.getId());
            startActivity(intent);
        });

        fab = findViewById(R.id.fabAddQuery);
        fab.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                realizarConsulta(user, ConsultaService.globalLastHourInfo());
            }
        });

        consultaService = new ConsultaService(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ((TextView) findViewById(R.id.textView)).setText(
                    (user.getDisplayName()!=null &&
                        !user.getDisplayName().trim().isEmpty() &&
                        !user.isAnonymous())
                            ? getString(R.string.welcome_user_fmt, user.getDisplayName())
                            : getString(R.string.welcome_guest)
            );
            cargarHistorial(user);
        } else if (savedInstanceState == null) {
            launchSignIn();
        }
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
            new DeleteUserAlertDialog().show(getSupportFragmentManager(), "deleteUserDialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteUserConfirmed() {
        deleteUser();
    }

    private void launchSignIn(){
        // Choose Firebase Authentication providers
        List<AuthUI.IdpConfig> authProviders = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build()
        );

        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.auth_custom_layout)
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
                if(name != null){
                    ((TextView) findViewById(R.id.textView)).setText(getString(R.string.welcome_user_fmt, name));
                }else ((TextView) findViewById(R.id.textView)).setText(getString(R.string.welcome_guest));

                cargarHistorial(user);
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


    private void realizarConsulta(FirebaseUser user, ConsultaInfo info) {
        fab.setEnabled(false);

        consultaService.askForSismos(info, new Callback<UsgsResponse>() {
            @Override
            public void onResponse(Call<UsgsResponse> call, Response<UsgsResponse> response) {
                UsgsResponse usgsResponse = response.body();
                if (usgsResponse == null) {
                    Log.w(LOG_TAG, "Respuesta no válida: " + response.code());
                    errorConsulta();
                    return;
                }

                ConsultaUsgs consulta = consultaService.buildConsulta(info, usgsResponse);

                new ConsultaRepository(user).save(consulta)
                        .addOnSuccessListener(ref -> {
                            Log.i(LOG_TAG, "Consulta guardada: " + ref.getId());
                            Toast.makeText(MainActivity.this, R.string.query_saved, Toast.LENGTH_SHORT).show();
                            cargarHistorial(user);
                            fab.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(LOG_TAG, "Error al guardar", e);
                            errorConsulta();
                        });
            }

            @Override
            public void onFailure(Call<UsgsResponse> call, Throwable t) {
                Log.e(LOG_TAG, "Error de red: " + t.getMessage());
                errorConsulta();
            }
        });
    }

    private void errorConsulta() {
        Toast.makeText(this, R.string.query_error, Toast.LENGTH_SHORT).show();
        fab.setEnabled(true);
    }

    private void cargarHistorial(FirebaseUser user) {
        new ConsultaRepository(user).last(20)
                .addOnSuccessListener(snapshot -> {
                    List<ConsultaUsgs> consultas = snapshot.toObjects(
                            ConsultaUsgs.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                    adapter.setConsultas(consultas);
                    tvEmpty.setVisibility(consultas.isEmpty() ? View.VISIBLE : View.GONE);
                    Log.i(LOG_TAG, "Historial cargado: " + consultas.size() + " consultas");
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error al leer el historial", e);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
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

    private void deleteUser() {
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