package taipt4.kotlin.eatitkotlinserver.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.eventbus.UpdateActiveEvent
import taipt4.kotlin.eatitkotlinserver.model.Shipper

class ShipperAdapter(
    internal var context: Context,
    internal var listShipper: MutableList<Shipper>
): RecyclerView.Adapter<ShipperAdapter.ShipperViewHolder>() {

    inner class ShipperViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var txtShipperName: TextView = itemView.findViewById(R.id.txt_shipper_name)
        var txtShipperPhone: TextView = itemView.findViewById(R.id.txt_shipper_phone)
        var btnEnable: SwitchCompat = itemView.findViewById(R.id.btn_enable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipperViewHolder {
        return ShipperViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_shipper, parent, false))
    }

    override fun onBindViewHolder(holder: ShipperViewHolder, position: Int) {
        holder.txtShipperName.text = listShipper[position].name
        holder.txtShipperPhone.text = listShipper[position].phone
        holder.btnEnable.isChecked = listShipper[position].isActive
//        Log.d("TaiPT4", listShipper[position].name!! + " --- " + listShipper[position].phone + " --- " +listShipper[position].isActive)

        // Event
        holder.btnEnable.setOnCheckedChangeListener{ compoundButton, isChecked ->
            EventBus.getDefault().postSticky(UpdateActiveEvent(listShipper[position], isChecked))
        }
    }

    override fun getItemCount(): Int {
        return listShipper.size
    }


}