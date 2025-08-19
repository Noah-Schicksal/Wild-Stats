package com.fallen.wildstats.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fallen.wildstats.MainActivity
import com.fallen.wildstats.R
import com.fallen.wildstats.ui.utils.exibirMensagem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private lateinit var email: String
    private lateinit var senha: String

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 1001
        private const val TAG = "LoginFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // IDs do XML - substitua pelos seus se necessário
        val edtEmailLogin = view.findViewById<TextView>(R.id.hint_login_email)
        val edtSenhaLogin = view.findViewById<TextView>(R.id.hint_login_senha)
        val btnLogin = view.findViewById<Button>(R.id.button_login)
        val btnLoginGoogle = view.findViewById<Button>(R.id.button_login_google)
        val txtCadastrar = view.findViewById<TextView>(R.id.textView_cadastro)

        // Configuração do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Login com e-mail/senha
        btnLogin.setOnClickListener {
            email = edtEmailLogin.text.toString().trim()
            senha = edtSenhaLogin.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                exibirMensagem("Preencha todos os campos")
                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(context).apply {
                setMessage("Fazendo login...")
                setCancelable(false)
                show()
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        val exception = task.exception
                        when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                exibirMensagem("E-mail ou senha incorretos")
                            }
                            is FirebaseAuthInvalidUserException -> {
                                exibirMensagem("Usuário não encontrado. Verifique o email ou faça cadastro.")
                            }
                            else -> {
                                exibirMensagem("Erro ao fazer login: ${exception?.message}")
                            }
                        }
                    }
                }
        }

        // Login com Google
        btnLoginGoogle.setOnClickListener {
            val progressDialog = ProgressDialog(context).apply {
                setMessage("Fazendo login com Google...")
                setCancelable(false)
                show()
            }

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
            // O ProgressDialog será fechado no retorno do Google Sign-In
        }

        // Navegar para registro
        txtCadastrar.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                exibirMensagem("Falha no login com Google: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val progressDialog = ProgressDialog(context).apply {
            setMessage("Finalizando login com Google...")
            setCancelable(false)
            show()
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            exibirMensagem("Falha na autenticação com Google: token inválido.")
                        }
                        else -> {
                            exibirMensagem("Erro ao autenticar com Google: ${exception?.message}")
                        }
                    }
                }
            }
    }
}
