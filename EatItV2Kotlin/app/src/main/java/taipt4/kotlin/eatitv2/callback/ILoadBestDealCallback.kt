package taipt4.kotlin.eatitv2.callback

import taipt4.kotlin.eatitv2.model.BestDeal

interface ILoadBestDealCallback {

    fun onLoadBestDealSuccess(listBestDeal: List<BestDeal>)
    fun onLoadBestDealFailed(errorMessage: String)
}