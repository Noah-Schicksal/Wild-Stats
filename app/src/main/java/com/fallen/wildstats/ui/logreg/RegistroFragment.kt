package com.fallen.wildstats.ui.logreg

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.fallen.wildstats.MainActivity
import com.fallen.wildstats.R
import com.fallen.wildstats.ui.utils.exibirMensagem
import com.fallen.wildstats.ui.utils.id_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegistroFragment : Fragment() {

    private lateinit var email: String
    private lateinit var senha: String
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val edtEmail = view.findViewById<EditText>(R.id.hint_email_cadastro)
        val edtSenha = view.findViewById<EditText>(R.id.hint_senha_cadastro)
        val btnRegistrar = view.findViewById<Button>(R.id.button_registro)
        val btnGoogle = view.findViewById<Button>(R.id.button_google)

        // Cadastro com email e senha
        btnRegistrar.setOnClickListener {
            email = edtEmail.text.toString().trim()
            senha = edtSenha.text.toString().trim()
            if (validarCampos(edtEmail, edtSenha)) {
                mostrarProgress("Registrando usuário...")

                firebaseAuth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        fecharProgress()
                        if (task.isSuccessful) {
                            val idUsuario = task.result?.user?.uid
                            idUsuario?.let {
                                val usuario = id_user(it, email)
                                salvarIdFirestore(usuario)
                            }
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                        } else {
                            tratarErro(task.exception)
                        }
                    }
            }
        }

        // Configura Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Botão de login com Google
        btnGoogle.setOnClickListener {
            mostrarProgress("Registrando usuário...")
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!, account.email ?: "")
            } catch (e: ApiException) {
                fecharProgress()
                exibirMensagem("Falha no login com Google: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, emailGoogle: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idUsuario = task.result?.user?.uid
                    idUsuario?.let {
                        val usuario = id_user(it, emailGoogle)
                        salvarIdFirestore(usuario) // <-- Agora salva no Firestore também
                    }
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                } else {
                    exibirMensagem("Falha na autenticação com Google")
                }
                fecharProgress()
            }
    }

    private fun salvarIdFirestore(usuario: id_user) {
        Log.d("RegistroFragment", "Iniciando salvamento no Firestore para o usuário: ${usuario.id}")
        firestore.collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                Log.d("RegistroFragment", "Usuário ${usuario.id} salvo com sucesso no Firestore.")
                exibirMensagem("Cadastro completo com sucesso!")
            }
            .addOnFailureListener { e ->
                Log.e("RegistroFragment", "Erro ao salvar usuário ${usuario.id} no Firestore: ${e.message}", e)
                exibirMensagem("Erro ao salvar dados: ${e.message}")
            }
    }

    private fun validarCampos(edtEmail: EditText, edtSenha: EditText): Boolean {
        var valido = true
        if (email.isEmpty()) {
            edtEmail.error = "O email é obrigatório"
            valido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.error = "Digite um email válido"
            valido = false
        } else {
            edtEmail.error = null
        }

        if (senha.isEmpty()) {
            edtSenha.error = "A senha é obrigatória"
            valido = false
        } else if (senha.length < 6) {
            edtSenha.error = "A senha deve ter no mínimo 6 caracteres"
            valido = false
        } else {
            edtSenha.error = null
        }

        return valido
    }

    private fun tratarErro(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                exibirMensagem("E-mail inválido, tente novamente")
            }
            is FirebaseAuthUserCollisionException -> {
                exibirMensagem("Esse e-mail já está sendo usado")
            }
            else -> {
                exibirMensagem("Erro desconhecido: ${exception?.message}")
            }
        }
    }

    private fun mostrarProgress(mensagem: String) {
        progressDialog = ProgressDialog(context).apply {
            setMessage(mensagem)
            setCancelable(false)
            show()
        }
    }

    private fun fecharProgress() {
        progressDialog?.dismiss()
    }
}
