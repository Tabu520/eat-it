package taipt4.kotlin.eatitkotlinserver.common

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.eventbus.LoadOrderEvent

class BottomSheetOrderFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = LayoutInflater.from(context)
            .inflate(R.layout.fragment_order_filter, container, false)
        initViews(root)
        return root
    }

    private fun initViews(view: View) {
        val placedFilter = view.findViewById<LinearLayout>(R.id.placed_filter)
        val shippingFilter = view.findViewById<LinearLayout>(R.id.shipping_filter)
        val shippedFilter = view.findViewById<LinearLayout>(R.id.shipped_filter)
        val cancelledFilter = view.findViewById<LinearLayout>(R.id.cancelled_filter)



        placedFilter.setOnClickListener {
            Log.d("TaiPT4", "Click on placedFilter")
            EventBus.getDefault().postSticky(LoadOrderEvent(0))
            dismiss()
        }
        shippingFilter.setOnClickListener {
            Log.d("TaiPT4", "Click on placedFilter")
            EventBus.getDefault().postSticky(LoadOrderEvent(1))
            dismiss()
        }
        shippedFilter.setOnClickListener {
            Log.d("TaiPT4", "Click on placedFilter")
            EventBus.getDefault().postSticky(LoadOrderEvent(2))
            dismiss()
        }
        cancelledFilter.setOnClickListener {
            Log.d("TaiPT4", "Click on placedFilter")
            EventBus.getDefault().postSticky(LoadOrderEvent(-1))
            dismiss()
        }
    }

    companion object {
        private var instance: BottomSheetOrderFragment? = null

        fun getInstance(): BottomSheetOrderFragment {
            if (instance == null) {
                instance = BottomSheetOrderFragment()
            }
            return instance!!
        }
    }
}