package taipt4.kotlin.eatitv2.ui.vieworder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import taipt4.kotlin.eatitv2.model.Order

class ViewOrderViewModel: ViewModel() {

    val mutableLiveDataOrderList: MutableLiveData<List<Order>> = MutableLiveData()

    fun setMutableLiveDataOrderList(orderList: List<Order>) {
        mutableLiveDataOrderList.value = orderList
    }

}