package taipt4.kotlin.eatitkotlinserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.callback.IRecyclerItemClickListener
import taipt4.kotlin.eatitkotlinserver.eventbus.UpdateActiveEvent
import taipt4.kotlin.eatitkotlinserver.model.Shipper

class SelectedShipperAdapter(
    internal var context: Context,
    internal var listShipper: List<Shipper>
): RecyclerView.Adapter<SelectedShipperAdapter.ShipperViewHolder>() {

    var lastCheckedImageView: ImageView? = null
    var selectedShipper: Shipper? = null
        private set

    inner class ShipperViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var txtShipperName: TextView = itemView.findViewById(R.id.txt_shipper_name)
        var txtShipperPhone: TextView = itemView.findViewById(R.id.txt_shipper_phone)
        var imChecked: ImageView = itemView.findViewById(R.id.im_checked)
        var iRecyclerItemClickListener: IRecyclerItemClickListener? = null

        fun setClick(iRecyclerItemClickListener: IRecyclerItemClickListener) {
            this.iRecyclerItemClickListener = iRecyclerItemClickListener
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            iRecyclerItemClickListener!!.onItemClick(v!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipperViewHolder {
        return ShipperViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected, parent, false))
    }

    override fun onBindViewHolder(holder: ShipperViewHolder, position: Int) {
        holder.txtShipperName.text = listShipper[position].name
        holder.txtShipperPhone.text = listShipper[position].phone
        holder.setClick(object: IRecyclerItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (lastCheckedImageView != null) {
                    lastCheckedImageView!!.setImageResource(0)
                }
                holder.imChecked.setImageResource(R.drawable.ic_baseline_check_24)
                lastCheckedImageView = holder.imChecked
                selectedShipper = listShipper[position]
            }
        })
    }

    override fun getItemCount(): Int {
        return listShipper.size
    }

}