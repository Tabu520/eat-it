package taipt4.kotlin.eatitv2.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.adapter.BestDealAdapter
import taipt4.kotlin.eatitv2.adapter.PopularCategoryAdapter
import taipt4.kotlin.eatitv2.eventbus.MenuItemBack

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var loopingViewPager: LoopingViewPager
    private lateinit var layoutAnimationController: LayoutAnimationController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        initView(root)

        // Bind data
        homeViewModel.listLoadedPopularCategories.observe(viewLifecycleOwner, {
            val listData = it
            val adapter = PopularCategoryAdapter(requireContext(), listData)
            recyclerView.adapter = adapter
            recyclerView.layoutAnimation = layoutAnimationController
        })
        homeViewModel.listLoadedBestDeals.observe(viewLifecycleOwner, {
            val adapter = BestDealAdapter(requireContext(), it, true)
            loopingViewPager.adapter = adapter
        })

        return root
    }

    private fun initView(root: View) {
        Log.d("TaiPT4", "HomeFragment --- #initView")
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        recyclerView = root.findViewById(R.id.recycler_view_popular) as RecyclerView
        loopingViewPager = root.findViewById(R.id.looping_view_pager) as LoopingViewPager

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun onResume() {
        Log.d("TaiPT4", "HomeFragment --- #onResume")
        super.onResume()
        loopingViewPager.resumeAutoScroll()
    }

    override fun onPause() {
        Log.d("TaiPT4", "HomeFragment --- #onResume")
        loopingViewPager.pauseAutoScroll()
        super.onPause()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}