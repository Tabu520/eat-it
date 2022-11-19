package taipt4.kotlin.eatitv2.ui.menu

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import taipt4.kotlin.eatitv2.callback.ILoadCategoryCallback
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.model.Category
import taipt4.kotlin.eatitv2.model.PopularCategory

class MenuViewModel : ViewModel(), ILoadCategoryCallback {

    private var categoryMutableLiveData: MutableLiveData<List<Category>>? = null
    private var errorMessages: MutableLiveData<String> = MutableLiveData()
    private var loadCategoryCallback: ILoadCategoryCallback = this

    fun getCategoryList(): MutableLiveData<List<Category>> {
        if (categoryMutableLiveData == null) {
            categoryMutableLiveData = MutableLiveData()
            loadCategory()
        }
        return categoryMutableLiveData!!
    }

    fun getErrorMessage(): MutableLiveData<String> {
        return errorMessages
    }

    fun loadCategory() {
        Log.d("TaiPT4", "#loadCategory")
        val tempList = ArrayList<Category>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot.children) {
                    val model = itemSnapShot.getValue(Category::class.java)
                    model!!.menu_id = itemSnapShot.key
                    tempList.add(model)
                }
                loadCategoryCallback.onLoadCategorySuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                loadCategoryCallback.onLoadCategoryFailed(error.message)
            }

        })
    }


    override fun onLoadCategorySuccess(listCategory: List<Category>) {
        Log.d("TaiPT4", "#onLoadCategorySuccess")
        categoryMutableLiveData!!.value = listCategory
    }

    override fun onLoadCategoryFailed(errorMessage: String) {
        Log.d("TaiPT4", "#onLoadCategoryFailed")
        errorMessages.value = errorMessage
    }
}