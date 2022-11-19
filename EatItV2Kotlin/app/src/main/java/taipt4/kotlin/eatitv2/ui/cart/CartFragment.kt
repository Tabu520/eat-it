package taipt4.kotlin.eatitv2.ui.cart

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.adapter.CartAdapter
import taipt4.kotlin.eatitv2.callback.IButtonCallback
import taipt4.kotlin.eatitv2.callback.ILoadTimeFromFirebaseCallback
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.common.SwipeHelper
import taipt4.kotlin.eatitv2.database.CartDataSource
import taipt4.kotlin.eatitv2.database.CartDatabase
import taipt4.kotlin.eatitv2.database.LocalCartDataSource
import taipt4.kotlin.eatitv2.eventbus.CountCartEvent
import taipt4.kotlin.eatitv2.eventbus.HideFabCart
import taipt4.kotlin.eatitv2.eventbus.MenuItemBack
import taipt4.kotlin.eatitv2.eventbus.UpdateItemCart
import taipt4.kotlin.eatitv2.model.FCMSendData
import taipt4.kotlin.eatitv2.model.Order
import taipt4.kotlin.eatitv2.remote.ICloudFunctions
import taipt4.kotlin.eatitv2.remote.IFCMServices
import taipt4.kotlin.eatitv2.remote.RetrofitCloudClient
import taipt4.kotlin.eatitv2.remote.RetrofitFCMClient
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CartFragment : Fragment(), ILoadTimeFromFirebaseCallback {

    private val REQUEST_BRAINTREE_CODE: Int = 8888
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter: CartAdapter
    private var loadTimeCallback: ILoadTimeFromFirebaseCallback = this

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource: CartDataSource
    private lateinit var recyclerViewState: Parcelable

    private lateinit var txtEmptyCart: TextView
    private lateinit var txtTotalPrice: TextView
    private lateinit var groupPlaceHolder: CardView
    private lateinit var recyclerCart: RecyclerView
    private lateinit var btnPlaceOrder: Button

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    internal var address: String = ""
    internal var comment: String = ""

    private lateinit var cloudFunctions: ICloudFunctions
    private lateinit var iFcmService: IFCMServices
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
            val nonce = intent!!.paymentMethodNonce

            // calculate sum cart
            cartDataSource.sumPrice(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<Double> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(totalPrice: Double) {
                        // Get all items to create cart
                        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser!!.uid!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ listCartItem ->
                                // After have all cart items, we will submit cart payment
                                val headers = HashMap<String, String>()
                                headers["Authorization"] = Common.buildToken(Common.authorizeToken)
                                compositeDisposable.add(cloudFunctions.submitPayment(headers, totalPrice, nonce!!.nonce)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ braintreeTransaction ->
                                        if (braintreeTransaction.success) {
                                            // Create order
                                            val finalPrice = totalPrice
                                            val order = Order()
                                            order.userId = Common.currentUser!!.uid
                                            order.userName = Common.currentUser!!.name
                                            order.userPhone = Common.currentUser!!.phone
                                            order.shippingAddress = address
                                            order.comment = comment
                                            order.lat = currentLocation.latitude
                                            order.lng = currentLocation.longitude
                                            order.cartItemList = listCartItem
                                            order.totalPayment = totalPrice
                                            order.finalPayment = finalPrice
                                            order.discount = 0
                                            order.isCod = false
                                            order.transactionId = braintreeTransaction.transaction!!.id

                                            // Submit to firebase
                                            syncLocalTimeWithServerTime(order)
                                        }
                                    }, { throwable ->
                                        Toast.makeText(context, throwable.message, Toast.LENGTH_SHORT)
                                                .show()
                                    }))
                            }, { throwable ->
                                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_SHORT).show()
                            })
                        )
                    }

                    override fun onError(e: Throwable) {
                        if (!e.message!!.contains("Query returned empty"))
                            Toast.makeText(context, "[SUM CART] ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                    }

                })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        EventBus.getDefault().postSticky(HideFabCart(true))

        cartViewModel =
            ViewModelProvider(this).get(CartViewModel::class.java)
        // Init datasource
        cartViewModel.initCartDataSource(requireContext())
        val root = inflater.inflate(R.layout.fragment_cart, container, false)
        initView(root)
        initLocation()
        cartViewModel.getMutableLiveDataCartItem().observe(viewLifecycleOwner, {
            if (it == null || it.isEmpty()) {
                recyclerCart.visibility = View.GONE
                groupPlaceHolder.visibility = View.GONE
                txtEmptyCart.visibility = View.VISIBLE
            } else {
                recyclerCart.visibility = View.VISIBLE
                groupPlaceHolder.visibility = View.VISIBLE
                txtEmptyCart.visibility = View.GONE

                adapter = CartAdapter(requireContext(), it)
                recyclerCart.adapter = adapter
            }
        })
        return root
    }

    @SuppressLint("MissingPermission")
    private fun initView(root: View) {

        setHasOptionsMenu(true) // Important!! If not add it, menu will never be inflated

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        iFcmService = RetrofitFCMClient.getInstance().create(IFCMServices::class.java)

        recyclerCart = root.findViewById(R.id.cart_recycler) as RecyclerView
        recyclerCart.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerCart.layoutManager = layoutManager
        recyclerCart.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        val swipe = object : SwipeHelper(requireContext(), recyclerCart, 200) {
            override fun instantiateCustomButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<CustomButton>
            ) {
                buffer.add(
                    CustomButton(
                        requireContext(),
                        "Delete",
                        30,
                        0,
                        Color.parseColor("#FF3C30"),
                        object : IButtonCallback {
                            override fun onClick(pos: Int) {
                                val deleteItem = adapter.getItemAtPosition(pos)
                                cartDataSource.deleteCart(deleteItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : SingleObserver<Int> {
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun onSuccess(t: Int) {
                                            adapter.notifyItemRemoved(pos)
                                            sumCart()
                                            EventBus.getDefault().postSticky(CountCartEvent(true))
                                            Toast.makeText(
                                                context,
                                                "Delete item successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onError(e: Throwable) {
                                            Toast.makeText(
                                                context,
                                                "[DELETE CART]123121414 ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    })
                            }
                        })
                )
            }

        }

        txtEmptyCart = root.findViewById(R.id.cart_empty_cart) as TextView
        txtTotalPrice = root.findViewById(R.id.cart_txt_total_price) as TextView
        groupPlaceHolder = root.findViewById(R.id.cart_group_place_holder) as CardView
        btnPlaceOrder = root.findViewById(R.id.cart_btn_place_order) as Button

        btnPlaceOrder.setOnClickListener {
            Log.d("TaiPT4", "#btnPlaceOrder.setOnClickListener")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("One more step!")

            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_place_order, null)
            val edtAddress = view.findViewById(R.id.place_order_edt_address) as TextInputEditText
            val edtComment = view.findViewById(R.id.place_order_edt_comment) as TextInputEditText
            val rdiHomeAddress = view.findViewById(R.id.place_order_rdi_home_address) as RadioButton
            val rdiOtherAddress =
                view.findViewById(R.id.place_order_rdi_other_address) as RadioButton
            val rdiThisAddress = view.findViewById(R.id.place_order_rdi_this_address) as RadioButton
            val rdiCOD = view.findViewById(R.id.place_order_rdi_cod) as RadioButton
            val rdiBraintree = view.findViewById(R.id.place_order_rdi_braintree) as RadioButton
            val txtAddressDetail =
                view.findViewById(R.id.place_order_txt_address_detail) as TextView

            // Set data
            edtAddress.setText(Common.currentUser!!.address!!)

            // Event
            rdiHomeAddress.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    edtAddress.setText(Common.currentUser!!.address!!)
                    txtAddressDetail.visibility = View.GONE
                }
            }
            rdiOtherAddress.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    edtAddress.setText("")
                    txtAddressDetail.visibility = View.GONE
                }
            }
            rdiThisAddress.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    fusedLocationProviderClient.lastLocation
                        .addOnFailureListener {
                            txtAddressDetail.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                        .addOnSuccessListener {
                            val coordinates = StringBuilder().append(it.latitude).append("/").append(it.longitude).toString()

                            val singleAddress = Single.just(getAddressFromLatLng(it.latitude, it.longitude))
                            val disposable = singleAddress.subscribeWith(object: DisposableSingleObserver<String>() {
                                override fun onSuccess(t: String) {
                                    edtAddress.setText(coordinates)
                                    txtAddressDetail.visibility = View.VISIBLE
                                    txtAddressDetail.text = t
                                }

                                override fun onError(e: Throwable) {
                                    edtAddress.setText(coordinates)
                                    txtAddressDetail.visibility = View.VISIBLE
                                    txtAddressDetail.text = e.message
                                }

                            })

                        }
                }
            }

            builder.setView(view)
            builder.setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("YES") { dialog, _ ->
                    if (rdiCOD.isChecked) {
                        paymentCOD(edtAddress.text.toString(), edtComment.text.toString())
                    } else if (rdiBraintree.isChecked) {
                        address = edtAddress.text.toString()
                        comment = edtComment.text.toString()
                        if (!TextUtils.isEmpty(Common.currentToken)) {
                            val dropInRequest = DropInRequest().clientToken(Common.currentToken)
//                            startActivityForResult(dropInRequest.getIntent(context), REQUEST_BRAINTREE_CODE)

                            startForResult.launch(dropInRequest.getIntent(context))
                        }
                    }
                }

            val dialog = builder.setView(view)
            dialog.show()
        }

    }

    private fun paymentCOD(address: String, comment: String) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                cartDataSource.sumPrice(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object: SingleObserver<Double> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(t: Double) {
                            val finalPrice = t
                            val order = Order()
                            order.userId = Common.currentUser!!.uid
                            order.userName = Common.currentUser!!.name
                            order.userPhone = Common.currentUser!!.phone
                            order.shippingAddress = address
                            order.comment = comment
                            order.lat = currentLocation.latitude
                            order.lng = currentLocation.longitude
                            order.cartItemList = it
                            order.totalPayment = t
                            order.finalPayment = finalPrice
                            order.discount = 0
                            order.isCod = true
                            order.transactionId = "Cash On Delivery"

                            // Submit to firebase
                            syncLocalTimeWithServerTime(order)
                        }

                        override fun onError(e: Throwable) {
                            if (!e.message!!.contains("Query returned empty"))
                                Toast.makeText(context, "[SUM CART] ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                        }

                    })
            }, { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }))
    }

    private fun syncLocalTimeWithServerTime(order: Order) {
        val offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
        offsetRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offset = snapshot.getValue(Long::class.java)
                val estimatedServerTimeInMs =  System.currentTimeMillis() + offset!! // Add missing offset to your current time
                val sdf = SimpleDateFormat("MMM dd yyyy, HH:mm")
                val date = Date(estimatedServerTimeInMs)
                Log.d("TaiPT4", "Estimated time = " + sdf.format(date))
                loadTimeCallback.onLoadTimeSuccess(order, estimatedServerTimeInMs)
            }

            override fun onCancelled(error: DatabaseError) {
                loadTimeCallback.onLoadTimeFailed(error.message)
            }

        })
    }

    private fun writeOrderToFirebase(order: Order) {
        FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .child(Common.createOrderNumber())
            .setValue(order)
            .addOnFailureListener { error -> Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener {
                // Clean cart
                if (it.isSuccessful) {
                    cartDataSource.cleanCart(Common.currentUser!!.uid!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object: SingleObserver<Int> {
                            override fun onSubscribe(d: Disposable) {

                            }
                            override fun onSuccess(t: Int) {
                                val dataSend = HashMap<String, String>()
                                dataSend[Common.NOTI_TITLE] = "New order"
                                dataSend[Common.NOTI_CONTENT] = "You have new order " + Common.currentUser!!.phone
                                val sendData = FCMSendData(Common.getNewOrderTopic(), dataSend)
                                compositeDisposable.add(
                                    iFcmService.sendNotification(sendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe (
                                            { fcmResponse ->
                                                if (fcmResponse.success != 0)
                                                    Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()
                                            }, { e ->
                                                Toast.makeText(requireContext(), "Order has been sent but notification failed", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                )

                            }
                            override fun onError(e: Throwable) {
                                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val result: String
        try {
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
            result = if (addressList != null && addressList.size > 0) {
                val address = addressList[0]
                StringBuilder(address.getAddressLine(0)).toString()
            } else {
                "Address not found!"
            }
        } catch (e: IOException) {
            return e.message!!
        }
        return result
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currentLocation = p0.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 10f
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemCart) {
        Log.d(
            "TaiPT4",
            "onUpdateItemInCart --- #newValue == ${event.cartItem.foodExtraPrice} -- $cartDataSource"
        )
        recyclerViewState = recyclerCart.layoutManager!!.onSaveInstanceState()!!
        cartDataSource.updateCart(event.cartItem)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Int) {
                    Log.d("TaiPT4", "onSuccess --- #newValue == $t")
                    calculateTotalPrice()
                    recyclerCart.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(
                        context,
                        "[UPDATE CART]123121414 ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_settings).isVisible = false // Hide Setting menu when in Cart
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        Toast.makeText(context, "Clear cart successfully", Toast.LENGTH_SHORT)
                            .show()
                        EventBus.getDefault().postSticky(CountCartEvent(true))
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }

                })
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun calculateTotalPrice() {
        cartDataSource.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(price: Double) {
                    txtTotalPrice.text = StringBuilder("Total: $").append(Common.formatPrice(price))
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "[SUM CART] ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                }

            })
    }

    private fun sumCart() {
        cartDataSource.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Double) {
                    txtTotalPrice.text = StringBuilder("Total: $").append(t)
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty")) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

            })
    }

    override fun onLoadTimeSuccess(order: Order, estimatedTime: Long) {
        order.createdDate = estimatedTime
        order.orderStatus = 0
        writeOrderToFirebase(order)
    }

    override fun onLoadTimeFailed(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        cartViewModel.onStop()
        compositeDisposable.clear()
        EventBus.getDefault().postSticky(HideFabCart(false))
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }

}