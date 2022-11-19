package taipt4.kotlin.eatitv2.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.model.CommentModel
import taipt4.kotlin.eatitv2.model.Food

class FoodDetailViewModel : ViewModel() {

    private var foodMutableLiveData: MutableLiveData<Food>? = null
    private var commentMutableLiveData: MutableLiveData<CommentModel>? = null

    init {
        commentMutableLiveData = MutableLiveData()
    }

    fun getFoodMutableLiveData(): MutableLiveData<Food> {
        if (foodMutableLiveData == null) {
            foodMutableLiveData = MutableLiveData()
        }
        foodMutableLiveData!!.value = Common.selectedFood
        return foodMutableLiveData!!
    }

    fun getCommentMutableLiveData(): MutableLiveData<CommentModel> {
        if (commentMutableLiveData == null) {
            commentMutableLiveData = MutableLiveData()
        }
        return commentMutableLiveData!!
    }

    fun setCommentModel(commentModel: CommentModel) {
        if (commentMutableLiveData != null) {
            commentMutableLiveData!!.value = commentModel
        }
    }

    fun setFoodModel(food: Food) {
        if (foodMutableLiveData != null) {
            foodMutableLiveData!!.value = food
        }
    }

}