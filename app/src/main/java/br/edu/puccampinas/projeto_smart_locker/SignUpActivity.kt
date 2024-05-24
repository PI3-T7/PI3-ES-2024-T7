package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import java.time.LocalDate
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A classe SignUpActivity representa a tela de cadastro de um novo usuário.
 * @property binding Objeto que contém as referências aos componentes de layout desta Activity.
 * @property auth Instância do FirebaseAuth para autenticação do usuário.
 * @property database Instância do Firebase Firestore para interagir com o banco de dados em tempo real.
 * @property values Um mapa para armazenar os valores inseridos pelo usuário durante o cadastro.
 * @property networkChecker Instância de NetworkChecker para verificar a conectividade de rede.
 * @authors: Isabella, Marcos e Lais.
 */
@RequiresApi(Build.VERSION_CODES.O)
class SignUpActivity : AppCompatActivity() {
    // Configuração do ViewBinding
    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    // Configuração do FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Configuração do Firebase Firestore
    private val database by lazy { FirebaseFirestore.getInstance() }

    // Criação do mapa para guardar os valores para cadastro
    private val values = mutableMapOf<String, String>()

    // Inicialização de uma instancia de NetworkChecker para verificar a conectividade de rede.
    private val networkChecker by lazy {
        NetworkChecker(
            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
                ?: throw IllegalStateException("ConnectivityManager not available")
        )
    }
    /**
     * Método onCreate é chamado quando a Activity é criada.
     * @authors: Isabella.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            arrow.setOnClickListener { finish() }
            btSignup.setOnClickListener { validData() }

            setCursorToStartOnFocusChange(editName)
            setCursorToStartOnFocusChange(editCpf)
            setCursorToStartOnFocusChange(editEmail)

            togglePasswordVisibility.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Se o botão está marcado, mostrar a senha
                    editPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    // Se o botão não está marcado, ocultar a senha
                    editPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                // Para atualizar a exibição do EditText
                editPassword.text?.let { editPassword.setSelection(it.length) }
            }

            togglePasswordVisibility2.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Se o botão está marcado, mostrar a senha
                    editConfirmPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    // Se o botão não está marcado, ocultar a senha
                    editConfirmPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                // Para atualizar a exibição do EditText
                editConfirmPassword.text?.let { editConfirmPassword.setSelection(it.length) }
            }

            setupPasswordToggle(
                togglePasswordVisibility,
                editPassword,
                InputType.TYPE_CLASS_TEXT
            )

            setupPasswordToggle(
                togglePasswordVisibility2,
                editConfirmPassword,
                InputType.TYPE_CLASS_TEXT
            )
        }
    }
    /**
     * Função para alterar o estado do toggle das senhas.
     * @authors: Isabella.
     */
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

    /**
     * Define o comportamento de colocar o cursor no início do texto quando o editText sai de foco.
     * @param editText O EditText para o qual o comportamento será aplicado.
     * @authors: Isabella.
     */
    private fun setCursorToStartOnFocusChange(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0)
            }
        }
    }

    /**
     * Valida os dados inseridos pelo usuário antes de realizar o cadastro.
     * Extrai os valores dos campos de entrada de texto e realiza as seguintes validações:
     *  - Verifica se algum campo está em branco e exibe um alerta caso positivo.
     *  - Valida o CPF, a data de nascimento, o número de telefone, a senha, a confirmação de senha e o email.
     *  - Atualiza o estado visual dos campos de entrada de texto com base nas validações.
     *  - Se todas as validações passarem, chama a função `registerUser()` para realizar o cadastro do usuário.
     *  @authors: Isabella.
     */
    private fun validData() {
        with(binding) {
            values["nome_completo"] = editName.text.toString()
            values["cpf"] = editCpf.masked
            values["data_de_nascimento"] = editBirthDate.masked
            values["celular"] = editPhone.masked
            values["email"] = editEmail.text.toString()
            values["senha"] = editPassword.text.toString().replace(" ", "")
            values["senha2"] = editConfirmPassword.text.toString().replace(" ", "")

            // Se houver valores em branco, retornar
            if (values.values.any { it.isBlank() }) {
                showErrorMessage("Erro: Preencha todos os campos para continuar")
                return
            }

            val (isCpfvalid, errorMessageCpf) = isCPF(values["cpf"].toString())
            updateInputState(
                editCpf,
                tvCpfError,
                errorMessageCpf,
                !isCpfvalid
            )

            val (isBirthDateValid, errorMessageDate) = isLegalAge(values["data_de_nascimento"].toString())
            updateInputState(
                editBirthDate,
                tvDateError,
                errorMessageDate,
                !isBirthDateValid
            )

            val (isPhoneValid, errorMessagePhone) = isPhone(values["celular"].toString())
            updateInputState(
                editPhone,
                tvPhoneError,
                errorMessagePhone,
                !isPhoneValid
            )

            val (isPasswordValid, errorMessagePassword) = isPassword(values["senha"].toString())
            updateInputState(
                editPassword,
                tvPasswordError,
                errorMessagePassword,
                !isPasswordValid
            )

            val (arePasswordsMatch, errorMessagePasswords) = arePasswords(
                values["senha"].toString(),
                values["senha2"].toString()
            )
            updateInputState(
                editConfirmPassword,
                tvConfirmPasswordError,
                errorMessagePasswords,
                !arePasswordsMatch
            )

            val (isEmailValid, errorMessageEmail) = isEmail(values["email"].toString())
            updateInputState(
                editEmail,
                tvEmailError,
                errorMessageEmail,
                !isEmailValid
            )

            if (isCpfvalid && isBirthDateValid && isPhoneValid && isPasswordValid && arePasswordsMatch && isEmailValid) registerUser()
        }
    }

    /**
     * Exibe um diálogo de ERRO customizado com uma mensagem simples e um botão "OK".
     * @param message A mensagem a ser exibida no diálogo de alerta.
     * @authors: Lais.
     */
    private fun showErrorMessage(message: String) {
        // Inflate o layout personalizado
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_error, null)

        // Crie o AlertDialog com o layout personalizado
        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Configure o botão OK para fechar o diálogo
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        // Atualize a mensagem no TextView
        val textViewMessage = view.findViewById<TextView>(R.id.tvMessage)
        textViewMessage.text = message

        // Mostre o diálogo
        alertDialog.show()
    }

    /**
     * Valida um número de CPF.
     * @param document O número de CPF a ser validado.
     * @return Um par contendo um booleano que indica se o CPF é válido e uma mensagem de erro.
     * @authors: Marcos.
     */
    private fun isCPF(document: String): Pair<Boolean, String> {
        val numbers = document.filter { it.isDigit() }.map { it.toString().toInt() }
        if (numbers.size != 11) return Pair(false, "Número de CPF inválido")
        if (numbers.all { it == numbers[0] }) return Pair(false, "Número de CPF inválido")
        val dv1 = ((0..8).sumOf { (it + 1) * numbers[it] }).rem(11).let {
            if (it >= 10) 0 else it
        }
        val dv2 = ((0..8).sumOf { it * numbers[it] }.let { (it + (dv1 * 9)).rem(11) }).let {
            if (it >= 10) 0 else it
        }
        val isValid = numbers[9] == dv1 && numbers[10] == dv2
        return Pair(isValid, if (isValid) "" else "Número de CPF inválido")
    }

    /**
     * Verifica se a data fornecida é valida e se indica que o usuário tem pelo menos 14 anos de idade.
     * @param givenDate A data de nascimento fornecida pelo usuário.
     * @return Um par contendo um booleano que indica se a data indica uma idade válida e uma mensagem de erro.
     * @authors: Isabella.
     */
    private fun isLegalAge(givenDate: String): Pair<Boolean, String> {
        if (givenDate.length < 10) return Pair(false, "Data inválida")

        val birthDate = givenDate.split("/")
        val age = LocalDate.now()
            .minusDays(birthDate[0].toLong())
            .minusMonths(birthDate[1].toLong())
            .minusYears(birthDate[2].toLong()).year.toLong()

        return if (age < 14) {
            Pair(false, "Você deve ter pelo menos 14 anos de idade")
        } else {
            Pair(true, "")
        }
    }

    /**
     * Valida um número de telefone.
     * @param givenPhone O número de telefone a ser validado.
     * @return Um par contendo um booleano que indica se o número de telefone é válido e uma mensagem de erro.
     * @authors: Isabella.
     */
    private fun isPhone(givenPhone: String): Pair<Boolean, String> {
        val phone = givenPhone.filter { it.isDigit() }
        if (phone.length != 11) return Pair(false, "Número de celular inválido")
        if (phone.substring(0, 2).toInt() !in 11..99) return Pair(
            false,
            "Número de celular inválido"
        )
        return Pair(true, "")
    }

    /**
     * Valida uma senha com base em determinados critérios.
     * @param givenPassword A senha a ser validada.
     * @return Um par contendo um booleano que indica se a senha é válida e uma mensagem de erro.
     * @authors: Isabella.
     */
    private fun isPassword(givenPassword: String): Pair<Boolean, String> {
        if (givenPassword.length < 8) {
            return Pair(false, "Senha muito curta (min 8 caracteres)")
        }
        if (!givenPassword.contains(Regex("[A-Z]"))) {
            return Pair(false, "A senha deve conter pelo menos uma letra maiúscula")
        }
        if (!givenPassword.contains(Regex("[a-z]"))) {
            return Pair(false, "A senha deve conter pelo menos uma letra minúscula")
        }
        if (!givenPassword.contains(Regex("\\d"))) {
            return Pair(false, "A senha deve conter pelo menus um número")
        }
        if (!givenPassword.contains(Regex("[^A-Za-z0-9]"))) {
            return Pair(false, "A senha deve conter ao menos um caractere especial (ex: !@#\$%&*)")
        }
        return Pair(true, "")
    }

    /**
     * Valida um endereço de email.
     * @param email O endereço de email a ser validado.
     * @return Um par contendo um booleano que indica se o email é válido e uma mensagem de erro.
     * @authors: Isabella.
     */
    private fun isEmail(email: String): Pair<Boolean, String> {
        val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return Pair(isValid, if (isValid) "" else "Email inválido")
    }

    /**
     * Verifica se duas senhas são iguais.
     * @param givenPassword1 A primeira senha.
     * @param givenPassword2 A segunda senha.
     * @return Um par contendo um booleano que indica se as senhas são iguais e uma mensagem de erro.
     * @authors: Isabella.
     */
    private fun arePasswords(
        givenPassword1: String,
        givenPassword2: String
    ): Pair<Boolean, String> {
        val passwordsMatch = givenPassword1 == givenPassword2
        return Pair(passwordsMatch, if (passwordsMatch) "" else "As senhas não correspondem")
    }

    /**
     * Registra um novo usuário.
     * Verifica se há conexão com a internet usando a instância de NetworkChecker.
     * Se houver conexão, tenta criar um novo usuário no Firebase Authentication com o email e senha fornecidos.
     * Se a operação for bem-sucedida, envia um email de verificação, salva os dados do usuário no Firebase Firestore,
     * inicia a VerifyActivity e encerra a SignUpActivity atual.
     * Se houver um erro na operação, exibe um alerta com a mensagem apropriada.
     * Se não houver conexão com a internet, inicia a NetworkErrorActivity.
     * @authors: Marcos.
     */
    private fun registerUser() {
        if (networkChecker.hasInternet()) {
            auth.createUserWithEmailAndPassword(
                values["email"].toString(), values["senha"].toString()
            ).addOnSuccessListener { authResult ->
                authResult.user?.sendEmailVerification()?.addOnCompleteListener {
                    values.remove("senha2")
                    database.collection("Pessoas").document(authResult.user?.uid.toString())
                        .set(values)
                    startActivity(Intent(this, VerifyActivity::class.java))
                    finish()
                }
            }.addOnFailureListener { exception ->
                if (exception.message.toString() == "The email address is badly formatted.") {
                    showErrorMessage("Erro: Endereço de email inválido.")
                } else {
                    showErrorMessage("Erro: Endereço de email já registrado!")
                }
            }
        } else {
            startActivity(Intent(this, NetworkErrorActivity::class.java))
        }
    }

    /**
     * Atualiza o estado visual de um campo de entrada de texto com base no status fornecido.
     * @param editText O campo de entrada de texto a ser atualizado.
     * @param textView O textView associado que exibirá mensagens de erro.
     * @param text A mensagem de erro a ser exibida.
     * @param status O status que indica se há um erro (true) ou não (false).
     * @authors: Isabella.
     */
    private fun updateInputState(
        editText: EditText,
        textView: TextView,
        text: String,
        status: Boolean
    ) {
        val errorIcon: Drawable?

        if (status) {
            editText.setBackgroundResource(R.drawable.shape_input_invalid)
            textView.text = text
            errorIcon = ContextCompat.getDrawable(this, R.drawable.icon_error)
        } else {
            editText.setBackgroundResource(R.drawable.shape_inputs)
            textView.text = ""
            errorIcon = null
        }

        // Obtém o ícone original do início
        val originalStartIcon = editText.compoundDrawablesRelative[0]

        // Adiciona o ícone de erro no final, mantendo o ícone original no início
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            originalStartIcon,
            null,
            errorIcon,
            null
        )
    }

}
