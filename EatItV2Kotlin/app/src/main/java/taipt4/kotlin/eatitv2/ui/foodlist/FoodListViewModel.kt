package taipt4.kotlin.eatitv2.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.model.Food

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