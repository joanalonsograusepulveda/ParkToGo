package com.parktogo.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.parktogo.R;
import com.parktogo.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    private AwesomeValidation awesomeValidation;

    private final static int RC_SIGN_IN = 123;

    private ProgressDialog mDialog;

    private String mail = "";
    private String pass = "";
    private String comprobar = "";

    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("754225252087-hrg8cpteq528k2c7r573v2oh4hbe34lu.apps.googleusercontent.com")
            .build();

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;

    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPrefGoogle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPrefGoogle = getActivity().getSharedPreferences("MySharedPrefGoogle",Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mDialog = new ProgressDialog(getContext());

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(getActivity(),R.id.txtEmailRegistro, Patterns.EMAIL_ADDRESS,R.string.invalid_mail);
        awesomeValidation.addValidation(getActivity(),R.id.txtPasswordRegistro,".{6,}",R.string.invalid_password);

        binding.btnLoginLogin.setOnClickListener(v -> {
            if (awesomeValidation.validate()){
                mail = binding.txtEmailLogin.getEditText().getText().toString();
                pass = binding.txtPasswordLogin.getEditText().getText().toString();
                if (!mail.isEmpty() && !pass.isEmpty()){

                    mDialog.setMessage("Espere un momento...");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    firebaseAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                mDialog.dismiss();
                                inicioHome();
                            }else {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                dameToastdeerror(errorCode);
                                mDialog.dismiss();
                            }
                        }
                    });
                }else{
                    Toast.makeText(getContext(), "Faltan rellenar los campos!", Toast.LENGTH_LONG).show();

                }
            }
        });

        binding.btnOlvidadoLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_passwordFragment);
        });

        binding.btnRegistrarseLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registroFragment);
        });
        binding.btnLoginGoogleLogin.setOnClickListener(v -> {
            signIn();
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken(), account.getEmail());
        } catch (ApiException e) {

        }
    }

    private void inicioHome() {
        user = firebaseAuth.getCurrentUser();
        assert  user!= null;

        SharedPreferences sharedPref = getActivity().getSharedPreferences("MySharedPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("Sesion",true);
        editor.apply();

        getActivity().finish();
        Intent intent = new Intent(getContext(),HomeActivity.class);
        intent.putExtra("Usuario",user);
        startActivity(intent);
    }

    private void firebaseAuthWithGoogle(String idToken, String idEmail) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (sharedPrefGoogle.getBoolean("SesionGoogle",false)) {
                        inicioHome();
                    } else {
                        crearPerfil(idEmail);
                    }
                }
            }
        });
    }

    private void crearPerfil(String idEmail) {
        user = firebaseAuth.getCurrentUser();
        assert user != null;
        String uidString = user.getUid();

        Bundle bundle = new Bundle();
        bundle.putString("bundleKeyUid", uidString);
        bundle.putString("bundleKeyEmail", idEmail);
        bundle.putString("bundleKeyPass", "");
        getParentFragmentManager().setFragmentResult("requestKey", bundle);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("MySharedPrefGoogle",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("SesionGoogle",true);
        editor.apply();

        Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_crearPerfilFragment);

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
                Toast.makeText(getContext(), "La dirección de correo electrónico está mal.", Toast.LENGTH_LONG).show();
                binding.txtEmailLogin.setError("La dirección de correo electrónico está mal.");
                binding.txtEmailLogin.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(getContext(), "La contraseña no es válida o el usuario no tiene contraseña.", Toast.LENGTH_LONG).show();
                binding.txtPasswordLogin.setError("la contraseña es incorrecta ");
                binding.txtPasswordLogin.requestFocus();
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
                binding.txtEmailLogin.setError("La dirección de correo electrónico ya está siendo utilizada por otra cuenta.");
                binding.txtEmailLogin.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(getContext(), "Esta credencial ya está asociada con una cuenta de usuario diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(getContext(), "La cuenta de usuario ha sido inhabilitada por un administrador...", Toast.LENGTH_LONG).show();
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
                binding.txtPasswordLogin.setError("La contraseña no es válida, debe tener al menos 6 caracteres");
                binding.txtPasswordLogin.requestFocus();
                break;

        }

    }
}