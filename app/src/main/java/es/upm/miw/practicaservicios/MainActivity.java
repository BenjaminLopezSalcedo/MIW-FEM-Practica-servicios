package es.upm.miw.practicaservicios;

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

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final static String LOG_TAG = "MiW";

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