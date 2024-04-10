package br.edu.puccampinas.projeto_smart_locker

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import android.os.Bundle
import android.text.InputType
import android.widget.Toast

// IMPORTAR BRAINTREEPAYMENTS, E TB ADICIONAR NO GRADLE

class CartaoCadastrandoActivity : AppCompatActivity() {

    private lateinit var cardForm : CardForm

    override fun OnCreate(SavedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrandocartao)

        cardForm = findViewById(R.id.cardForm)
        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .actionLabel("comprar")
            .setup(this@CartaoCadastrandoActivity)
        cardForm.cvvEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        bntCadastrar.setOnClickListener{
            if(cardForm.isValid){
                Toast.makeText(this@CartaoCadastrandoActivity,"Card number:${cardForm.cartaoNum}\n"+
                    "Cartão data expiração: ${cardForm.cartao.dataexp}\n"+
                    "Cartão cvv: ${cardForm.cvv}\n"+
                    "Cartão nome : ${cardForm.cartaoNome}\n"
                    ,Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@CartaoCadastrandoActivity,"Favor completar os campos corretamente",Toast.LENGTH_LONG);show()
            }
        }
    }
}




