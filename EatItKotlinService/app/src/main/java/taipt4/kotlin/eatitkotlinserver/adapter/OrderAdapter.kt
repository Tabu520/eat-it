package taipt4.kotlin.eatitkotlinserver.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.model.Order
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
class OrderAdapter(internal var context: Context, internal var listOrder: MutableList<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var orderImage = itemView.findViewById<ImageView>(R.id.order_item_order_image)
        internal var orderTime = itemView.findViewById<TextView>(R.id.order_item_order_time)
        internal var orderNumber = itemView.findViewById<TextView>(R.id.order_item_order_number)
        internal var orderStatus = itemView.findViewById<TextView>(R.id.order_item_order_status)
        internal var numberOfItem = itemView.findViewById<TextView>(R.id.order_item_number_item)
        internal var userName = itemView.findViewById<TextView>(R.id.order_item_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_order_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        Glide.with(context).load(listOrder[position].cartItemList!![0].foodImage)
            .into(holder.orderImage)
        holder.orderNumber.text = listOrder[position].key
        Common.setSpanStringColor(
            "Order date ",
            sdf.format(listOrder[position].createdDate),
            holder.orderTime,
            Color.parseColor("#333639")
        )
        Common.setSpanStringColor(
            "Name ",
            listOrder[position].userName!!,
            holder.userName,
            Color.parseColor("#006061")
        )
        Common.setSpanStringColor(
            "Order status ",
            Common.convertStatusToString(listOrder[position].orderStatus),
            holder.orderStatus,
            Color.parseColor("#00574B")
        )
        Common.setSpanStringColor(
            "Number of items ",
            if (listOrder[position].cartItemList == null) "0" else listOrder[position].cartItemList!!.size.toString(),
            holder.numberOfItem,
            Color.parseColor("#00574B")
        )

    }

    override fun getItemCount(): Int {
        return listOrder.size
    }

    fun getItemAtPosition(pos: Int): Order {
        return listOrder[pos]
    }

    fun removeItem(pos: Int) {
        listOrder.removeAt(pos)
    }
}