package com.example.appagenda;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appagenda.Objetos.Contactos;
import com.example.appagenda.Objetos.ReferenciasFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListaActivity extends ListActivity {
    private FirebaseDatabase basedatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference referencia;
    private Button btnNuevo, btnCerrar;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        basedatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        referencia = basedatabase.getReferenceFromUrl(ReferenciasFirebase.URL_DATABASE +
            ReferenciasFirebase.DATABASE_NAME + "/" +
            ReferenciasFirebase.TABLE_NAME);
        btnNuevo = (Button) findViewById(R.id.btnNuevo);
        btnCerrar = (Button) findViewById(R.id.btnCerrar);
        obtenerContactos();
        btnNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(ListaActivity.this, LoginActivity.class);
                startActivity(i);
                Toast.makeText(context, "Se ha cerrado la sesión.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent i = new Intent(ListaActivity.this, LoginActivity.class);
            startActivity(i);
        }
    }

    public void obtenerContactos() {
        final ArrayList<Contactos> contactos = new ArrayList<Contactos>();
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Contactos contacto = dataSnapshot.getValue(Contactos.class);
                contactos.add(contacto);
                final MyArrayAdapter adapter = new MyArrayAdapter(context, R.layout.layout_contacto, contactos);
                setListAdapter(adapter);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        referencia.addChildEventListener(listener);
    }

    class MyArrayAdapter extends ArrayAdapter<Contactos> {
        Context context;
        int textViewRecursoId;
        ArrayList<Contactos> objects;

        public MyArrayAdapter(Context context, int textViewRecursoId, ArrayList<Contactos> objects) {
            super(context, textViewRecursoId, objects);
            this.context = context;
            this.textViewRecursoId = textViewRecursoId;
            this.objects = objects;
        }

        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(this.textViewRecursoId, null);
            TextView lblNombre = (TextView) view.findViewById(R.id.lblNombreContacto);
            TextView lblTelefono = (TextView) view.findViewById(R.id.lblTelefonoContacto);
            Button btnModificar = (Button) view.findViewById(R.id.btnModificar);
            Button btnBorrar = (Button) view.findViewById(R.id.btnBorrar);

            if (objects.get(position).getFavorite()>0) {
                lblNombre.setTextColor(Color.BLUE);
                lblTelefono.setTextColor(Color.BLUE);
            } else {
                lblNombre.setTextColor(Color.BLACK);
                lblTelefono.setTextColor(Color.BLACK);
            }

            lblNombre.setText(objects.get(position).getNombre());
            lblTelefono.setText(objects.get(position).getTelefono1());

            btnBorrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder confirmar = new AlertDialog.Builder(ListaActivity.this);
                    confirmar.setTitle("Borrar");
                    confirmar.setMessage("¿Seguro que quiere borrar este contacto?");
                    confirmar.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            borrarContacto(objects.get(position).get_ID());
                            objects.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show();
                        }
                    });
                    confirmar.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    confirmar.show();
                }
            });

            btnModificar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle oBundle = new Bundle();
                    oBundle.putSerializable("contacto", objects.get(position));
                    Intent i = new Intent();
                    i.putExtras(oBundle);
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
            });
            return view;
        }
    }

    public void borrarContacto(String childIndex) {
        referencia.child(String.valueOf(childIndex)).removeValue();
    }
}