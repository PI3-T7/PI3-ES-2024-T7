package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.projeto_smart_locker.databinding.ActivityOpenLockerBinding

/**
 * Activity que mostra as opções de abrir o armário momentaneamente ou encerrar locação
 * @autor Isabella
 */
class OpenLockerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityOpenLockerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            showAlertCancel()
        }

        binding.btConfirm.setOnClickListener {
            if (binding.btnOpen.isChecked) {
                // Se o botão abrir armario estiver selecionado
                val intent = Intent(this, ConfirmOpenActivity::class.java)
                startActivity(intent)
                finish()
            } else if (binding.btnFinish.isChecked) {

                // Se o botão encerrar locação estiver selecionado
                val intent = Intent(this, EndLeaseActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Se nenhum botão estiver selecionado
                Toast.makeText(this, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Exibe um diálogo de confirmação de cancelamento com opções "SIM" e "NÃO".
     * Este diálogo é usado para confirmar se o usuário deseja cancelar uma operação.
     * Dependendo da escolha do usuário, a atividade pode ser finalizada e outra atividade pode ser iniciada.
     * @autor Isabella
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
            customDialog.dismiss()
        }

        btnYes.setOnClickListener {
            startActivity(Intent(this, ManagerMainScreenActivity::class.java))
            finish()
            customDialog.dismiss()
        }

        // Mostre o diálogo
        customDialog.show()
    }
}