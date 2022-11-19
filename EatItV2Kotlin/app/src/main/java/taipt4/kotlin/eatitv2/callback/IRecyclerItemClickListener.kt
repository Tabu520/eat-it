package taipt4.kotlin.eatitv2.callback

import android.view.View

interface IRecyclerItemClickListener {

    fun onItemClick(view: View, position: Int)
}