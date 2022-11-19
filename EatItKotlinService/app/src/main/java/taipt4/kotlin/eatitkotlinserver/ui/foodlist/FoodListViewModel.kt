package taipt4.kotlin.eatitkotlinserver.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.model.Food

class FoodListViewModel : ViewModel() {

    private var foodMutableLiveData: MutableLiveData<List<Food>>? = null

    fun getFoodModelList(): MutableLiveData<List<Food>> {
        if (foodMutableLiveData == null) {
            foodMutableLiveData = MutableLiveData()
        }
        foodMutableLiveData!!.value = Common.selectedCategory!!.foods
        return foodMutableLiveData!!
    }
}