package com.parktogo.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parktogo.MainActivity;
import com.parktogo.R;
import com.parktogo.databinding.DialogExitViewBinding;
import com.parktogo.databinding.DialogViewBinding;
import com.parktogo.databinding.FragmentActualizarPasswordBinding;

import java.util.HashMap;

public class ActualizarPasswordFragment extends Fragment {

    private FragmentActualizarPasswordBinding binding;

    private ProgressDialog mDialog;

    private DatabaseReference BD_USUARIOS;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;

    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("754225252087-hrg8cpteq528k2c7r573v2oh4hbe34lu.apps.googleusercontent.com")
            .build();

    private GoogleSignInClient mGoogleSignInClient;
    private DialogExitViewBinding bindingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentActualizarPasswordBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mDialog = new ProgressDialog(getContext());

        BD_USUARIOS = FirebaseDatabase.getInstance().getReference("BD Usuarios");
        firebaseAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        binding.btnBackButtonActualizarPassword.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_actualizarPasswordFragment_to_navigation_settings);
        });

        binding.btnActualizarPassword.setOnClickListener(v ->  {
            String actual = binding.txtActualPasswordActualizar.getEditText().getText().toString().trim();
            String nueva = binding.txtNuevaPasswordActualizar.getEditText().getText().toString().trim();

            if (TextUtils.isEmpty(actual)){
                Toast.makeText(getContext(), "Falta rellenar la contraseña actual",Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(nueva)){
                Toast.makeText(getContext(), "Falta rellenar la contraseña nueva",Toast.LENGTH_SHORT).show();
            }

            if (!TextUtils.isEmpty(actual) && !TextUtils.isEmpty(nueva) && actual.length() >=9 && nueva.length() >=9){
                cambioPassUsuario(actual,nueva);
            }else{
                Toast.makeText(getContext(),"La contraseña tiene que tener un minimo de 9 digitos",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cambioPassUsuario(String actual, String nueva) {
        AuthCredential authCredential = EmailAuthProvider.getCredential((user.getEmail()), actual);

        mDialog.setMessage("Espere un momento...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user.updatePassword(nueva)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        String value = binding.txtNuevaPasswordActualizar.getEditText().getText().toString().trim();
                                        HashMap<String, Object> result = new HashMap<>();
                                        result.put("Pass", value);
                                        BD_USUARIOS.child(user.getUid()).updateChildren(result)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mDialog.dismiss();
                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        View v = View.inflate(getContext(), R.layout.dialog_exit_password_view, null);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setView(v);

                                        AlertDialog alertDialog = builder.create();
                                        mDialog.dismiss();
                                        alertDialog.show();
                                        alertDialog.setCancelable(false);
                                        alertDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);

                                        bindingDialog = DialogExitViewBinding.bind(v);
                                        bindingDialog.btnSalirExit.setOnClickListener(v1 -> {
                                            FirebaseAuth.getInstance().signOut();
                                            signOut();
                                            alertDialog.dismiss();
                                            SharedPreferences sharedPref = getActivity().getSharedPreferences("MySharedPref",Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putBoolean("Sesion",false);
                                            editor.apply();
                                            Toast.makeText(getContext(), "Se ha cerrado la sesion",Toast.LENGTH_SHORT).show();
                                            getActivity().finish();
                                            Intent intent = new Intent(getContext(), MainActivity.class);
                                            startActivity(intent);
                                        });
                                        bindingDialog.btnNoExit.setOnClickListener(v1 -> {
                                            binding.txtActualPasswordActualizar.getEditText().setText("");
                                            binding.txtNuevaPasswordActualizar.getEditText().setText("");
                                            alertDialog.dismiss();
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mDialog.dismiss();
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut() {
        mGoogleSignInClient.signOut();
    }
}