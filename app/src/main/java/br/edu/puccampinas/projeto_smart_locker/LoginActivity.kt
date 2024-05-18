package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    // Configuração do ViewBinding
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    // Configuração do FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Inicialização de uma instancia de NetworkChecker para verificar a conectividad de rede.
    private val networkChecker by lazy {
        NetworkChecker(
            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
                ?: throw IllegalStateException("ConnectivityManager not available")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            arrow.setOnClickListener { finish() }
            btMap.setOnClickListener {
                startActivity(Intent(this@LoginActivity, MapActivity::class.java))
            }
            btLogin.setOnClickListener {
                // Chama a função para validar o usuário ao clicar no botão de login
                validUser(editUsuario.text.toString(), editSenha.text.toString())
            }
            txtEsqueceuSenha.setOnClickListener {
                // Chama a função para redefinir a senha ao clicar no texto "Esqueceu sua senha?"
                forgotPassword(editUsuario.text.toString())
            }
        }
        // Configura o cursor para começar no início do campo de usuário ao receber o foco
        setCursorToStartOnFocusChange(binding.editUsuario)
        // Configura o botão de alternar visibilidade da senha
        setupPasswordToggle(
            binding.togglePasswordVisibility,
            binding.editSenha,
            InputType.TYPE_CLASS_TEXT
        )
    }
    // Função para enviar e-mail de redefinição de senha
    private fun forgotPassword(email: String) {
        if (email.isBlank()) {   // Verifica se o campo de e-mail está em branco
            Toast.makeText(this, "Digite um email para a recuperação de senha!", Toast.LENGTH_LONG).show()
            return
        }
        // Envia um e-mail de redefinição de senha para o endereço fornecido
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Se a operação for bem-sucedida, redireciona para a tela de redefinição de senha
                startActivity(Intent(this, ForgetActivity::class.java).putExtra("email", email))
                finish()
            } else {
                // Se a operação falhar, exibe uma mensagem de erro
                Toast.makeText(this, "Endereço de email inválido!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Função que altera a visibilidade da senha ao alternar o toggle
    private fun setupPasswordToggle(
        toggleButton: ToggleButton,
        editText: EditText,
        inputType: Int
    ) {
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se o botão está marcado, mostrar a senha
                editText.inputType = inputType or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Se o botão não está marcado, ocultar a senha
                editText.inputType = inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Para atualizar a exibição do EditText
            editText.text?.let { editText.setSelection(it.length) }
        }
    }

    // Função para voltar o cursor no incício do input
    private fun setCursorToStartOnFocusChange(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }

    private fun validUser(email: String, password: String) {

        if (networkChecker.hasInternet()) {
            // Verifica se o email ou a senha estão em branco
            if (email.isBlank() or password.isBlank()) return
            // Tenta fazer login com o email e a senha fornecidos
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    // Verifica se o email do usuário foi verificado
                    if (authResult.user?.isEmailVerified == false) {
                        // Se não estiver verificado, faz logout e exibe mensagem para o usuário
                        auth.signOut()
                        Toast.makeText(
                            this,
                            "Por favor, ative a conta através do link enviado no email e tente novamente!",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }
                    // Se o login for bem-sucedido e o email estiver verificado, redireciona para a tela principal do cliente
                    FirebaseFirestore.getInstance()
                        .collection("Pessoas")
                        .document(authResult.user?.uid.toString())
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e("Erro no Firebase Firestore", error.message.toString())
                            }
                            if (snapshot != null && snapshot.exists()) {
                                if (snapshot.get("gerente").toString() == "true") {
                                    startActivity(Intent(this, ManagerMainScreenActivity::class.java))
                                } else {
                                    startActivity(Intent(this, ClientMainScreenActivity::class.java))
                                }
                                finish()
                            }
                        }
                }.addOnFailureListener { exception ->
                    // Trata falhas durante o processo de login
                    if (exception.message.toString() == "The email address is badly formatted.") {
                        // Verifica se o formato do email está incorreto
                        Toast.makeText(
                            this,
                            "Endereço de email inválido, por favor digite novamente!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Se a falha não for relacionada ao formato do email, exibe mensagem de erro genérica
                        Toast.makeText(
                            this,
                            "Email ou senha incorretos, por favor digite novamente!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            startActivity(Intent(this, NetworkErrorActivity::class.java))
        }
    }
}