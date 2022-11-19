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
import taipt4.kotlin.eatitkotlinserver.model.*

class AddonAdapter(var context: Context, var listAddonModel: MutableList<AddonModel>): RecyclerView.Adapter<AddonAdapter.AddonViewHolder>() {

    private var editPos: Int = 0
    private var updateAddonModel: UpdateAddonModel = UpdateAddonModel()

    inner class AddonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        return AddonViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_addon_size_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AddonViewHolder, position: Int) {
        holder.txtName.text = listAddonModel[position].name
        holder.txtPrice.text = listAddonModel[position].price.toString()

        //Event
        holder.imgDelete.setOnClickListener {
            listAddonModel.removeAt(position)
            notifyItemRemoved(position)
            updateAddonModel.listAddonModel = listAddonModel
            EventBus.getDefault().postSticky(updateAddonModel)
        }
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                editPos = position
                EventBus.getDefault().postSticky(SelectAddonModel(listAddonModel[position]))
            }
        })
    }

    override fun getItemCount(): Int {
        return listAddonModel.size
    }

    fun addNewAddon(addonModel: AddonModel) {
        listAddonModel.add(addonModel)
        notifyItemInserted(listAddonModel.size - 1)
        updateAddonModel.listAddonModel = listAddonModel
        EventBus.getDefault().postSticky(updateAddonModel)
    }

    fun editAddon(addonModel: AddonModel) {
        listAddonModel[editPos] = addonModel
        notifyItemChanged(editPos)
        updateAddonModel.listAddonModel = listAddonModel
        EventBus.getDefault().postSticky(updateAddonModel)
    }
}