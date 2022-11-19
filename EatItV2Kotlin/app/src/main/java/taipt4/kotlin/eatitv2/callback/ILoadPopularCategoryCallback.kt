package taipt4.kotlin.eatitv2.callback

import taipt4.kotlin.eatitv2.model.PopularCategory

interface ILoadPopularCategoryCallback {

    fun onLoadPopularSuccess(listPopularCategory: List<PopularCategory>)
    fun onLoadPopularFailed(errorMessage: String)
}