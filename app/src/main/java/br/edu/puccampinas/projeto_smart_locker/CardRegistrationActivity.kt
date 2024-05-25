package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityCardRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button

/**
 * Activity responsável pelo registro de cartões.
 * @authors: Alex, Isabella, Marcos e Lais
 */
class CardRegistrationActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCardRegistrationBinding.inflate(layoutInflater) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance() }

    private val networkChecker by lazy {
        NetworkChecker(
            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
                ?: throw IllegalStateException("ConnectivityManager not available")
        )
    }

    /**
     * Método chamado quando a atividade é criada.
     * @authors: Alex, Isabella, Marcos e Lais
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configuração dos listeners para os botões e campos de entrada
        with(binding) {

            btnCadastrar.setOnClickListener {
                isInputsValid()
            }

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

            nav.buttonHome.setOnClickListener {
                showAlertCancel()
            }

            nav.buttonVoltar.visibility = View.GONE
        }
    }

    /**
     * Função que realiza o cadastro de um novo cartão.
     * @authors: Marcos, Isabella e Lais
     */
    private fun cadastrarCartao() {
        if (networkChecker.hasInternet()) {
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
                    // Inicia a CardsActivity após o cadastro bem-sucedido
                    startActivity(Intent(this, CardsActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    showAlert("Falha ao cadastrar cartão!")
                }

            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("meuFiltro"))
        } else {
            startActivity(Intent(this, NetworkErrorActivity::class.java))
        }
    }

    /**
     * Função responsável por fazer a validação dos campos de input do cartão
     * @authors: Marcos, Isabella e Lais
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isInputsValid() {
        with(binding) {

            // Verifica se os campos são nulos
            if (editNumCartao.text.isNullOrEmpty() ||
                editName.text.isNullOrEmpty() ||
                editDataValidade.text.isNullOrEmpty() ||
                editCVV.text.isNullOrEmpty()
            ) {
                showErrorMessage("Erro: Preencha todos os campos para continuar.")
                return
            }

            // Verifica se o checkbox está check
            if (!(checkBoxCiente.isChecked)) {
                showErrorMessage("Erro: Você precisa concordar com os termos para continuar.")
                return
            }

            // Faz a validação de cada input altera seu estado
            val isCardNumberValid = isCardNumberValid(editNumCartao.masked)
            updateInputState(
                editNumCartao,
                tvErrorNumber,
                "Número de cartão inválido",
                isCardNumberValid
            )

            val isExpirationDateValid = isExpirationDateValid(editDataValidade.masked)
            Log.d("MinhaTag", "datavalidade $isExpirationDateValid")
            updateInputState(editDataValidade, tvErrorDate, "Data inválida", isExpirationDateValid)

            val isCardCvvValid = isCardCvvValid(editCVV.text.toString())
            updateInputState(editCVV, tvErrorCvv, "Cvv inválido", isCardCvvValid)

            // Se tudo estiver válido, realiza o cadastro do cartão
            if (isCardCvvValid && isExpirationDateValid && isCardNumberValid) cadastrarCartao()
        }
    }

    /**
     * Função que faz a validação do numero do cartão
     * @author: Isabella
     * @param cardNumber - o número do cartão
     * @return true se o numero do cartão for válido, false caso contrário.
     */
    private fun isCardNumberValid(cardNumber: String): Boolean {
        return cardNumber.length >= 16
    }

    /**
     * Verifica se a data de validade do cartão é válida.
     * @author: Isabella
     * @param date A data de validade do cartão no formato "MM/YY".
     * @return true se a data de validade for válida, false caso contrário.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isExpirationDateValid(date: String): Boolean {

        // Obtém a data atual
        val currentDate = LocalDate.now()
        val currentYear = currentDate.year % 100
        val currentMonth = currentDate.monthValue

        // Divide a data de validade em mês e ano
        val (expirationMonth, expirationYear) = date.split("/")

        // Verifica se o ano de expiração é menor que o ano atual ou se é o mesmo ano, mas o mês de expiração é menor
        if (expirationYear.toInt() < currentYear || (expirationYear.toInt() == currentYear && expirationMonth.toInt() < currentMonth)) {
            return false
        }

        // Verifica se o mês de expiração está dentro do intervalo de 1 a 12
        if (expirationMonth.toInt() !in 1..12) return false

        return true
    }

    /**
     * Verifica se o cvv é válido
     * @author: Isabella
     * @param cvv O cvv do cartão.
     * @return true se o cvv for válido, false caso contrário.
     */
    private fun isCardCvvValid(cvv: String): Boolean {
        return cvv.length == 3
    }


    /**
     * Cria um TextWatcher com formatação e validação customizadas.
     * @authors: Marcos e Isabella
     * Este TextWatcher atualiza o texto de um TextView com o conteúdo atualizado de um EditText.
     * @param textView O TextView associado que exibirá o texto atualizado.
     * @return Um TextWatcher configurado para atualizar o textView conforme o texto do EditText é alterado.
     */
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

    /**
     * Exibe um diálogo de alerta com uma mensagem simples e um botão "OK".
     * @author: Isabella
     * @param message A mensagem a ser exibida no diálogo de alerta.
     */
    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Exibe um diálogo de confirmação de cancelamento com opções "SIM" e "NÃO".
     * @authors: Lais e Isabella
     * Este diálogo é usado para confirmar se o usuário deseja cancelar uma operação.
     * Dependendo da escolha do usuário, a atividade pode ser finalizada e outra atividade pode ser iniciada.
     */
    private fun showAlertCancel() {
        // Inflate o layout customizado
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_cancel_operation, null)

        // Crie o AlertDialog e ajuste sua altura desejada
        val customDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Impede o fechamento do diálogo ao tocar fora dele
            .create()

        // Defina a altura desejada para o diálogo
        customDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 600)

        // Configure os botões do diálogo
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo3)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes3)

        btnNo.setOnClickListener {
            // Fecha o diálogo sem fazer logout
            customDialog.dismiss()
        }

        btnYes.setOnClickListener {
            startActivity(Intent(this,ManagerMainScreenActivity::class.java))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }

    /**
     * Exibe um diálogo de ERRO customizado com uma mensagem simples e um botão "OK".
     * @author: Lais
     * @param message A mensagem a ser exibida no diálogo de alerta.
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
     * Atualiza o estado visual de um campo de entrada de texto com base no status fornecido.
     * @author: Isabella
     * @param editText O campo de entrada de texto a ser atualizado.
     * @param textView O textView associado que exibirá mensagens de erro.
     * @param text A mensagem de erro a ser exibida.
     * @param status O status que indica se há um erro (true) ou não (false).
     */
    private fun updateInputState(
        editText: EditText,
        textView: TextView,
        text: String,
        status: Boolean
    ) {
        val errorIcon: Drawable?

        if (status) {
            editText.setBackgroundResource(R.drawable.shape_inputs)
            textView.text = ""
            errorIcon = null
        } else {
            editText.setBackgroundResource(R.drawable.shape_input_invalid)
            textView.text = text
            errorIcon = ContextCompat.getDrawable(this, R.drawable.icon_error)
        }

        // Adiciona o ícone de erro no final, mantendo o ícone original no início
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            errorIcon,
            null
        )
    }
}