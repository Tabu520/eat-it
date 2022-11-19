package taipt4.kotlin.eatitv2.callback

import taipt4.kotlin.eatitv2.model.Category

interface ILoadCategoryCallback {

    fun onLoadCategorySuccess(listCategory: List<Category>)
    fun onLoadCategoryFailed(errorMessage: String)
}