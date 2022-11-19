package taipt4.kotlin.eatitv2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.callback.IRecyclerItemClickListener
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.database.CartDataSource
import taipt4.kotlin.eatitv2.database.CartDatabase
import taipt4.kotlin.eatitv2.database.LocalCartDataSource
import taipt4.kotlin.eatitv2.eventbus.CountCartEvent
import taipt4.kotlin.eatitv2.eventbus.FoodItemClick
import taipt4.kotlin.eatitv2.model.CartItem
import taipt4.kotlin.eatitv2.model.Food

class FoodListAdapter(internal var context: Context, internal var foodList: List<Food>) :
    RecyclerView.Adapter<FoodListAdapter.MyViewHolder>() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val cartDataSource: CartDataSource

    init {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var foodName: TextView = itemView.findViewById(R.id.food_name) as TextView
        var foodPrice: TextView = itemView.findViewById(R.id.food_price) as TextView
        var foodImage: ImageView = itemView.findViewById(R.id.food_image) as ImageView
        var foodIconFavorite: ImageView = itemView.findViewById(R.id.food_icon_fav) as ImageView
        var foodShoppingCart: ImageView =
            itemView.findViewById(R.id.food_icon_shopping) as ImageView

        private var listener: IRecyclerItemClickListener? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }

        override fun onClick(v: View?) {
            listener!!.onItemClick(v!!, adapterPosition)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodListAdapter.MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_food_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(foodList[position].image).into(holder.foodImage)
        holder.foodName.text = foodList[position].name
        holder.foodPrice.text = StringBuilder("$").append(foodList[position].price.toString()).toString()

        // Event
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                Common.selectedFood = foodList[position]
                Common.selectedFood!!.key = position.toString()
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList[position]))
            }
        })

        holder.foodShoppingCart.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid!!
            cartItem.userPhone = Common.currentUser!!.phone!!
            cartItem.foodId = foodList[position].id!!
            cartItem.foodName = foodList[position].name!!
            cartItem.foodImage = foodList[position].image!!
            cartItem.foodPrice = foodList[position].price.toDouble()
            cartItem.foodQuantity = 1
            cartItem.foodExtraPrice = 0.0
            cartItem.foodAddon = "Default"
            cartItem.foodSize = "Default"

            cartDataSource.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid!!,
                cartItem.foodId,
                cartItem.foodSize,
                cartItem.foodAddon
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if (cartItemFromDB.equals(cartItem)) {
                            // If item is already in database, just update
                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity =
                                cartItemFromDB.foodQuantity + cartItem.foodQuantity

                            cartDataSource.updateCart(cartItemFromDB).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(context, "Update Cart Success", Toast.LENGTH_SHORT).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(context, "[UPDATE CART] ${e.message}", Toast.LENGTH_SHORT).show()
                                    }

                                })
                        } else {
                            // If item is not available in database, just insert
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(context, "Add to cart successfully", Toast.LENGTH_SHORT).show()
                                        // Send a notify to HomeActivity to update CounterFab
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, {
                                        Toast.makeText(context, "[INSERT CART] " + it!!.message, Toast.LENGTH_SHORT).show()
                                    })
                            )
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(context, "Add to cart successfully", Toast.LENGTH_SHORT).show()
                                        // Send a notify to HomeActivity to update CounterFab
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, { it ->
                                        Toast.makeText(context, "[INSERT CART] " + it!!.message, Toast.LENGTH_SHORT).show()
                                    })
                            )
                        } else {
                            Toast.makeText(context, "[INSERT CART] $it", Toast.LENGTH_SHORT).show()
                        }
                    }

                })
        }
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun getItemViewType(position: Int): Int {

        return if (foodList.size == 1)
            Common.DEFAULT_COLUMN_COUNT
        else
            if (foodList.size % 2 == 0)
                Common.DEFAULT_COLUMN_COUNT
            else
                if (position > 1 && position == foodList.size - 1)
                    Common.FULL_WIDTH_COLUMN
                else
                    Common.DEFAULT_COLUMN_COUNT
    }

    fun onStop() {
        compositeDisposable.clear()
    }

}