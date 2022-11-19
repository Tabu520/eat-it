package taipt4.kotlin.eatitv2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.callback.IRecyclerItemClickListener
import taipt4.kotlin.eatitv2.eventbus.PopularFoodItemClick
import taipt4.kotlin.eatitv2.model.PopularCategory

class PopularCategoryAdapter(internal var context: Context, internal var popularCategories: List<PopularCategory>)
    : RecyclerView.Adapter<PopularCategoryAdapter.MyViewHolder>(){

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var categoryName: TextView = itemView.findViewById(R.id.popular_category_name) as TextView
        var categoryImage: CircleImageView = itemView.findViewById(R.id.popular_category_image) as CircleImageView

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_popular_categories_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(popularCategories[position].image).into(holder.categoryImage)
        holder.categoryName.text = popularCategories[position].name

        holder.setListener(object: IRecyclerItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                EventBus.getDefault().postSticky(PopularFoodItemClick(popularCategories[position]))
            }

        })
    }

    override fun getItemCount(): Int {
        return popularCategories.size
    }


}