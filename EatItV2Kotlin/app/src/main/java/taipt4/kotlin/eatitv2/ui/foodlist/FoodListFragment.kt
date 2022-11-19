package taipt4.kotlin.eatitv2.ui.foodlist

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.adapter.FoodListAdapter
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.eventbus.MenuItemBack
import taipt4.kotlin.eatitv2.model.Category
import taipt4.kotlin.eatitv2.model.Food
import java.util.*
import kotlin.collections.ArrayList

class FoodListFragment : Fragment() {

    private lateinit var foodListViewModel: FoodListViewModel
    private lateinit var recyclerFoodList: RecyclerView
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: FoodListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodListViewModel =
            ViewModelProvider(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list, container, false)

        initView(root)

        foodListViewModel.getFoodModelList().observe(viewLifecycleOwner, {
            if (it != null) {
                adapter = FoodListAdapter(requireContext(), it)
                recyclerFoodList.adapter = adapter
                recyclerFoodList.layoutAnimation = layoutAnimationController
            }
        })
        return root
    }

    private fun initView(root: View) {

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar!!.title = Common.selectedCategory!!.name!!

        recyclerFoodList = root.findViewById(R.id.recycler_food_list) as RecyclerView
        recyclerFoodList.setHasFixedSize(true)
        recyclerFoodList.layoutManager = LinearLayoutManager(context)

        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.selectedCategory!!.name
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        // Event
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                startSearch(query!!)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        // Clear text when click clear button on Search View
        val btnClose: ImageView = searchView.findViewById(R.id.search_close_btn)
        btnClose.setOnClickListener {
            val edtSearch: EditText = searchView.findViewById(R.id.search_src_text)
            // Clear text
            edtSearch.setText("")
            // Clear query
            searchView.setQuery("", false)
            // Collapse the action view
            searchView.onActionViewCollapsed()
            // Collapse the search widget
            menuItem.collapseActionView()
            // Restore result to original
            foodListViewModel.getFoodModelList()
        }
    }

    private fun startSearch(query: String) {
        val resultFood = ArrayList<Food>()
        for (food in Common.selectedCategory!!.foods!!) {
            if (food.name!!.toLowerCase(Locale.ROOT).contains(query)) {
                resultFood.add(food)
            }
        }
        foodListViewModel.getFoodModelList().value = resultFood
    }

    override fun onStop() {
        if (adapter != null) {
            adapter!!.onStop()
        }
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}