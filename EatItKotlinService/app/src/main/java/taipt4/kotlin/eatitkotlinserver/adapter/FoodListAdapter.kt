package taipt4.kotlin.eatitkotlinserver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.callback.IRecyclerItemClickListener
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.model.Food

class FoodListAdapter(internal var context: Context, internal var foodList: List<Food>) :
    RecyclerView.Adapter<FoodListAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var foodName: TextView = itemView.findViewById(R.id.food_name) as TextView
        var foodPrice: TextView = itemView.findViewById(R.id.food_price) as TextView
        var foodImage: ImageView = itemView.findViewById(R.id.food_image) as ImageView

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
            }
        })

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

    fun getItemAtPosition(pos: Int): Food {
        return foodList[pos]
    }

}