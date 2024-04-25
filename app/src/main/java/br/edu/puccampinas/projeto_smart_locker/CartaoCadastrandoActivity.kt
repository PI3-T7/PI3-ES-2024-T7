package br.edu.puccampinas.projeto_smart_locker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import android.widget.Button

class CartaoCadastrandoActivity : AppCompatActivity() {

    private lateinit var checkBoxCiente: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrandocartao)

        val etCardNumber = findViewById<EditText>(R.id.editNumCartao)
        val tvCardNumber = findViewById<TextView>(R.id.tvCardNumberDetail)

        val etName = findViewById<EditText>(R.id.editName)
        val tvName = findViewById<TextView>(R.id.tvNameDetail)

        val etExpirationDate = findViewById<EditText>(R.id.editDataValidade)
        val tvExpirationDate = findViewById<TextView>(R.id.tvExpDateDetail)

        val etCvv = findViewById<EditText>(R.id.editCVV)
        val tvCvv = findViewById<TextView>(R.id.tvCvvDetail)

        checkBoxCiente = findViewById(R.id.checkBoxCiente)

        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
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
        etCardNumber.addTextChangedListener(
            createTextWatcher(
                tvCardNumber,
                etCardNumber,
                ::formatCardNumber
            )
        )
        etName.addTextChangedListener(createTextWatcher(tvName, etName, ::validateName))
        etExpirationDate.addTextChangedListener(
            createTextWatcher(
                tvExpirationDate,
                etExpirationDate,
                ::formatCardExpDate
            )
        )
        etCvv.addTextChangedListener(createTextWatcher(tvCvv, etCvv, ::validateCvv))
    }

    // Função para realizar o cadastro do cartão
    private fun cadastrarCartao() {
        // Implemente aqui a lógica de cadastro do cartão
        // Por exemplo, exibir uma mensagem de sucesso
        // ou chamar uma função para realizar o cadastro no banco de dados
        // Este método será chamado apenas se a checkbox estiver marcada
        // e o cadastro for válido
        exibirMensagem("Cadastro realizado com sucesso!")
    }

    // Função para exibir mensagem de erro se a checkbox não estiver marcada
    private fun exibirErroCheckBox() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Erro")
        alertDialogBuilder.setMessage("Você precisa concordar com os termos para continuar.")
        alertDialogBuilder.setPositiveButton("OK", null)
        alertDialogBuilder.show()
    }

    // Função para exibir mensagem de erro se os campos não estiverem preenchidos
    private fun exibirErroCampos() {
        val alertDialogBuilder = AlertDialog.Builder(this)
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
        editText: EditText,
        formatter: ((String) -> String)? = null
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (formatter == null) {
                    // Se o formatador for nulo, apenas mostra o texto
                    textView.text = s
                } else {
                    // Se não, formata o textView e o editText para exibir corretamente
                    textView.text = formatter(s.toString())

                    // Remove o TextWatcher para evitar recursão infinita
                    editText.removeTextChangedListener(this)

                    val formattedText = formatter(s.toString())
                    editText.setText(formattedText)

                    // Restaura o TextWatcher e move o cursor para o final do texto
                    editText.addTextChangedListener(this)
                    editText.setSelection(formattedText.length)
                }
            }
        }
    }

    // Função que faz a formatação do número do cartão no formato "xxxx xxxx xxxx xxxx"
    private fun formatCardNumber(num: String) =
        num.replace("\\D".toRegex(), "").chunked(4).joinToString(" ")

    // Função que faz a formatação da data de validade do cartão no formato MM/YY
    private fun formatCardExpDate(date: String) =
        date.replace("\\D".toRegex(), "").chunked(2).joinToString("/") { it.take(2) }

    // Função que valida e formata o CVV permitindo apenas até 3 caracteres numéricos
    private fun validateCvv(cvv: String): String {
        return cvv.replace("\\D".toRegex(), "").take(3)
    }

    // Função que valida o nome permitindo qualquer entrada de texto
    private fun validateName(name: String): String {
        return name // Aceita qualquer entrada no campo Nome do Cartão
    }

    // Função para exibir uma mensagem simples com um botão "OK"
    private fun exibirMensagem(mensagem: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(mensagem)
        alertDialogBuilder.setPositiveButton("OK", null)
        alertDialogBuilder.show()
    }
}