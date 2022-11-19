package taipt4.kotlin.eatitv2.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.model.CommentModel

class CommentAdapter(internal var context: Context, internal var listComment: List<CommentModel>): RecyclerView.Adapter<CommentAdapter.MyViewHolder>() {


    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var commentName: TextView = itemView.findViewById(R.id.comment_name) as TextView
        var commentDate: TextView = itemView.findViewById(R.id.comment_date) as TextView
        var commentContent: TextView = itemView.findViewById(R.id.comment_content) as TextView
        var ratingBar: RatingBar = itemView.findViewById(R.id.comment_rating_bar) as RatingBar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_comment_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val timeStamp = listComment[position].commentTimeStamp!!["timeStamp"].toString().toLong()
        holder.commentDate.text = DateUtils.getRelativeTimeSpanString(timeStamp)
        holder.commentName.text = listComment[position].name
        holder.commentContent.text = listComment[position].comment
        holder.ratingBar.rating = listComment[position].ratingValue

    }

    override fun getItemCount(): Int {
        return listComment.size
    }


}