package taipt4.kotlin.eatitv2.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import taipt4.kotlin.eatitv2.callback.ILoadBestDealCallback
import taipt4.kotlin.eatitv2.callback.ILoadPopularCategoryCallback
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.model.BestDeal
import taipt4.kotlin.eatitv2.model.PopularCategory

class HomeViewModel : ViewModel(), ILoadPopularCategoryCallback, ILoadBestDealCallback {

    private var listPopularCategories: MutableLiveData<List<PopularCategory>>? = null
    private var listBestDeals: MutableLiveData<List<BestDeal>>? = null
    private lateinit var errorMessages: MutableLiveData<String>
    private var loadPopularCategoryCallback: ILoadPopularCategoryCallback = this
    private var loadBestDealCallback: ILoadBestDealCallback = this

    val listLoadedPopularCategories: LiveData<List<PopularCategory>>
        get() {
            if (listPopularCategories == null) {
                listPopularCategories = MutableLiveData()
                errorMessages = MutableLiveData()
                loadPopularCategory()
            }
            return listPopularCategories!!
        }

    val listLoadedBestDeals: LiveData<List<BestDeal>>
        get() {
            if (listBestDeals == null) {
                listBestDeals = MutableLiveData()
                errorMessages = MutableLiveData()
                loadBestDeal()
            }
            return listBestDeals!!
        }

    /* --------- POPULAR CATEGORY ------------*/
    private fun loadPopularCategory() {
        Log.d("TaiPT4", "#loadPopularCategory")
        val tempList = ArrayList<PopularCategory>()
        val popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot.children) {
                    val model = itemSnapShot.getValue(PopularCategory::class.java)
                    tempList.add(model!!)
                }
                loadPopularCategoryCallback.onLoadPopularSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                loadPopularCategoryCallback.onLoadPopularFailed(error.message)
            }

        })
    }

    override fun onLoadPopularSuccess(listPopularCategory: List<PopularCategory>) {
        Log.d("TaiPT4", "#onLoadPopularSuccess")
        listPopularCategories!!.value = listPopularCategory
    }

    override fun onLoadPopularFailed(errorMessage: String) {
        Log.d("TaiPT4", "#onLoadPopularFailed")
        errorMessages.value = errorMessage
    }

    /* --------- BEST DEAL ------------*/
    private fun loadBestDeal() {
        Log.d("TaiPT4", "#loadBestDeal")
        val tempList = ArrayList<BestDeal>()
        val bestDealRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEAL_REF)
        bestDealRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot.children) {
                    val model = itemSnapShot.getValue(BestDeal::class.java)
                    Log.d("TaiPT4", model!!.image.toString())
                    tempList.add(model)
                }
                loadBestDealCallback.onLoadBestDealSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                loadBestDealCallback.onLoadBestDealFailed(error.message)
            }

        })
    }

    override fun onLoadBestDealSuccess(listBestDeal: List<BestDeal>) {
        Log.d("TaiPT4", "#onLoadBestDealSuccess")
        listBestDeals!!.value = listBestDeal
    }

    override fun onLoadBestDealFailed(errorMessage: String) {
        Log.d("TaiPT4", "#onLoadBestDealFailed")
        errorMessages.value = errorMessage
    }
}