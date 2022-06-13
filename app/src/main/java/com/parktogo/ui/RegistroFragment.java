package com.parktogo.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parktogo.R;
import com.parktogo.databinding.DialogViewBinding;
import com.parktogo.databinding.FragmentRegistroBinding;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegistroFragment extends Fragment {

    FragmentRegistroBinding binding;
    DialogViewBinding bindingDialog;
    FirebaseAuth firebaseAuth;
    AwesomeValidation awesomeValidation;
    private ProgressDialog mDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegistroBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDialog = new ProgressDialog(getContext());
        firebaseAuth = FirebaseAuth.getInstance();

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(getActivity(),R.id.txtEmailRegistro, Patterns.EMAIL_ADDRESS,R.string.invalid_mail);
        awesomeValidation.addValidation(getActivity(),R.id.txtPasswordRegistro,".{9,}",R.string.invalid_password);

        binding.btnBackButtonRegistro.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_registroFragment_to_loginFragment);
        });

        binding.btnCrearRegistro.setOnClickListener(v -> {

            String mail = binding.txtEmailRegistro.getEditText().getText().toString();
            String pass = binding.txtPasswordRegistro.getEditText().getText().toString();

            mDialog.setMessage("Espere un momento...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            if(awesomeValidation.validate()){
                firebaseAuth.createUserWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            assert  user!= null;

                            String uidString = user.getUid();
                            String emailString = binding.txtEmailRegistro.getEditText().getText().toString();
                            String passString = binding.txtPasswordRegistro.getEditText().getText().toString();

                            View v = View.inflate(getContext(), R.layout.dialog_registro_view, null);

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setView(v);

                            AlertDialog alertDialog = builder.create();
                            mDialog.dismiss();
                            alertDialog.show();
                            alertDialog.setCancelable(false);
                            alertDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);

                            bindingDialog = DialogViewBinding.bind(v);
                            bindingDialog.btnOkDialog.setOnClickListener(v1 -> {
                                alertDialog.dismiss();
                                firebaseAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()){
                                            Bundle bundle = new Bundle();
                                            bundle.putString("bundleKeyUid",uidString);
                                            bundle.putString("bundleKeyEmail",emailString);
                                            bundle.putString("bundleKeyPass",passString);
                                            getParentFragmentManager().setFragmentResult("requestKey", bundle);
                                            mDialog.dismiss();
                                            Navigation.findNavController(view).navigate(R.id.action_registroFragment_to_crearPerfilFragment);
                                        }else{
                                            mDialog.dismiss();
                                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                            dameToastdeerror(errorCode);
                                        }
                                    }
                                });
                            });
                        }else {
                            mDialog.dismiss();
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            dameToastdeerror(errorCode);
                        }
                    }
                });
            }else {
                mDialog.dismiss();
                Toast.makeText(getContext(), "Rellena todos los datos..!!", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void dameToastdeerror(String error) {

        switch (error) {

            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(getContext(), "El formato del token personalizado es incorrecto. Por favor revise la documentación", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(getContext(), "El token personalizado corresponde a una audiencia diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(getContext(), "La credencial de autenticación proporcionada tiene un formato incorrecto o ha caducado.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_EMAIL":
                Toast.makeText(getContext(), "La dirección de correo electrónico está mal formateada.", Toast.LENGTH_LONG).show();
                binding.txtEmailRegistro.setError("La dirección de correo electrónico está mal formateada.");
                binding.txtEmailRegistro.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(getContext(), "La contraseña no es válida o el usuario no tiene contraseña.", Toast.LENGTH_LONG).show();
                binding.txtPasswordRegistro.setError("la contraseña es incorrecta ");
                binding.txtPasswordRegistro.requestFocus();
                break;

            case "ERROR_USER_MISMATCH":
                Toast.makeText(getContext(), "Las credenciales proporcionadas no corresponden al usuario que inició sesión anteriormente..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Toast.makeText(getContext(),"Esta operación es sensible y requiere autenticación reciente. Inicie sesión nuevamente antes de volver a intentar esta solicitud.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Toast.makeText(getContext(), "Ya existe una cuenta con la misma dirección de correo electrónico pero diferentes credenciales de inicio de sesión. Inicie sesión con un proveedor asociado a esta dirección de correo electrónico.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(getContext(), "La dirección de correo electrónico ya está siendo utilizada por otra cuenta..   ", Toast.LENGTH_LONG).show();
                binding.txtEmailRegistro.setError("La dirección de correo electrónico ya está siendo utilizada por otra cuenta.");
                binding.txtEmailRegistro.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(getContext(), "Esta credencial ya está asociada con una cuenta de usuario diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(getContext(), "La cuenta de usuario ha sido inhabilitada por un administrador..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Toast.makeText(getContext(), "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_NOT_FOUND":
                Toast.makeText(getContext(), "No hay ningún registro de usuario que corresponda a este identificador. Es posible que se haya eliminado al usuario.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(getContext(), "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(getContext(), "Esta operación no está permitida. Debes habilitar este servicio en la consola.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(getContext(), "La contraseña proporcionada no es válida..", Toast.LENGTH_LONG).show();
                binding.txtPasswordRegistro.setError("La contraseña no es válida, debe tener al menos 6 caracteres");
                binding.txtPasswordRegistro.requestFocus();
                break;

        }

    }
}