package taipt4.kotlin.eatitkotlinshipper.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import taipt4.kotlin.eatitkotlinshipper.R
import taipt4.kotlin.eatitkotlinshipper.common.Common
import taipt4.kotlin.eatitkotlinshipper.model.ShippingOrderModel
import java.text.SimpleDateFormat
import java.util.*

class ShippingOrderAdapter(
    var context: Context,
    var shippingOrderModelList: List<ShippingOrderModel>
) :
    RecyclerView.Adapter<ShippingOrderAdapter.ShippingOrderViewHolder>() {

    var sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    inner class ShippingOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDate: TextView = itemView.findViewById(R.id.txt_date)
        var txtOrderAddress: TextView = itemView.findViewById(R.id.txt_order_address)
        var txtOrderNumber: TextView = itemView.findViewById(R.id.txt_order_number)
        var txtPayment: TextView = itemView.findViewById(R.id.txt_payment)
        var imgFood: ImageView = itemView.findViewById(R.id.img_food)
        var btnShipNow: MaterialButton = itemView.findViewById(R.id.btn_ship_now)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShippingOrderViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.layout_shipping_order, parent, false)
        return ShippingOrderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ShippingOrderViewHolder, position: Int) {
        Glide.with(context)
            .load(shippingOrderModelList[position].orderModel!!.cartItemList!![0].foodImage)
            .into(holder.imgFood)
        holder.txtDate.text =
            StringBuilder(sdf.format(shippingOrderModelList[position].orderModel!!.createdDate)).toString()
        Common.setSpanStringColor(
            "No.: ",
            shippingOrderModelList[position].orderModel!!.key!!,
            holder.txtOrderNumber,
            Color.parseColor("#BA454A")
        )
        Common.setSpanStringColor(
            "Address: ",
            shippingOrderModelList[position].orderModel!!.shippingAddress!!,
            holder.txtOrderAddress,
            Color.parseColor("#BA454A")
        )
        Common.setSpanStringColor(
            "Payment: ",
            shippingOrderModelList[position].orderModel!!.transactionId!!,
            holder.txtPayment,
            Color.parseColor("#BA454A")
        )
        if (shippingOrderModelList[position].isStartTrip) {
            holder.btnShipNow.isEnabled = false

        }
    }

    override fun getItemCount(): Int {
        return shippingOrderModelList.size
    }
}