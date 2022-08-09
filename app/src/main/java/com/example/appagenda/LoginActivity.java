package com.example.appagenda;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView txtUsuario, txtContrasena;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        iniciarComponentes();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {
                    iniciarSesion();
                } else {
                    Toast.makeText(getApplicationContext(), "Se necesita tener conexión a Internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void iniciarSesion () {
        String email = txtUsuario.getText().toString();
        String password = txtContrasena.getText().toString();
        boolean valido = true;

        if (txtUsuario.getText().toString().equals("")) {
            txtUsuario.setError("Introduce un usuario o correo electrónico.");
            valido = false;
        }
        if (txtContrasena.getText().toString().equals("")) {
            txtContrasena.setError("Introduce una contraseña.");
            valido = false;
        }
        if (valido) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                // updateUI(user);
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                            } else {
                                Log.d(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Usuario y/o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                                // updateUI(null);
                            }
                        }
                    });
        }
    }

    public void iniciarComponentes() {
        this.txtUsuario = findViewById(R.id.txtUsuario);
        this.txtContrasena = findViewById(R.id.txtContra);
        this.btnLogin = findViewById(R.id.btnLogin);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
}