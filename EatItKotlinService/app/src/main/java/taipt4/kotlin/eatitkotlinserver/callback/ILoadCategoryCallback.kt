package taipt4.kotlin.eatitkotlinserver.callback

import taipt4.kotlin.eatitkotlinserver.model.Category

interface ILoadCategoryCallback {

    fun onLoadCategorySuccess(listCategory: List<Category>)
    fun onLoadCategoryFailed(errorMessage: String)
}