package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCardRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CardRegistrationActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCardRegistrationBinding.inflate(layoutInflater) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {

            btnCadastrar.setOnClickListener {
                if (isInputValid()) {
                    if (checkBoxCiente.isChecked) {
                        // Realizar o cadastro ou ação desejada quando a checkbox está marcada
                        cadastrarCartao()
                    } else {
                        // Exibir mensagem de erro se a checkbox não estiver marcada
                        exibirErroCheckBox()
                    }
                } else {
                    // Exibir mensagem de erro se os campos não estiverem preenchidos
                    exibirErroCampos()
                }
            }

            // Chamando os eventos do input
            editNumCartao.addTextChangedListener(
                createTextWatcher(
                    tvCardNumberDetail,
                )
            )
            editName.addTextChangedListener(
                createTextWatcher(
                    tvNameDetail,
                )
            )
            editDataValidade.addTextChangedListener(
                createTextWatcher(
                    tvExpDateDetail,
                )
            )
            editCVV.addTextChangedListener(
                createTextWatcher(
                    tvCvvDetail,
                )
            )
            // Aqui é atribuido o botão de home do navbar, onde primeiro é finalizada a
            // CartoesActivity e depois essa activity
            nav.buttonHome.setOnClickListener {
                LocalBroadcastManager.getInstance(this@CardRegistrationActivity)
                    .sendBroadcast(Intent("meuFiltro"))
                finish()
            }
            // Aqui é atribuido o botão voltar do navbar, onde é finalizado somente essa activity
            nav.buttonVoltar.setOnClickListener {
                finish()
            }
        }
    }

    // Função para realizar o cadastro do cartão
    private fun cadastrarCartao() {
        val cartaoInfo = mapOf(
            "validade" to binding.editDataValidade.masked,
            "nome" to binding.editName.text.toString(),
            "numero" to binding.editNumCartao.masked,
        )
        database
            .collection("Pessoas")
            .document(auth.currentUser?.uid.toString())
            .update("cartoes", FieldValue.arrayUnion(cartaoInfo))
            .addOnSuccessListener {
                exibirMensagem("Cartão cadastrado com êxito!")
            }.addOnFailureListener {
                exibirMensagem("Falha ao cadastrar cartão!")
            }

        // Depois que o cartão foi cadastrado no banco de dados é preciso primeiro finalizar a
        // activity que está funcionando por trás dessa (CartoesActivity)
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("meuFiltro"))
        // Despois o usuário é redirecionado para a CartoesActvity (agora com os dados no novo
        // cartão incluidos
        startActivity(Intent(this, CardsActivity::class.java))
        // Depois é finalizada essa activity
        finish()
    }

    // Função para exibir mensagem de erro se a checkbox não estiver marcada
    private fun exibirErroCheckBox() {
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        alertDialogBuilder.setTitle("Erro")
        alertDialogBuilder.setMessage("Você precisa concordar com os termos para continuar.")
        alertDialogBuilder.setPositiveButton("OK", null)
        alertDialogBuilder.show()
    }

    // Função para exibir mensagem de erro se os campos não estiverem preenchidos
    private fun exibirErroCampos() {
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        alertDialogBuilder.setTitle("Erro")
        alertDialogBuilder.setMessage("Preencha todos os campos para continuar.")
        alertDialogBuilder.setPositiveButton("OK", null)
        alertDialogBuilder.show()
    }

    // Função que verifica se os campos estão preenchidos
    private fun isInputValid(): Boolean {
        val etCardNumber = findViewById<EditText>(R.id.editNumCartao)
        val etName = findViewById<EditText>(R.id.editName)
        val etExpirationDate = findViewById<EditText>(R.id.editDataValidade)
        val etCvv = findViewById<EditText>(R.id.editCVV)

        return etCardNumber.text.isNotEmpty() &&
                etName.text.isNotEmpty() &&
                etExpirationDate.text.isNotEmpty() &&
                etCvv.text.isNotEmpty()
    }


    // Função que cria um TextWatcher com formatação e validação customizadas
    private fun createTextWatcher(
        textView: TextView,
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                textView.text = s.toString()
            }
        }
    }

    // Função para exibir uma mensagem simples com um botão "OK"
    private fun exibirMensagem(mensagem: String) {
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        alertDialogBuilder.setMessage(mensagem)
        alertDialogBuilder.setPositiveButton("OK", null)
        alertDialogBuilder.show()
    }
}