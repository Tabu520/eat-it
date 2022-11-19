package taipt4.kotlin.eatitv2.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.eventbus.BestDealItemClick
import taipt4.kotlin.eatitv2.model.BestDeal

class BestDealAdapter(context: Context, itemList: List<BestDeal>, isInfinite: Boolean)
    : LoopingPagerAdapter<BestDeal>(context, itemList, isInfinite)
{
    override fun bindView(convertView: View, listPosition: Int, viewType: Int) {
        val imageView: ImageView = convertView.findViewById(R.id.best_deal_image) as ImageView
        val textView: TextView = convertView.findViewById(R.id.best_deal_name) as TextView

        // Set data
        Glide.with(context).load(itemList!![listPosition].image).into(imageView)
        Log.d("TaiPT4", itemList!![listPosition].image.toString())
        textView.text = itemList!![listPosition].name

        convertView.setOnClickListener {
            EventBus.getDefault().postSticky(BestDealItemClick(itemList!![listPosition]))
        }
    }

    override fun inflateView(viewType: Int, container: ViewGroup, listPosition: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deal_item, container, false)
    }
}