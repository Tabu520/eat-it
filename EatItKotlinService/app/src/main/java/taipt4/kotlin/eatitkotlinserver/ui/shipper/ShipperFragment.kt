package taipt4.kotlin.eatitkotlinserver.ui.shipper

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import taipt4.kotlin.eatitkotlinserver.R
import taipt4.kotlin.eatitkotlinserver.adapter.CategoryAdapter
import taipt4.kotlin.eatitkotlinserver.adapter.ShipperAdapter
import taipt4.kotlin.eatitkotlinserver.common.Common
import taipt4.kotlin.eatitkotlinserver.databinding.FragmentShipperBinding
import taipt4.kotlin.eatitkotlinserver.eventbus.ChangeMenuClick
import taipt4.kotlin.eatitkotlinserver.eventbus.UpdateActiveEvent
import taipt4.kotlin.eatitkotlinserver.model.Category
import taipt4.kotlin.eatitkotlinserver.model.Food
import taipt4.kotlin.eatitkotlinserver.model.Shipper

class ShipperFragment : Fragment() {

    companion object {
        fun newInstance() = ShipperFragment()
    }

    private var _binding: FragmentShipperBinding? = null
    private val binding get() = _binding!!
    private lateinit var shipperViewModel: ShipperViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: ShipperAdapter? = null
    internal var listShipper: List<Shipper> = ArrayList()
    internal var savedShipperListBeforeSearch: List<Shipper> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShipperBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initView(root)

        shipperViewModel = ViewModelProvider(this).get(ShipperViewModel::class.java)
        shipperViewModel.getListShipper().observe(viewLifecycleOwner, {
            dialog.dismiss()
            if (it != null) {
                listShipper = it
                if (savedShipperListBeforeSearch.isEmpty())
                    savedShipperListBeforeSearch = it
                adapter = ShipperAdapter(requireContext(), listShipper.toMutableList())
                binding.recyclerShipper.adapter = adapter
                binding.recyclerShipper.layoutAnimation = layoutAnimationController
            }
        })
        shipperViewModel.getErrorMessage().observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })

        return root
    }

    private fun initView(root: View) {

        setHasOptionsMenu(true)

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        binding.recyclerShipper.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerShipper.layoutManager = layoutManager
        binding.recyclerShipper.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu, menu)

        // Create search view
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        //Event
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                startSearchShipper(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        // Clear text when click Clear button
        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn)
        closeButton.setOnClickListener {
            val edtSearch = searchView.findViewById<EditText>(R.id.search_src_text)
            // Clear text
            edtSearch.setText("")
            // Clear query
            searchView.setQuery("", false)
            // Collapse the action view
            searchView.onActionViewCollapsed()
            // Collapse the search widget
            menuItem.collapseActionView()
            // Restore result to original
            shipperViewModel.getListShipper().value = savedShipperListBeforeSearch
        }
    }

    private fun startSearchShipper(query: String?) {
        val resultShipper: MutableList<Shipper> = ArrayList()
        for (i in listShipper.indices) {
            val shipper = listShipper[i]
            if (shipper.name!!.lowercase().contains(query!!.lowercase())) {
                resultShipper.add(shipper)
            }
        }
        // Update search result
        shipperViewModel.getListShipper().value = resultShipper
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateActiveEvent(event: UpdateActiveEvent) {
        val updateData = HashMap<String, Any>()
        updateData["active"] = event.isActive
        FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
            .child(event.shipper.uid!!)
            .updateChildren(updateData)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Update state to ${event.isActive}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateActiveEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateActiveEvent::class.java)
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        _binding = null
        super.onDestroy()
    }

}