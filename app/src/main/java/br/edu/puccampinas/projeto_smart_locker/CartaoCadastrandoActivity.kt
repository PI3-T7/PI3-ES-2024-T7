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
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCadastrandocartaoBinding

class CartaoCadastrandoActivity : AppCompatActivity() {
    private lateinit var checkBoxCiente: CheckBox
    private val binding by lazy { ActivityCadastrandocartaoBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkBoxCiente = findViewById(R.id.checkBoxCiente)

        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
        binding.btnCadastrar.setOnClickListener {
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
        binding.editNumCartao.addTextChangedListener(
            createTextWatcher(
                binding.tvCardNumberDetail,
            )
        )
        binding.editName.addTextChangedListener(
            createTextWatcher(
                binding.tvNameDetail,
            )
        )
        binding.editDataValidade.addTextChangedListener(
            createTextWatcher(
                binding.tvExpDateDetail,
            )
        )
        binding.editCVV.addTextChangedListener(
            createTextWatcher(
                binding.tvCvvDetail,
            )
        )

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
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
        alertDialogBuilder.setTitle("Erro")
        alertDialogBuilder.setMessage("Você precisa concordar com os termos para continuar.")
        alertDialogBuilder.setPositiveButton("OK", null)
        alertDialogBuilder.show()
    }

    // Função para exibir mensagem de erro se os campos não estiverem preenchidos
    private fun exibirErroCampos() {
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
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
        val alertDialogBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
        alertDialogBuilder.setMessage(mensagem)
        alertDialogBuilder.setPositiveButton("OK", null)
        alertDialogBuilder.show()
    }
}