package com.parktogo.ui;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parktogo.R;
import com.parktogo.databinding.FragmentCrearPerfilBinding;
import com.parktogo.databinding.FragmentRegistroBinding;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class CrearPerfilFragment extends Fragment {

    private AwesomeValidation awesomeValidation;

    private String sexo = "";
    private String genero = "";
    private int comprobar = 0;
    private String imagen;
    private String resultUid, resultEmail, resultPass;
    private String edadString;
    private Uri downloadUri = null;

    private final Date date = new Date();
    private FirebaseAuth firebaseAuth;
    private SimpleDateFormat fecha = new SimpleDateFormat("d 'de' MMMM 'del' yyyy");
    private String stringFecha = fecha.format(date);

    private DatePickerDialog.OnDateSetListener setListener;

    private FragmentCrearPerfilBinding binding;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Bitmap thumb_bitmap = null;
    private byte[] thumb_byte;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCrearPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                resultUid = result.getString("bundleKeyUid");
                resultEmail = result.getString("bundleKeyEmail");
                resultPass = result.getString("bundleKeyPass");
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Foto perfil subidas");
        storageReference = FirebaseStorage.getInstance().getReference().child("img_comprimidas");

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(getActivity(), R.id.txtNombrePerfil, "[a-zA-Z]+", R.string.invalid_name);

        binding.txtFechaCrearPerfil.setText(stringFecha);

        binding.imageViewPerfil.setOnClickListener(v -> {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setRequestedSize(480, 480)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        });

        binding.rbPersonalizado.setOnClickListener(v -> {
            binding.rbhombre.setChecked(false);
            binding.rbMujer.setChecked(false);
            binding.rbNoContestar.setChecked(false);

            binding.txtGeneroPersonalizado.setEnabled(true);
            binding.txtGeneroPersonalizado.setVisibility(View.VISIBLE);
            sexo = binding.rbPersonalizado.getText().toString();
            comprobar = 0;
            comprobar = 4;
        });

        binding.rbhombre.setOnClickListener(v -> {
            binding.rbPersonalizado.setChecked(false);
            binding.txtGeneroPersonalizado.setEnabled(false);
            binding.txtGeneroPersonalizado.setVisibility(View.INVISIBLE);
            sexo = binding.rbhombre.getText().toString();
            genero = binding.rbhombre.getText().toString();
            comprobar = 0;
            comprobar = 1;
        });

        binding.rbMujer.setOnClickListener(v -> {
            binding.rbPersonalizado.setChecked(false);
            binding.txtGeneroPersonalizado.setEnabled(false);
            binding.txtGeneroPersonalizado.setVisibility(View.INVISIBLE);
            sexo = binding.rbMujer.getText().toString();
            genero = binding.rbMujer.getText().toString();
            comprobar = 0;
            comprobar = 2;
        });

        binding.rbNoContestar.setOnClickListener(v -> {
            binding.rbPersonalizado.setChecked(false);
            binding.txtGeneroPersonalizado.setEnabled(false);
            binding.txtGeneroPersonalizado.setVisibility(View.INVISIBLE);
            sexo = binding.rbNoContestar.getText().toString();
            genero = "?";
            comprobar = 0;
            comprobar = 3;
        });

        binding.btnFechaCrearPerfil.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog_NoActionBar,
                            setListener, year, month, day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                binding.txtNacimientoCrearPerfil.getEditText().setText(date);

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

        binding.btnCrearPerfil.setOnClickListener(v -> {
            if (awesomeValidation.validate()) {
                if (!binding.txtNacimientoCrearPerfil.getEditText().getText().toString().equals("")) {
                    if (!(thumb_byte == null)) {
                        switch (comprobar) {
                            case 1:
                                insertarUsuario();
                                break;
                            case 2:
                                insertarUsuario();
                                break;
                            case 3:
                                insertarUsuario();
                                break;
                            case 4:
                                insertarUsuario4();
                                break;
                        }
                    } else {
                        Toast.makeText(getContext(), "Falta ingresar la foto de perfil!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Falta ingresar la fecha de nacimiento!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void insertarUsuario4() {
        genero = binding.txtGeneroPersonalizado.getEditText().getText().toString();
        String nombreString = binding.txtNombrePerfil.getEditText().getText().toString();
        String fechaString = binding.txtFechaCrearPerfil.getText().toString();
        String fechaNacimiento = binding.txtNacimientoCrearPerfil.getEditText().getText().toString();

        StorageReference ref = storageReference.child("imagen_" + nombreString);
        UploadTask uploadTask = ref.putBytes(thumb_byte);

        HashMap<Object, Object> datosUser = new HashMap<>();

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
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
                datosUser.put("Email", resultEmail);
                datosUser.put("Pass", resultPass);
                datosUser.put("Nombre", nombreString);
                datosUser.put("Edad", edadString);
                datosUser.put("Fecha Nacimiento", fechaNacimiento);
                datosUser.put("Fecha Cuenta", fechaString);
                datosUser.put("Sexo", sexo);
                datosUser.put("Genero", genero);
                datosUser.put("Imagen", imagen);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("BD Usuarios");
                reference.child(resultUid).setValue(datosUser);
            }
        });

        inicioHome();
    }

    private void insertarUsuario() {
        String nombreString = binding.txtNombrePerfil.getEditText().getText().toString();
        String fechaString = binding.txtFechaCrearPerfil.getText().toString();
        String fechaNacimiento = binding.txtNacimientoCrearPerfil.getEditText().getText().toString();

        StorageReference ref = storageReference.child("imagen_" + nombreString);
        UploadTask uploadTask = ref.putBytes(thumb_byte);

        HashMap<Object, Object> datosUser = new HashMap<>();

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
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
                datosUser.put("Email", resultEmail);
                datosUser.put("Pass", resultPass);
                datosUser.put("Nombre", nombreString);
                datosUser.put("Edad", edadString);
                datosUser.put("Fecha Nacimiento", fechaNacimiento);
                datosUser.put("Fecha Cuenta", fechaString);
                datosUser.put("Sexo", sexo);
                datosUser.put("Genero", genero);
                datosUser.put("Imagen", imagen);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("BD Usuarios");
                reference.child(resultUid).setValue(datosUser);
            }
        });

        inicioHome();
    }

    private void inicioHome() {
        getActivity().finish();
        Intent intent = new Intent(getContext(), HomeActivity.class);
        startActivity(intent);
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
                        .into(binding.imageViewPerfil);

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
}