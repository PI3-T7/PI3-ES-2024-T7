package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Build
import java.time.LocalDate
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Activity SignUp -> Activity da tela de cadastro de um novo usuário
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            arrow.setOnClickListener { finish() }
            btSignup.setOnClickListener { validData() }
        }

        setCursorToStartOnFocusChange(binding.editName)
        setCursorToStartOnFocusChange(binding.editCpf)
        setCursorToStartOnFocusChange(binding.editEmail)

        binding.togglePasswordVisibility.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se o botão está marcado, mostrar a senha
                binding.editPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Se o botão não está marcado, ocultar a senha
                binding.editPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Para atualizar a exibição do EditText
            binding.editPassword.text?.let { binding.editPassword.setSelection(it.length) }
        }

        binding.togglePasswordVisibility2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se o botão está marcado, mostrar a senha
                binding.editConfirmPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Se o botão não está marcado, ocultar a senha
                binding.editConfirmPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Para atualizar a exibição do EditText
            binding.editConfirmPassword.text?.let { binding.editConfirmPassword.setSelection(it.length) }
        }

        setupPasswordToggle(
            binding.togglePasswordVisibility,
            binding.editPassword,
            InputType.TYPE_CLASS_TEXT
        )

        setupPasswordToggle(
            binding.togglePasswordVisibility2,
            binding.editConfirmPassword,
            InputType.TYPE_CLASS_TEXT
        )
    }

    // Função que altera o toggle
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

    private fun validData() {
        with(binding) {
            values["nome_completo"] = editName.text.toString()
            values["cpf"] = editCpf.masked
            values["data_de_nascimento"] = editBirthDate.masked
            values["celular"] = editPhone.masked
            values["email"] = editEmail.text.toString()
            values["senha"] = editPassword.text.toString().replace(" ", "")
            values["senha2"] = editConfirmPassword.text.toString().replace(" ", "")
        }
        if (values.values.any { it.isBlank() }) {
            Toast.makeText(
                this,
                "Por favor, preencha todos os campos antes de prosseguir!",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (!isCPF(values["cpf"].toString())) {
            Toast.makeText(
                this,
                "Por favor, verifique se o CPF está correto antes de prosseguir!",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (!isLegalAge(values["data_de_nascimento"].toString())) return
        if (!isPhone(values["celular"].toString())) {
            Toast.makeText(
                this,
                "Por favor, verifique se o número de telefone está correto antes de prosseguir!",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (!isPassword(values["senha"].toString(), values["senha2"].toString())) return
        registerUser()
    }

    private fun isCPF(document: String): Boolean {
        val numbers = document.filter { it.isDigit() }.map { it.toString().toInt() }
        if (numbers.size != 11) return false
        if (numbers.all { it == numbers[0] }) return false
        val dv1 = ((0..8).sumOf { (it + 1) * numbers[it] }).rem(11).let {
            if (it >= 10) 0 else it
        }
        val dv2 = ((0..8).sumOf { it * numbers[it] }.let { (it + (dv1 * 9)).rem(11) }).let {
            if (it >= 10) 0 else it
        }
        return numbers[9] == dv1 && numbers[10] == dv2
    }

    private fun isLegalAge(givenDate: String): Boolean {
        if (givenDate.length < 10) {
            Toast.makeText(
                this,
                "Por favor, verifique se a data de nascimento está correta antes de prosseguir!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        val birthDate = givenDate.split("/")
        val age = LocalDate.now()
            .minusDays(birthDate[0].toLong())
            .minusMonths(birthDate[1].toLong())
            .minusYears(birthDate[2].toLong()).year.toLong()
        if (age < 14) {
            Toast.makeText(
                this,
                "O usuário deve ter pelo menos 14 anos de para realizar o cadastro!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun isPhone(givenPhone: String): Boolean {
        val phone = givenPhone.filter { it.isDigit() }
        if (phone.length != 11) return false
        if (phone.substring(0, 2).toInt() !in 11..99) return false
        return true
    }

    private fun isPassword(givenPassword1: String, givenPassword2: String): Boolean {
        if (givenPassword1 != givenPassword2) {
            Toast.makeText(
                this,
                "Os campos de senha não batem, por favor digite novamente!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (givenPassword1.length < 8) {
            Toast.makeText(
                this,
                "A senha é muito curta, ela deve conter pelo menos 8 caracteres!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (!givenPassword1.contains(Regex("[A-Z]"))) {
            Toast.makeText(
                this,
                "A senha é muito fraca, ela deve conter pelo menos uma letra maiúscula!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (!givenPassword1.contains(Regex("[a-z]"))) {
            Toast.makeText(
                this,
                "A senha é muito fraca, ela deve conter pelo menos uma letra minúscula!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (!givenPassword1.contains(Regex("\\d"))) {
            Toast.makeText(
                this,
                "A senha é muito fraca, ela deve conter pelo menos um número!",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        if (!givenPassword1.contains(Regex("[^A-Za-z0-9]"))) {
            Toast.makeText(
                this,
                "A senha deve conter pelo menos um caracter especial! (ex: !@#$%&*)",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun registerUser() {
        auth.createUserWithEmailAndPassword(
            values["email"].toString(), values["senha"].toString()
        ).addOnSuccessListener { authResult ->
            authResult.user?.sendEmailVerification()?.addOnCompleteListener {
                values.remove("senha2")
                database.collection("Pessoas").document(authResult.user?.uid.toString()).set(values)
                startActivity(Intent(this, VerifyActivity::class.java))
                finish()
            }
        }.addOnFailureListener { exception ->
            if (exception.message.toString() == "The email address is badly formatted.") {
                Toast.makeText(
                    this,
                    "Endereço de email inválido!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Endereço de email já registrado!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
