package taipt4.kotlin.eatitkotlinserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.callback.IRecyclerItemClickListener
import taipt4.kotlin.eatitkotlinserver.model.SelectSizeModel
import taipt4.kotlin.eatitkotlinserver.model.SizeModel
import taipt4.kotlin.eatitkotlinserver.model.UpdateSizeModel

class SizeAdapter(var context: Context, var listSizeModel: MutableList<SizeModel>) :
    RecyclerView.Adapter<SizeAdapter.SizeViewHolder>() {

    private var editPos: Int = 0
    private var updateSizeModel: UpdateSizeModel = UpdateSizeModel()

    inner class SizeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.addon_size_name)
        val txtPrice: TextView = itemView.findViewById(R.id.addon_size_price)
        var imgDelete: ImageView = itemView.findViewById(R.id.addon_size_img_delete)

        private var listener: IRecyclerItemClickListener? = null
        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }

        init {
            itemView.setOnClickListener {
                listener!!.onItemClick(it, adapterPosition)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeViewHolder {
        return SizeViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_addon_size_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SizeViewHolder, position: Int) {
        holder.txtName.text = listSizeModel[position].name
        holder.txtPrice.text = listSizeModel[position].price.toString()

        //Event
        holder.imgDelete.setOnClickListener {
            listSizeModel.removeAt(position)
            notifyItemRemoved(position)
            updateSizeModel.listSizeModel = listSizeModel
            EventBus.getDefault().postSticky(updateSizeModel)
        }
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                editPos = position
                EventBus.getDefault().postSticky(SelectSizeModel(listSizeModel[position]))
            }
        })
    }

    override fun getItemCount(): Int {
        return listSizeModel.size
    }

    fun addNewSize(sizeModel: SizeModel) {
        listSizeModel.add(sizeModel)
        notifyItemInserted(listSizeModel.size - 1)
        updateSizeModel.listSizeModel = listSizeModel
        EventBus.getDefault().postSticky(updateSizeModel)
    }

    fun editSize(sizeModel: SizeModel) {
        listSizeModel[editPos] = sizeModel
        notifyItemChanged(editPos)
        updateSizeModel.listSizeModel = listSizeModel
        EventBus.getDefault().postSticky(updateSizeModel)
    }
}