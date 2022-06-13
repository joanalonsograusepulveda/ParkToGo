package com.parktogo.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.parktogo.R;
import com.parktogo.databinding.FragmentPerfilBinding;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;

    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference BD_USUARIOS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        BD_USUARIOS = firebaseDatabase.getReference("BD Usuarios");
        Consulta();
    }

    private void Consulta(){
        Query query = BD_USUARIOS.orderByChild("Email").equalTo(user.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String nickName = ""+ds.child("Nombre").getValue();
                    String email = ""+ds.child("Email").getValue();
                    String edad = ""+ds.child("Edad").getValue();
                    String sexo = ""+ds.child("Genero").getValue();
                    String fecha = ""+ds.child("Fecha Cuenta").getValue();
                    String imagen = ""+ds.child("Imagen").getValue();

                    binding.txtNombrePerfil.setText(nickName);
                    binding.txtFechaPerfil.setText(fecha);
                    binding.txtCorreoPerfil.setText(email);
                    binding.txtEdadPerfil.setText(edad);
                    binding.txtSexoPerfil.setText(sexo);

                    Picasso.get()
                            .load(imagen)
                            .transform(new CropCircleTransformation())
                            .into(binding.imageViewFotoPerfil);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}