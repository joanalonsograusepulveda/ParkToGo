package com.parktogo.ui;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parktogo.R;
import com.parktogo.databinding.DialogViewBinding;
import com.parktogo.databinding.FragmentActualizarPerfilBinding;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ActualizarPerfilFragment extends Fragment {

    private FragmentActualizarPerfilBinding binding;

    private String sexo = "";
    private String genero = "";
    private int comprobar = 0;
    private String imagen;
    private String nacimiento;
    private AwesomeValidation awesomeValidation;
    private String edadString;
    private String resultUid, resultPass, resultFecha;
    private boolean comprobarNacimiento = false;

    private DatePickerDialog.OnDateSetListener setListener;
    private ProgressDialog mDialog;
    private DialogViewBinding bindingDialog;

    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference BD_USUARIOS;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Bitmap thumb_bitmap = null;
    private byte[] thumb_byte;
    private Uri downloadUri = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentActualizarPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDialog = new ProgressDialog(getContext());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        BD_USUARIOS = firebaseDatabase.getReference("BD Usuarios");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Foto perfil subidas");
        storageReference = FirebaseStorage.getInstance().getReference().child("img_comprimidas");

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(getActivity(), R.id.txtActualizaNombre, "[a-zA-Z]+", R.string.invalid_name);

        consulta();

        binding.imageViewActualizarFoto.setOnClickListener(v -> {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setRequestedSize(480, 480)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        });

        binding.btnBackButtonActualizar.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_actualizarPerfilFragment_to_navigation_settings);
        });

        binding.rbPersonalizadoActualizar.setOnClickListener(v -> {
            binding.rbhombreActualizado.setChecked(false);
            binding.rbMujerActualizado.setChecked(false);
            binding.rbNoContestarActualizado.setChecked(false);

            binding.txtGeneroPersonalizadoActualizado.setEnabled(true);
            binding.txtGeneroPersonalizadoActualizado.setVisibility(View.VISIBLE);
            sexo = binding.rbPersonalizadoActualizar.getText().toString();
            comprobar = 0;
            comprobar = 4;
        });

        binding.rbhombreActualizado.setOnClickListener(v -> {
            binding.rbPersonalizadoActualizar.setChecked(false);
            binding.txtGeneroPersonalizadoActualizado.setEnabled(false);
            binding.txtGeneroPersonalizadoActualizado.setVisibility(View.INVISIBLE);
            sexo = binding.rbhombreActualizado.getText().toString();
            genero = binding.rbhombreActualizado.getText().toString();
            comprobar = 0;
            comprobar = 1;
        });

        binding.rbMujerActualizado.setOnClickListener(v -> {
            binding.rbPersonalizadoActualizar.setChecked(false);
            binding.txtGeneroPersonalizadoActualizado.setEnabled(false);
            binding.txtGeneroPersonalizadoActualizado.setVisibility(View.INVISIBLE);
            sexo = binding.rbMujerActualizado.getText().toString();
            sexo = binding.rbMujerActualizado.getText().toString();
            genero = binding.rbMujerActualizado.getText().toString();
            comprobar = 0;
            comprobar = 2;
        });

        binding.rbNoContestarActualizado.setOnClickListener(v -> {
            binding.rbPersonalizadoActualizar.setChecked(false);
            binding.txtGeneroPersonalizadoActualizado.setEnabled(false);
            binding.txtGeneroPersonalizadoActualizado.setVisibility(View.INVISIBLE);
            sexo = binding.rbNoContestarActualizado.getText().toString();
            genero = "?";
            comprobar = 0;
            comprobar = 3;
        });

        binding.btnFechaActualizar.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog_NoActionBar,
                            setListener, year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
            comprobarNacimiento = true;
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {

                month = month + 1;
                String date = day + "/" + month + "/" + year;
                binding.txtNacimientoActualizar.getEditText().setText(date);
                nacimiento = date;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    LocalDate today = LocalDate.now();
                    LocalDate birthdate = LocalDate.of(year, month, day);
                    Period p = Period.between(birthdate, today);
                    if (p.getYears() > 1) {
                        edadString = p.getYears() + " Años";
                    } else {
                        edadString = p.getYears() + " Año";
                    }
                }
            }
        };

        binding.btnActualizarPerfil.setOnClickListener(v -> {
            mDialog.setMessage("Espere un momento...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            if (awesomeValidation.validate()) {
                if (!binding.txtNacimientoActualizar.getEditText().getText().toString().equals("")) {
                    if (!comprobarNacimiento) {
                        String[] corteNacimiento = nacimiento.split("/");
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            LocalDate today = LocalDate.now();
                            LocalDate birthdate = LocalDate.of(Integer.parseInt(corteNacimiento[2]), Integer.parseInt(corteNacimiento[1]), Integer.parseInt(corteNacimiento[0]));
                            Period p = Period.between(birthdate, today);
                            if (p.getYears() > 1) {
                                edadString = p.getYears() + " Años";
                            } else {
                                edadString = p.getYears() + " Año";
                            }
                        }
                        switch (comprobar) {
                            case 1:
                                Actualizar();
                                break;
                            case 2:
                                Actualizar();
                                break;
                            case 3:
                                Actualizar();
                                break;
                            case 4:
                                Actualizar4();
                                break;
                        }
                        mDialog.dismiss();
                    } else {
                        switch (comprobar) {
                            case 1:
                                Actualizar();
                                break;
                            case 2:
                                Actualizar();
                                break;
                            case 3:
                                Actualizar();
                                break;
                            case 4:
                                Actualizar4();
                                break;
                        }
                        mDialog.dismiss();
                    }
                    mDialog.dismiss();
                } else {
                    mDialog.dismiss();
                    Toast.makeText(getContext(), "Falta ingresar la fecha de nacimiento!", Toast.LENGTH_LONG).show();
                }
            } else {
                mDialog.dismiss();
            }
            mDialog.dismiss();
        });
    }

    private void Actualizar4() {
        genero = binding.txtGeneroPersonalizadoActualizado.getEditText().getText().toString();
        String nombreString = binding.txtActualizaNombre.getEditText().getText().toString();
        String emailString = binding.txtEmailActualizar.getEditText().getText().toString();

        StorageReference ref = storageReference.child("imagen" + nombreString);
        UploadTask uploadTask = ref.putBytes(thumb_byte);

        HashMap<String, Object> datosUser = new HashMap<>();

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                downloadUri = task.getResult();
                imagen = String.valueOf(task.getResult());
                databaseReference.push().child("url_foto").setValue(downloadUri.toString());
                datosUser.put("Uid", resultUid);
                datosUser.put("Email", emailString);
                datosUser.put("Pass", resultPass);
                datosUser.put("Nombre", nombreString);
                datosUser.put("Edad", edadString);
                datosUser.put("Fecha Nacimiento", nacimiento);
                datosUser.put("Fecha Cuenta", resultFecha);
                datosUser.put("Sexo", sexo);
                datosUser.put("Genero", genero);
                datosUser.put("Imagen", imagen);

                BD_USUARIOS.child(user.getUid()).updateChildren(datosUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                            }
                        });
            }
        });
        actualizado();
    }

    private void Actualizar() {
        String nombreString = binding.txtActualizaNombre.getEditText().getText().toString();
        String emailString = binding.txtEmailActualizar.getEditText().getText().toString();

        StorageReference ref = storageReference.child("imagen" + nombreString);
        UploadTask uploadTask = ref.putBytes(thumb_byte);

        HashMap<String, Object> datosUser = new HashMap<>();

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                downloadUri = task.getResult();
                imagen = String.valueOf(task.getResult());
                databaseReference.push().child("url_foto").setValue(downloadUri.toString());
                datosUser.put("Uid", resultUid);
                datosUser.put("Email", emailString);
                datosUser.put("Pass", resultPass);
                datosUser.put("Nombre", nombreString);
                datosUser.put("Edad", edadString);
                datosUser.put("Fecha Nacimiento", nacimiento);
                datosUser.put("Fecha Cuenta", resultFecha);
                datosUser.put("Sexo", sexo);
                datosUser.put("Genero", genero);
                datosUser.put("Imagen", imagen);

                BD_USUARIOS.child(user.getUid()).updateChildren(datosUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                            }
                        });
            }
        });
        actualizado();
    }

    private void consulta() {
        Query query = BD_USUARIOS.orderByChild("Email").equalTo(user.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = "" + ds.child("Uid").getValue();
                    String nickName = "" + ds.child("Nombre").getValue();
                    String email = "" + ds.child("Email").getValue();
                    String pass = "" + ds.child("Pass").getValue();
                    String edad = "" + ds.child("Edad").getValue();
                    String sexo2 = "" + ds.child("Sexo").getValue();
                    String genero2 = "" + ds.child("Genero").getValue();
                    String fecha = "" + ds.child("Fecha Cuenta").getValue();
                    nacimiento = "" + ds.child("Fecha Nacimiento").getValue();
                    String imagen = "" + ds.child("Imagen").getValue();

                    binding.txtActualizaNombre.getEditText().setText(nickName);
                    binding.txtNacimientoActualizar.getEditText().setText(edad);
                    binding.txtNacimientoActualizar.getEditText().setText(nacimiento);
                    binding.txtEmailActualizar.getEditText().setText(email);

                    switch (sexo2) {
                        case "Hombre":
                            binding.rbhombreActualizado.setChecked(true);
                            genero = genero2;
                            comprobar = 0;
                            comprobar = 1;
                            break;
                        case "Mujer":
                            binding.rbMujerActualizado.setChecked(true);
                            genero = genero2;
                            comprobar = 0;
                            comprobar = 2;
                            break;
                        case "Prefiero no contestar":
                            binding.rbNoContestarActualizado.setChecked(true);
                            genero = genero2;
                            comprobar = 0;
                            comprobar = 3;
                            break;
                        case "Personalizado":
                            binding.rbPersonalizadoActualizar.setChecked(true);
                            binding.txtGeneroPersonalizadoActualizado.getEditText().setText(genero2);
                            binding.txtGeneroPersonalizadoActualizado.setEnabled(true);
                            binding.txtGeneroPersonalizadoActualizado.setVisibility(View.VISIBLE);
                            genero = genero2;
                            comprobar = 0;
                            comprobar = 4;
                            break;
                    }

                    resultUid = uid;
                    resultPass = pass;
                    resultFecha = fecha;

                    Picasso.get()
                            .load(imagen)
                            .transform(new CropCircleTransformation())
                            .into(binding.imageViewActualizarFoto);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File url = new File(resultUri.getPath());

                Picasso.get()
                        .load(url)
                        .transform(new CropCircleTransformation())
                        .into(binding.imageViewActualizarFoto);

                try {
                    thumb_bitmap = new Compressor(getContext())
                            .setMaxWidth(480)
                            .setMaxHeight(480)
                            .setQuality(90)
                            .compressToBitmap(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                thumb_byte = byteArrayOutputStream.toByteArray();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void actualizado() {
        View v = View.inflate(getContext(), R.layout.dialog_actualizado_view, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);

        AlertDialog alertDialog = builder.create();
        mDialog.dismiss();
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);

        bindingDialog = DialogViewBinding.bind(v);
        bindingDialog.btnOkDialog.setOnClickListener(v1 -> {
            consulta();
            alertDialog.dismiss();
        });
    }
}