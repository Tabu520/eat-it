package taipt4.kotlin.eatitv2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.callback.IRecyclerItemClickListener
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.eventbus.CategoryClick
import taipt4.kotlin.eatitv2.model.Category

class CategoryAdapter (internal var context: Context, internal var categories: List<Category>)
    : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>(){

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var categoryName: TextView = itemView.findViewById(R.id.category_name) as TextView
        var categoryImage: ImageView = itemView.findViewById(R.id.category_image) as ImageView

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
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(categories[position].image).into(holder.categoryImage)
        holder.categoryName.text = categories[position].name

        // Event
        holder.setListener(object : IRecyclerItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                Common.selectedCategory = categories[position]
                EventBus.getDefault().postSticky(CategoryClick(true, categories[position]))
            }

        })
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun getItemViewType(position: Int): Int {

        return if (categories.size == 1)
            Common.DEFAULT_COLUMN_COUNT
        else
            if (categories.size % 2 == 0)
                Common.DEFAULT_COLUMN_COUNT
            else
                if (position > 1 && position == categories.size - 1)
                    Common.FULL_WIDTH_COLUMN
                else
                    Common.DEFAULT_COLUMN_COUNT
    }

}