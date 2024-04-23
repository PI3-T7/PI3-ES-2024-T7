package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
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

class CardAdapter(private val card: List<CartoesCadastrados>) : Adapter<RecyclerView.ViewHolder>() {

    inner class CardItemViewHolder(itemview: View) : ViewHolder(itemview) {
        val cardNumer: TextView = itemView.findViewById(R.id.tv_card_number)
        val cardFlag: TextView = itemView.findViewById(R.id.tv_add)
    }

    inner class AddCardViewHolder(itemview: View) : ViewHolder(itemview) {
        val btn: ImageView = itemview.findViewById(R.id.img_add)
        val cardView: CardView = itemview.findViewById(R.id.cv_card)
        val tvCardFlag: TextView = itemView.findViewById((R.id.tv_add))

//        init {
//            cardView.setOnClickListener{
//                Toast.makeText(this,"clicou",Toast.LENGTH_LONG).show()
//            }
//        }
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
            holder.cardView.setOnClickListener{
                val intent = Intent(holder.itemView.context, CartaoCadastrandoActivity::class.java)
                startActivity(holder.itemView.context,intent,null)
            }
        } else {
            val card = card[position - 1]
            (holder as CardItemViewHolder)
            holder.cardNumer.text = card.numero
            holder.cardFlag.text = card.bandeira
        }
    }

    override fun getItemCount(): Int {
        return card.size + 1
    }

}