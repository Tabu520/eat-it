package taipt4.kotlin.eatitkotlinshipper.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import taipt4.kotlin.eatitkotlinshipper.callback.IShippingOrderCallback
import taipt4.kotlin.eatitkotlinshipper.common.Common
import taipt4.kotlin.eatitkotlinshipper.model.ShippingOrderModel

class HomeViewModel : ViewModel(), IShippingOrderCallback {

    private var orderModelMutableLiveData: MutableLiveData<List<ShippingOrderModel>> =
        MutableLiveData()
    private var errorMessages: MutableLiveData<String> = MutableLiveData()
    private var listener: IShippingOrderCallback = this

    fun getOrderModelMutableLiveData(shipperPhone: String): MutableLiveData<List<ShippingOrderModel>> {
        loadOrderByShipper(shipperPhone)
        return orderModelMutableLiveData
    }

    private fun loadOrderByShipper(shipperPhone: String) {
        val tempList: MutableList<ShippingOrderModel> = ArrayList()
        val orderRef = FirebaseDatabase.getInstance()
            .getReference(Common.SHIPPING_ORDER_REF)
    }

    override fun onLoadShippingOrderSuccess(shippingOrders: List<ShippingOrderModel>) {
        orderModelMutableLiveData.value = shippingOrders
    }

    override fun onLoadShippingOrderFailure(message: String) {
        errorMessages.value = message
    }
}