package taipt4.kotlin.eatitv2.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.model.Order
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val context: Context, var listOrders: List<Order>): RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    internal var calendar: Calendar = Calendar.getInstance()
    @SuppressLint("SimpleDateFormat")
    internal var sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    inner class OrderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        internal var orderImage = itemView.findViewById<ImageView>(R.id.order_item_order_image)
        internal var orderDate = itemView.findViewById<TextView>(R.id.order_item_order_date)
        internal var orderNumber = itemView.findViewById<TextView>(R.id.order_item_order_number)
        internal var orderStatus = itemView.findViewById<TextView>(R.id.order_item_order_status)
        internal var orderComment = itemView.findViewById<TextView>(R.id.order_item_order_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_item, parent, false))
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        Glide.with(context).load(listOrders[position].cartItemList!![0].foodImage).into(holder.orderImage)
        calendar.timeInMillis = listOrders[position].createdDate
        val date = Date(listOrders[position].createdDate)
        holder.orderDate!!.text = StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
            .append(" ")
            .append(sdf.format(date))
            .toString()
        holder.orderNumber!!.text = StringBuilder("Order number: ").append(listOrders[position].orderNumber).toString()
        holder.orderComment!!.text = StringBuilder("Comment: ").append(listOrders[position].comment).toString()
        holder.orderStatus!!.text = StringBuilder("Status: ").append(Common.convertStatusToText(listOrders[position].orderStatus)).toString()
    }

    override fun getItemCount(): Int {
        return listOrders.size
    }


}