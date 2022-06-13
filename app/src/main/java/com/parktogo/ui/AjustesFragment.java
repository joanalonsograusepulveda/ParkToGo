package com.parktogo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.parktogo.MainActivity;
import com.parktogo.R;
import com.parktogo.databinding.DialogExitViewBinding;
import com.parktogo.databinding.DialogViewBinding;
import com.parktogo.databinding.FragmentAjustesBinding;

public class AjustesFragment extends Fragment {

    private FragmentAjustesBinding binding;
    private DialogExitViewBinding bindingDialog;

    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("754225252087-hrg8cpteq528k2c7r573v2oh4hbe34lu.apps.googleusercontent.com")
            .build();

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAjustesBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        binding.btnDesconectarAjustes.setOnClickListener(v -> {
            v = View.inflate(getContext(), R.layout.dialog_exit_view, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(v);

            AlertDialog alertDialog = builder.create();
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
                alertDialog.dismiss();
            });
        });

        binding.btnActualizarAjustes.setOnClickListener(v1 -> {
            Navigation.findNavController(view).navigate(R.id.action_navigation_settings_to_actualizarPerfilFragment);
        });

        binding.btnCambiarConstrasenyaAjustes.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_navigation_settings_to_actualizarPasswordFragment);
        });
    }

    private void signOut() {
        mGoogleSignInClient.signOut();
    }
}