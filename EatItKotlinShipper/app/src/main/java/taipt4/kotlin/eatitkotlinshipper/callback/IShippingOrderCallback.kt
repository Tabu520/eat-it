package taipt4.kotlin.eatitkotlinshipper.callback

import taipt4.kotlin.eatitkotlinshipper.model.ShippingOrderModel

interface IShippingOrderCallback {
    fun onLoadShippingOrderSuccess(shippingOrders: List<ShippingOrderModel>)
    fun onLoadShippingOrderFailure(message: String)
}