package taipt4.kotlin.eatitkotlinserver.ui.order

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import taipt4.kotlin.eatitkotlinserver.callback.ILoadOrderCallback
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.model.Order
import java.util.*
import kotlin.collections.ArrayList

class OrderViewModel : ViewModel(), ILoadOrderCallback {

    private val mutableListOrder = MutableLiveData<List<Order>>()
    val mutableListErrorMessage = MutableLiveData<String>()
    private val orderCallback: ILoadOrderCallback = this

    fun getMutableListOrder(): MutableLiveData<List<Order>> {
        loadOrder(0)
        return mutableListOrder
    }

    fun loadOrder(status: Int) {
        Log.d("TaiPT4", "loadOrder $status")
        val tempList: MutableList<Order> = ArrayList()
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .orderByChild("orderStatus")
            .equalTo(status.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshotItem in snapshot.children) {
                        val order = snapshotItem.getValue(Order::class.java)
                        order!!.key = snapshotItem.key
                        tempList.add(order)
                    }
                    orderCallback.onLoadOrderSuccess(tempList)
                }

                override fun onCancelled(error: DatabaseError) {
                    orderCallback.onLoadOrderFailed(error.message)
                }

            })
    }


    override fun onLoadOrderSuccess(orderList: List<Order>) {
        Log.d("TaiPT4", "onLoadOrderSuccess ${orderList.size}")
        if (orderList.size >= 0) {
            Collections.sort(orderList) {t1, t2 ->
                if (t1.createdDate > t2.createdDate) return@sort -1
                if (t1.createdDate == t2.createdDate) 0 else 1
            }
            mutableListOrder.value = orderList
        }
    }

    override fun onLoadOrderFailed(message: String) {
        Log.d("TaiPT4", "onLoadOrderFailed $message")
        mutableListErrorMessage.value = message
    }

}