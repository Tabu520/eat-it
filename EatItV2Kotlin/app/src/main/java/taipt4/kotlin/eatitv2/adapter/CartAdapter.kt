package taipt4.kotlin.eatitv2.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.database.CartDataSource
import taipt4.kotlin.eatitv2.database.CartDatabase
import taipt4.kotlin.eatitv2.database.LocalCartDataSource
import taipt4.kotlin.eatitv2.eventbus.UpdateItemCart
import taipt4.kotlin.eatitv2.model.CartItem
import taipt4.kotlin.eatitv2.model.Food

class CartAdapter(internal var context: Context, internal var listCartItem: List<CartItem>) :
    RecyclerView.Adapter<CartAdapter.MyViewHolder>() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val cartDataSource: CartDataSource

    init {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var cartImage: ImageView = itemView.findViewById(R.id.cart_item_cart_image) as ImageView
        var foodName: TextView = itemView.findViewById(R.id.cart_item_food_name) as TextView
        var foodPrice: TextView = itemView.findViewById(R.id.cart_item_food_price) as TextView
        var btnNumber: ElegantNumberButton = itemView.findViewById(R.id.cart_item_btn_number) as ElegantNumberButton

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(listCartItem[position].foodImage)
            .into(holder.cartImage)
        holder.foodName.text = listCartItem[position].foodName
        holder.foodPrice.text = (listCartItem[position].foodPrice + listCartItem[position].foodExtraPrice).toString()
        holder.btnNumber.number = listCartItem[position].foodQuantity.toString()
        holder.btnNumber.setOnValueChangeListener { _, _, newValue ->
            Log.d("TaiPT4", "btnNumber.setOnValueChangeListener --- #newValue == $newValue")
            listCartItem[position].foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemCart(listCartItem[position]))
        }
    }

    override fun getItemCount(): Int {
        return listCartItem.size
    }

    fun getItemAtPosition(pos: Int): CartItem {
        return listCartItem[pos]
    }
}