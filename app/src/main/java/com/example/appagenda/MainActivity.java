package com.example.appagenda;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appagenda.Objetos.Contactos;
import com.example.appagenda.Objetos.ReferenciasFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnGuardar, btnListar, btnLimpiar;
    private TextView txtNombre, txtDireccion, txtTelefono1, txtTelefono2, txtNotas;
    private CheckBox cbxFavorito;
    private FirebaseDatabase basedatabase;
    private DatabaseReference referencia;
    private Contactos savedContacto;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        setEvents();
    }

    public void initComponents() {
        this.basedatabase = FirebaseDatabase.getInstance();
        this.referencia = this.basedatabase.getReferenceFromUrl(ReferenciasFirebase.URL_DATABASE +
            ReferenciasFirebase.DATABASE_NAME + "/" +
            ReferenciasFirebase.TABLE_NAME);

        this.txtNombre = findViewById(R.id.txtNombre);
        this.txtTelefono1 = findViewById(R.id.txtTelefono1);
        this.txtTelefono2 = findViewById(R.id.txtTelefono2);
        this.txtDireccion = findViewById(R.id.txtDireccion);
        this.txtNotas = findViewById(R.id.txtNotas);
        this.cbxFavorito = findViewById(R.id.cbxFavorito);
        this.btnGuardar = findViewById(R.id.btnGuardar);
        this.btnListar = findViewById(R.id.btnListar);
        this.btnLimpiar = findViewById(R.id.btnLimpiar);
        savedContacto = null;
    }

    public void setEvents() {
        this.btnGuardar.setOnClickListener(this);
        this.btnListar.setOnClickListener(this);
        this.btnLimpiar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (isNetworkAvailable()) {
            switch (view.getId()) {
                case R.id.btnGuardar:
                    boolean completo = true;
                    if (txtNombre.getText().toString().equals("")) {
                        txtNombre.setError("Introduce el nombre.");
                        completo = false;
                    }
                    if (txtTelefono1.getText().toString().equals("")) {
                        txtTelefono1.setError("Introduce el teléfono principal.");
                        completo = false;
                    }
                    if (txtDireccion.getText().toString().equals("")) {
                        txtDireccion.setError("Introduce la dirección.");
                        completo = false;
                    }
                    if (completo) {
                        Contactos nContacto = new Contactos();
                        nContacto.setNombre(txtNombre.getText().toString());
                        nContacto.setTelefono1(txtTelefono1.getText().toString());
                        nContacto.setTelefono2(txtTelefono2.getText().toString());
                        nContacto.setDireccion(txtDireccion.getText().toString());
                        nContacto.setNotas(txtNotas.getText().toString());
                        nContacto.setFavorite(cbxFavorito.isChecked() ? 1 : 0);
                        if (savedContacto == null) {
                            agregarContacto(nContacto);
                            Toast.makeText(getApplicationContext(), "Contacto guardado con éxito.", Toast.LENGTH_SHORT).show();
                            limpiar();
                        } else {
                            actualizarContacto(id, nContacto);
                            Toast.makeText(getApplicationContext(), "Contacto actualizado con éxito.", Toast.LENGTH_SHORT).show();
                            limpiar();
                        }
                    }
                    break;
                case R.id.btnListar:
                    Intent i = new Intent(MainActivity.this, ListaActivity.class);
                    limpiar();
                    startActivityForResult(i, 0);
                    break;
                case R.id.btnLimpiar:
                    limpiar();
                    break;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Se necesita tener conexión a Internet.", Toast.LENGTH_SHORT).show();
        }
    }

    public void agregarContacto(Contactos c) {
        DatabaseReference newContactoReference = referencia.push();
        // Obtener el ID del registro y setearlo
        String id = newContactoReference.getKey();
        c.set_ID(id);
        newContactoReference.setValue(c);
    }

    public void actualizarContacto(String id, Contactos p) {
        p.set_ID(id);
        referencia.child(String.valueOf(id)).setValue(p);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public void limpiar() {
        savedContacto = null;
        txtNombre.setText("");
        txtTelefono1.setText("");
        txtTelefono2.setText("");
        txtDireccion.setText("");
        txtNotas.setText("");
        cbxFavorito.setChecked(false);
        id = "";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent != null) {
            Bundle oBundle = intent.getExtras();
            if (Activity.RESULT_OK == resultCode) {
                Contactos contacto = (Contactos) oBundle.getSerializable("contacto");
                savedContacto = contacto;
                id = contacto.get_ID();
                txtNombre.setText(contacto.getNombre());
                txtTelefono1.setText(contacto.getTelefono1());
                txtTelefono2.setText(contacto.getTelefono2());
                txtDireccion.setText(contacto.getDireccion());
                txtNotas.setText(contacto.getNotas());
                if (contacto.getFavorite()>0) {
                    cbxFavorito.setChecked(true);
                } else {
                    limpiar();
                }
            }
        }
    }
}