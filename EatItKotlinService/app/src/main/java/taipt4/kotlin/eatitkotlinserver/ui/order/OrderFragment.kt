package taipt4.kotlin.eatitkotlinserver.ui.order

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.adapter.OrderAdapter
import taipt4.kotlin.eatitkotlinserver.adapter.SelectedShipperAdapter
import taipt4.kotlin.eatitkotlinserver.callback.IButtonCallback
import taipt4.kotlin.eatitkotlinserver.callback.ILoadShipperCallback
import taipt4.kotlin.eatitkotlinserver.common.BottomSheetOrderFragment
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.common.SwipeHelper
import taipt4.kotlin.eatitkotlinserver.databinding.FragmentOrderBinding
import taipt4.kotlin.eatitkotlinserver.eventbus.ChangeMenuClick
import taipt4.kotlin.eatitkotlinserver.eventbus.LoadOrderEvent
import taipt4.kotlin.eatitkotlinserver.model.*
import taipt4.kotlin.eatitkotlinserver.remote.IFCMServices
import taipt4.kotlin.eatitkotlinserver.remote.RetrofitFCMClient

class OrderFragment : Fragment(), ILoadShipperCallback {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()
    private lateinit var iFcmServices: IFCMServices
    private lateinit var layoutAnimationController: LayoutAnimationController
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var adapter: OrderAdapter
    private var selectedShipperAdapter: SelectedShipperAdapter? = null
    private var loadShipperCallback: ILoadShipperCallback = this
    private var recyclerShipper: RecyclerView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initView(root)

        orderViewModel = ViewModelProvider(this).get(OrderViewModel::class.java)
        orderViewModel.mutableListErrorMessage.observe(viewLifecycleOwner, { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        })
        orderViewModel.getMutableListOrder().observe(viewLifecycleOwner, { listOrder ->
            if (listOrder != null) {
                adapter = OrderAdapter(requireContext(), listOrder.toMutableList())
                binding.recyclerOrderList.adapter = adapter
                binding.recyclerOrderList.layoutAnimation = layoutAnimationController
                updateTextCounter()
            }
        })

        return root
    }

    private fun initView(root: View) {

        iFcmServices = RetrofitFCMClient.getInstance().create(IFCMServices::class.java)
        setHasOptionsMenu(true)

        binding.recyclerOrderList.setHasFixedSize(true)
        binding.recyclerOrderList.layoutManager = LinearLayoutManager(context)

        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        val displayMetrics = DisplayMetrics()
        var width: Int = 0
        var height: Int = 0
        val version =
            Build.VERSION.SDK_INT   //Check android version. Returns API # ie 29 (Android 10), 30 (Android 11)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {    //If API is version 30 (Android 11) or greater, use the new method for getting width/height
            requireActivity().display?.getRealMetrics(displayMetrics) //New method
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        } else {
            @Suppress("DEPRECATION")    //Suppress the "Deprecation" warning
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        }

        val swipe = object : SwipeHelper(requireContext(), binding.recyclerOrderList, width / 6) {
            override fun instantiateCustomButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<CustomButton>
            ) {
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Directions",
                        35,
                        0,
                        Color.parseColor("#9b0000"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {

                            }
                        })
                )
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Call",
                        35,
                        0,
                        Color.parseColor("#560027"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                Dexter.withActivity(requireActivity())
                                    .withPermission(android.Manifest.permission.CALL_PHONE)
                                    .withListener(object : PermissionListener {
                                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                                            val order = adapter.getItemAtPosition(pos)
                                            val intent = Intent()
                                            intent.action = Intent.ACTION_DIAL
                                            intent.data = Uri.parse(
                                                StringBuilder("tel: ").append(order.userPhone)
                                                    .toString()
                                            )
                                            startActivity(intent)
                                        }

                                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                                            Toast.makeText(
                                                context,
                                                "You must accept ${response!!.permissionName}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onPermissionRationaleShouldBeShown(
                                            permission: PermissionRequest?,
                                            token: PermissionToken?
                                        ) {

                                        }

                                    }).check()
                            }
                        })
                )
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Remove",
                        35,
                        0,
                        Color.parseColor("#12005e"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                val order = adapter.getItemAtPosition(pos)
                                val builder = AlertDialog.Builder(requireContext())
                                    .setTitle("Delete")
                                    .setMessage("Do you really want to delete this order?")
                                    .setNegativeButton("CANCEL") { dialogInterface, _ -> dialogInterface.dismiss() }
                                    .setPositiveButton("DELETE") { dialogInterface, _ ->
                                        FirebaseDatabase.getInstance()
                                            .getReference(Common.ORDER_REF)
                                            .child(order.key!!)
                                            .removeValue()
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    e.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnSuccessListener {
                                                adapter.removeItem(pos)
                                                adapter.notifyItemRemoved(pos)
                                                updateTextCounter()
                                                dialogInterface.dismiss()
                                                Toast.makeText(
                                                    requireContext(),
                                                    "The order has been deleted",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                val dialog = builder.create()
                                dialog.show()
                                dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                                    .setTextColor(Color.LTGRAY)
                                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                    .setTextColor(Color.RED)
                            }
                        })
                )
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Edit",
                        35,
                        0,
                        Color.parseColor("#333639"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                showEditDialog(adapter.getItemAtPosition(pos), pos)
                            }
                        })
                )
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun showEditDialog(order: Order, pos: Int) {
        var layoutDialog: View? = null
        var builder: AlertDialog.Builder? = null
        var rdiShipping: RadioButton? = null
        var rdiShipped: RadioButton? = null
        var rdiCancelled: RadioButton? = null
        var rdiDelete: RadioButton? = null
        var rdiRestorePlaced: RadioButton? = null

        when (order.orderStatus) {
            -1 -> {
                layoutDialog = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_dialog_cancelled, null)
                builder = AlertDialog.Builder(requireContext()).setView(layoutDialog)
                rdiDelete = layoutDialog.findViewById(R.id.rdi_delete)
                rdiRestorePlaced = layoutDialog.findViewById(R.id.rdi_restore_placed)
            }
            0 -> {
                layoutDialog = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_dialog_shipping, null)
                recyclerShipper = layoutDialog.findViewById(R.id.recycler_shipper) as RecyclerView
                builder = AlertDialog.Builder(
                    requireContext(),
                    android.R.style.Theme_Material_Light_NoActionBar_Fullscreen
                ).setView(layoutDialog)
                rdiShipping = layoutDialog.findViewById(R.id.rdi_shipping)
                rdiCancelled = layoutDialog.findViewById(R.id.rdi_cancelled)
            }
            else -> {
                layoutDialog = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_dialog_shipped, null)
                builder = AlertDialog.Builder(requireContext()).setView(layoutDialog)
                rdiCancelled = layoutDialog.findViewById(R.id.rdi_cancelled)
                rdiShipped = layoutDialog.findViewById(R.id.rdi_shipped)
            }
        }
        // View
        val btnOk: Button = layoutDialog.findViewById(R.id.btn_ok)
        val btnCancel: Button = layoutDialog.findViewById(R.id.btn_cancel)
        val txtStatus: TextView = layoutDialog.findViewById(R.id.txt_status)

        // Set data
        txtStatus.text = "Order status (${Common.convertStatusToString(order.orderStatus)})"

        // Create dialog
        val dialog = builder.create()
        if (order.orderStatus == 0) {
            loadListShipper(
                pos, order, dialog, btnOk, btnCancel, rdiShipping,
                rdiShipped, rdiCancelled, rdiDelete, rdiRestorePlaced
            )
        } else {
            showDialog(
                pos, order, dialog, btnOk, btnCancel, rdiShipping,
                rdiShipped, rdiCancelled, rdiDelete, rdiRestorePlaced
            )
        }

    }

    private fun loadListShipper(
        pos: Int,
        order: Order,
        dialog: AlertDialog,
        btnOk: Button,
        btnCancel: Button,
        rdiShipping: RadioButton?,
        rdiShipped: RadioButton?,
        rdiCancelled: RadioButton?,
        rdiDelete: RadioButton?,
        rdiRestorePlaced: RadioButton?
    ) {
        val tempList: MutableList<Shipper> = ArrayList()
        val shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
        val shipperActive = shipperRef.orderByChild("active").equalTo(true)
        shipperActive.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (shipperSnapshot in snapshot.children) {
                    val shipperModel = shipperSnapshot.getValue(Shipper::class.java)
                    shipperModel!!.key = shipperSnapshot.key
                    tempList.add(shipperModel)
                }
                loadShipperCallback.onLoadShipperSuccess(
                    pos, order, tempList, dialog, btnOk, btnCancel,
                    rdiShipping, rdiShipped, rdiCancelled, rdiDelete, rdiRestorePlaced
                )
            }

            override fun onCancelled(error: DatabaseError) {
                loadShipperCallback.onLoadShipperFailed(error.message)
            }

        })
    }

    private fun showDialog(
        pos: Int,
        order: Order,
        dialog: AlertDialog,
        btnOk: Button,
        btnCancel: Button,
        rdiShipping: RadioButton?,
        rdiShipped: RadioButton?,
        rdiCancelled: RadioButton?,
        rdiDelete: RadioButton?,
        rdiRestorePlaced: RadioButton?
    ) {
        dialog.show()
        // Custom dialog
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnOk.setOnClickListener {
            if (rdiCancelled != null && rdiCancelled.isChecked) {
                updateOrder(pos, order, -1)
                dialog.dismiss()
            } else if (rdiRestorePlaced != null && rdiRestorePlaced.isChecked) {
                updateOrder(pos, order, 0)
                dialog.dismiss()
            } else if (rdiShipping != null && rdiShipping.isChecked) {
//                updateOrder(pos, order, 1)
                var shipper: Shipper?
                if (selectedShipperAdapter != null) {
                    shipper = selectedShipperAdapter!!.selectedShipper
                    if (shipper != null) {
//                        Toast.makeText(requireContext(), shipper.name, Toast.LENGTH_SHORT).show()
//                        dialog.dismiss()
                        createShippingOrder(shipper, order, dialog)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please choose shipper!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else if (rdiShipped != null && rdiShipped.isChecked) {
                updateOrder(pos, order, 2)
                dialog.dismiss()
            } else if (rdiDelete != null && rdiDelete.isChecked) {
                deleteOrder(pos, order)
                dialog.dismiss()
            }
        }
    }

    private fun createShippingOrder(shipper: Shipper, order: Order, dialog: AlertDialog) {
        val shippingOrder = ShippingOrderModel()
        shippingOrder.shipperName = shipper.name
        shippingOrder.shipperPhone = shipper.phone
        shippingOrder.orderModel = order
        shippingOrder.isStartTrip = false
        shippingOrder.currentLat = -1.0
        shippingOrder.currentLng = -1.0
        FirebaseDatabase.getInstance().getReference(Common.SHIPPING_ORDER_REF)
            .push()
            .setValue(shippingOrder)
            .addOnFailureListener { e: Exception ->
                dialog.dismiss()
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    dialog.dismiss()
                    Toast.makeText(
                        context,
                        "Order has been sent to shipper ${shipper.name}!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun deleteOrder(pos: Int, order: Order) {
        if (!TextUtils.isEmpty(order.key)) {
            FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .child(order.key!!)
                .removeValue()
                .addOnFailureListener { e ->
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener {
                    adapter.removeItem(pos)
                    adapter.notifyItemRemoved(pos)
                    updateTextCounter()
                    Toast.makeText(context, "Delete success", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Order number must not be null or empty!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateOrder(pos: Int, order: Order, status: Int) {
        if (!TextUtils.isEmpty(order.key)) {
            val updateData = HashMap<String, Any>()
            updateData["orderStatus"] = status
            FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .child(order.key!!)
                .updateChildren(updateData)
                .addOnFailureListener { e ->
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener {
                    val dialog =
                        SpotsDialog.Builder().setContext(requireContext()).setCancelable(false)
                            .build()
                    dialog.show()

                    // Load token
                    FirebaseDatabase.getInstance().getReference(Common.TOKEN_REF)
                        .child(order.userId!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val tokenModel = snapshot.getValue(TokenModel::class.java)
                                    val notificationData = HashMap<String, String>()
                                    notificationData[Common.NOTI_TITLE] =
                                        "Your order has been updated!"
                                    notificationData[Common.NOTI_CONTENT] =
                                        StringBuilder("Your order ")
                                            .append(order.key).append(" has been updated to ")
                                            .append(Common.convertStatusToString(status)).toString()
                                    val sendData =
                                        FCMSendData(tokenModel!!.token!!, notificationData)
                                    compositeDisposable.add(
                                        iFcmServices.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({ fcmResponse ->
                                                dialog.dismiss()
                                                if (fcmResponse.success == 1) {
                                                    Toast.makeText(
                                                        context,
                                                        "Update order successful",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Send notification failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }, { throwable ->
                                                Toast.makeText(
                                                    context,
                                                    throwable.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            })
                                    )
                                } else {
                                    dialog.dismiss()
                                    Toast.makeText(
                                        requireContext(),
                                        "Token not found!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                dialog.dismiss()
                                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        })

                    adapter.removeItem(pos)
                    adapter.notifyItemRemoved(pos)
                    updateTextCounter()
                }
        } else {
            Toast.makeText(context, "Order number must not be null or empty!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTextCounter() {
        binding.orderTxtOrderFilter.text = "Orders (${adapter.itemCount})"
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                Log.d("TaiPT4", "onOptionsItemSelected action_filter")
                val bottomSheet = BottomSheetOrderFragment.getInstance()
                bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            }
        }
        return true
    }

    override fun onLoadShipperSuccess(listShipper: List<Shipper>) {

    }

    override fun onLoadShipperSuccess(
        pos: Int,
        order: Order?,
        listShipper: List<Shipper>?,
        dialog: AlertDialog?,
        ok: Button?,
        cancel: Button?,
        rdiShipping: RadioButton?,
        rdiShipped: RadioButton?,
        rdiCancelled: RadioButton?,
        rdiDelete: RadioButton?,
        rdiRestorePlaced: RadioButton?
    ) {
        if (recyclerShipper != null) {
            recyclerShipper!!.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(context)
            recyclerShipper!!.layoutManager = layoutManager
            recyclerShipper!!.addItemDecoration(
                DividerItemDecoration(
                    context,
                    layoutManager.orientation
                )
            )

            selectedShipperAdapter = SelectedShipperAdapter(requireContext(), listShipper!!)
            recyclerShipper!!.adapter = selectedShipperAdapter
        }
        showDialog(
            pos,
            order!!,
            dialog!!,
            ok!!,
            cancel!!,
            rdiShipping,
            rdiShipped,
            rdiCancelled,
            rdiDelete,
            rdiRestorePlaced
        )
    }

    override fun onLoadShipperFailed(errorMessage: String) {
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLoadOrder(event: LoadOrderEvent) {
        Log.d("TaiPT4", "onLoadOrder ${event.status}")
        orderViewModel.loadOrder(event.status)
    }

    override fun onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent::class.java))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent::class.java)
        EventBus.getDefault().unregister(this)
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

}