package br.edu.puccampinas.projeto_smart_locker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * Adapter para gerenciar a exibição de cartões e o botão de adicionar cartão em um RecyclerView.
 * @author: Isabella
 * @param card Lista mutável de objetos CartoesCadastrados representando os cartões cadastrados.
 */
class CardAdapter(private val card: MutableList<CartoesCadastrados>) : Adapter<RecyclerView.ViewHolder>() {

    /**
     * ViewHolder para os itens de cartão.
     * @param itemView A view do item de cartão.
     */
    inner class CardItemViewHolder(itemview: View) : ViewHolder(itemview) {
        val cardNumer: TextView = itemView.findViewById(R.id.tv_card_number)
        val cardFlag: TextView = itemView.findViewById(R.id.tv_add)
    }

    /**
     * ViewHolder para o item de adicionar cartão.
     * @param itemView A view do item de adicionar cartão.
     */
    inner class AddCardViewHolder(itemview: View) : ViewHolder(itemview) {
        val cardView: CardView = itemview.findViewById(R.id.cv_card)
    }

    companion object {
        private const val TIPO_BOTAO_ADICIONAR = 0
        private const val TIPO_CARTAO_CADASTRADO = 1
    }

    /**
     * Retorna o tipo de view para a posição especificada.
     * @param position A posição no adapter.
     * @return O tipo de view, seja TIPO_BOTAO_ADICIONAR ou TIPO_CARTAO_CADASTRADO.
     */
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TIPO_BOTAO_ADICIONAR else TIPO_CARTAO_CADASTRADO
    }

    /**
     * Cria novos ViewHolders de acordo com o tipo de view.
     * @param parent O ViewGroup ao qual a nova view será adicionada após ser vinculada à posição do adapter.
     * @param viewType O tipo de view da nova view.
     * @return Um novo ViewHolder da view apropriada.
     */
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

    /**
     * Associa os dados do cartão ao ViewHolder apropriado com base na posição.
     * @param holder O ViewHolder a ser atualizado.
     * @param position A posição do item no adapter.
     */
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
    /**
     * Retorna o número total de itens no adapter (cartões cadastrados + 1 para o botão de adicionar).
     * @return O número total de itens.
     */
    override fun getItemCount(): Int {
        return card.size + 1
    }

}