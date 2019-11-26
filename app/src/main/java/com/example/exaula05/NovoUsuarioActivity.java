package com.example.exaula05;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class NovoUsuarioActivity extends AppCompatActivity {

    private EditText loginNovoUsuarioEditText;
    private EditText senhaNovoUsuarioEditText;
    private FirebaseAuth mAuth;

    private void setupViews (){
        loginNovoUsuarioEditText =
                findViewById(R.id.loginNovoUsuarioEditText);
        senhaNovoUsuarioEditText =
                findViewById(R.id.senhaNovoUsuarioEditText);
    }

    private void setupFirebase (){
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_usuario);
        setupViews();
        setupFirebase();
    }

    public void criarNovoUsuario(View view) {
        String email = loginNovoUsuarioEditText.getText().toString();
        String senha = senhaNovoUsuarioEditText.getText().toString();
        mAuth.createUserWithEmailAndPassword(
                email,
                senha
        ).addOnSuccessListener((result) -> {
            Toast.makeText(
                    this,
                    getString(R.string.usuario_criado),
                    Toast.LENGTH_SHORT
            ).show();
            finish();
        }).addOnFailureListener((exception) ->{
            Toast.makeText(
                    this,
                    getString(R.string.usuario_nao_criado),
                    Toast.LENGTH_SHORT
            ).show();
        });

    }


}
