package com.parktogo.ui;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.parktogo.R;
import com.parktogo.databinding.DialogViewBinding;
import com.parktogo.databinding.FragmentPasswordBinding;

public class PasswordFragment extends Fragment {

    private FragmentPasswordBinding binding;
    private DialogViewBinding bindingDialog;
    private String email = "";
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(getContext());

        binding.btnBackButtonPassword.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_passwordFragment_to_loginFragment);
        });

        binding.btnLoginEnviar.setOnClickListener(view1 -> {

            email = binding.txtEmailPassword.getEditText().getText().toString();

            if(!email.isEmpty()){
                mDialog.setMessage("Espere un momento...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                resetPassword(view,view1);
            }else{
                Toast.makeText(getContext(),"Debe ingresar el email",Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void resetPassword(View view, View v) {
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    View v = View.inflate(getContext(), R.layout.dialog_view, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setView(v);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.setCancelable(false);
                    alertDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);

                    bindingDialog = DialogViewBinding.bind(v);
                    bindingDialog.btnOkDialog.setOnClickListener(v1 -> {
                        alertDialog.dismiss();
                        Navigation.findNavController(view).navigate(R.id.action_passwordFragment_to_loginFragment);
                    });
                }else{
                    Toast.makeText(getContext(),"No se ha enviado el correo",Toast.LENGTH_SHORT).show();
                }

                mDialog.dismiss();
            }
        });
    }
}