package taipt4.kotlin.eatitv2.ui.menu

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.adapter.CategoryAdapter
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.common.SpacesItemDecoration
import taipt4.kotlin.eatitv2.eventbus.MenuItemBack
import taipt4.kotlin.eatitv2.model.Category
import java.util.*
import kotlin.collections.ArrayList

class MenuFragment : Fragment() {

    private lateinit var menuViewModel: MenuViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: CategoryAdapter? = null

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TaiPT4", "MenuFragment --- #onCreateView")
        menuViewModel =
            ViewModelProvider(this).get(MenuViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_category, container, false)
        initView(root)

        menuViewModel.getErrorMessage().observe(viewLifecycleOwner, {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        menuViewModel.getCategoryList().observe(viewLifecycleOwner, {
            dialog.dismiss()
            adapter = CategoryAdapter(requireContext(), it)
            recyclerView.adapter = adapter
            recyclerView.layoutAnimation = layoutAnimationController
        })
        return root
    }

    private fun initView(root: View) {
        Log.d("TaiPT4", "MenuFragment --- #initViews")

        setHasOptionsMenu(true)

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()

        recyclerView = root.findViewById(R.id.recycler_view_menu)

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        recyclerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter != null)
                {
                    when(adapter!!.getItemViewType(position)) {
                        Common.DEFAULT_COLUMN_COUNT -> 1
                        Common.FULL_WIDTH_COLUMN -> 2
                        else -> -1
                    }
                } else {
                    -1
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(SpacesItemDecoration(8))
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
            menuViewModel.loadCategory()
        }
    }

    private fun startSearch(query: String) {
        val resultCategory = ArrayList<Category>()
        for (category in adapter!!.categories) {
            if (category.name!!.toLowerCase(Locale.ROOT).contains(query)) {
                resultCategory.add(category)
            }
        }
        menuViewModel.getCategoryList().value = resultCategory
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}