package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder


// Classe Adapter para montar a RecyclerView dos cartões
class CardAdapter(private val card: MutableList<CartoesCadastrados>) : Adapter<RecyclerView.ViewHolder>() {

    // ViewHolder dos cards de cartões
    inner class CardItemViewHolder(itemview: View) : ViewHolder(itemview) {
        val cardNumer: TextView = itemView.findViewById(R.id.tv_card_number)
        val cardFlag: TextView = itemView.findViewById(R.id.tv_add)
    }

    // ViewHolder do card de adicionar cartão
    inner class AddCardViewHolder(itemview: View) : ViewHolder(itemview) {
        val btn: ImageView = itemview.findViewById(R.id.img_add)
        val cardView: CardView = itemview.findViewById(R.id.cv_card)
        val tvCardFlag: TextView = itemView.findViewById((R.id.tv_add))
    }

    companion object {
        private const val TIPO_BOTAO_ADICIONAR = 0
        private const val TIPO_CARTAO_CADASTRADO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TIPO_BOTAO_ADICIONAR else TIPO_CARTAO_CADASTRADO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_BOTAO_ADICIONAR) {
            val itemView = inflater.inflate(R.layout.recycler_view_btn_add_card, parent, false)
            AddCardViewHolder(itemView)
        } else {
            val itemView = inflater.inflate(R.layout.recycler_view_card, parent, false)
            CardItemViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            (holder as AddCardViewHolder)

            // Evento que faz o intent para a tela de cadastro de cartao
            holder.cardView.setOnClickListener{
                val intent = Intent(holder.itemView.context, CardRegistrationActivity::class.java)
                startActivity(holder.itemView.context,intent,null)
            }

        } else {
            val cartao = card[position - 1]
            val holderCartao = holder as CardItemViewHolder
            holderCartao.cardNumer.text = cartao.numeroFormatado
            holderCartao.cardFlag.text = cartao.bandeira
        }
    }

    override fun getItemCount(): Int {
        return card.size + 1
    }

}