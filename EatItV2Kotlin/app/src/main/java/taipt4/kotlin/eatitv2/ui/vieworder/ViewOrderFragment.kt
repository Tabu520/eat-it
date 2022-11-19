package taipt4.kotlin.eatitv2.ui.vieworder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.Unbinder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.adapter.OrderAdapter
import taipt4.kotlin.eatitv2.callback.ILoadOrderCallback
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.eventbus.MenuItemBack
import taipt4.kotlin.eatitv2.model.Order
import java.util.*
import kotlin.collections.ArrayList

class ViewOrderFragment: Fragment(), ILoadOrderCallback {

    private lateinit var viewOrderViewModel: ViewOrderViewModel
    private lateinit var unbinder: Unbinder

    private lateinit var dialog: AlertDialog
    private lateinit var recyclerViewOrder: RecyclerView

    internal var loadOrderCallback: ILoadOrderCallback = this

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOrderViewModel = ViewModelProvider(this).get(ViewOrderViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_view_orders, container, false)
        initViews(root)
        loadOrderFromFirebase()

        viewOrderViewModel.mutableLiveDataOrderList.observe(viewLifecycleOwner, {
            Collections.reverse(it)
            val adapter = OrderAdapter(requireContext(), it)
            recyclerViewOrder.adapter = adapter
        })

        return root
    }

    private fun loadOrderFromFirebase() {
        dialog.show()
        val orderList = ArrayList<Order>()

        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .orderByChild("userId")
            .equalTo(Common.currentUser!!.uid!!)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapShot in snapshot.children) {
                        val order = orderSnapShot.getValue(Order::class.java)
                        order!!.orderNumber = orderSnapShot.key
                        orderList.add(order)
                    }
                    loadOrderCallback.onLoadOrderSuccess(orderList)
                }

                override fun onCancelled(error: DatabaseError) {
                   loadOrderCallback.onLoadOrderFailed(error.message)
                }
            })
    }

    private fun initViews(root: View) {

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()

        recyclerViewOrder = root.findViewById(R.id.view_orders_recycler_order) as RecyclerView
        recyclerViewOrder.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewOrder.layoutManager = layoutManager
        recyclerViewOrder.addItemDecoration(DividerItemDecoration(requireContext(), layoutManager.orientation))


    }


    override fun onLoadOrderSuccess(orderList: List<Order>) {
        dialog.dismiss()
        viewOrderViewModel.setMutableLiveDataOrderList(orderList)
    }

    override fun onLoadOrderFailed(message: String) {
        dialog.dismiss()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}