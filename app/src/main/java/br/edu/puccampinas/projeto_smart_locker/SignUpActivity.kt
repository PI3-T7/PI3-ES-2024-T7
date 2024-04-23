package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Build
import java.time.LocalDate
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Activity SignUp -> Activity da tela de cadastro de um novo usuário

@RequiresApi(Build.VERSION_CODES.O)
class SignUpActivity : AppCompatActivity() {
    // Configuração do ViewBinding
    private val binding by lazy { ActivitySignUpBinding.inflate( layoutInflater ) }
    // Configuração do FirebaseAuth
    private val auth by lazy { FirebaseAuth.getInstance() }
    // Configuração do Firebase Firestore
    private val bd by lazy { FirebaseFirestore.getInstance() }
    // Criação do mapa para guardar os valores para cadastro
    private val values = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            btnArrow.setOnClickListener { finish() }
            btnSignUp.setOnClickListener { validData() }
        }
    }

    private fun validData() {
        with(binding){
            values["nome_completo"] = editTextName.text.toString()
            values["cpf"] = editTextCpf.text.toString()
            values["data_de_nascimento"] = editTextBirthDate.text.toString()
            values["celular"] = editTextPhone.text.toString()
            values["email"] = editTextEmail.text.toString()
            values["senha"] = editTextPassword.text.toString()
        }
        if (values.values.any{ it.isEmpty() }){
            Toast.makeText(this, "Por favor, preencha todos os campos antes de prosseguir.", Toast.LENGTH_LONG).show()
            return
        }
        if (!isCPF(values["cpf"].toString())) {
            Toast.makeText(this, "Por favor, verifique se o CPF está correto antes de prosseguir.", Toast.LENGTH_LONG).show()
            return
        }
//        if (!isLegalAge(values["data_de_nascimento"].toString())){
//            Toast.makeText(this, "O usuário deve ter pelo menos 14 anos para ser cadastrado!", Toast.LENGTH_LONG).show()
//            return
//        }
        if (!isPhone(values["celular"].toString())) {
            Toast.makeText(this, "Por favor, verifique se o número de telefone está correto antes de prosseguir", Toast.LENGTH_LONG).show()
            return
        }
        registerUser()
    }

    private fun isCPF(document: String): Boolean {
        val numbers = document.filter { it.isDigit() }.map { it.toString().toInt() }
        if (numbers.size != 11) return false
        // Caso de repetição
        if (numbers.all { it == numbers[0] }) return false
        // Digito 1
        val dv1 = ((0..8).sumOf { (it + 1) * numbers[it] }).rem(11).let {
            if (it >= 10) 0 else it
        }
        val dv2 = ((0..8).sumOf { it * numbers[it] }.let { (it + (dv1 * 9)).rem(11) }).let {
            if (it >= 10) 0 else it
        }
        return numbers[9] == dv1 && numbers[10] == dv2
    }

    private fun isLegalAge(givenDate: String): Boolean {
        val birthDate = givenDate.split("/")
        val age = LocalDate.now()
            .minusDays(birthDate[0].toLong())
            .minusMonths(birthDate[1].toLong())
            .minusYears(birthDate[2].toLong()).year.toLong()
        return age >= 14
    }

    private fun isPhone(givenPhone: String): Boolean {
        val phone = givenPhone.filter { it.isDigit() }
        if (phone.length != 11) return false
        if (phone.substring(0, 2).toInt() !in 11..99) return false
        return true
    }

    private fun registerUser() {
        auth.createUserWithEmailAndPassword(
            values["email"].toString(), values["senha"].toString()
        ).addOnSuccessListener {
            authResult ->
            authResult.user?.sendEmailVerification()?.addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, VerifyActivity::class.java))
                    bd.collection("Usuários").document(authResult.user?.uid.toString()).set(values)
                    finish()
                }
                else Toast.makeText(this, "Falha ao enviar email de verificação, tente outro endereço de email!", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            exception -> Toast.makeText(this, "Falha ao cadastrar usuário: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
}