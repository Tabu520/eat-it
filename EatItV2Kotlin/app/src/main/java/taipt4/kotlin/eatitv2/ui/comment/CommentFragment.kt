package taipt4.kotlin.eatitv2.ui.comment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import taipt4.kotlin.eatitv2.R
import taipt4.kotlin.eatitv2.adapter.CommentAdapter
import taipt4.kotlin.eatitv2.callback.ICommentCallback
import taipt4.kotlin.eatitv2.common.Common
import taipt4.kotlin.eatitv2.eventbus.MenuItemBack
import taipt4.kotlin.eatitv2.model.CommentModel

class CommentFragment : BottomSheetDialogFragment(), ICommentCallback {

    private val commentViewModel: CommentViewModel by viewModels()

    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var dialog: AlertDialog

    private var listener: ICommentCallback = this

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = LayoutInflater.from(context).inflate(R.layout.fragment_bottom_sheet_comment, container, false)
        initViews(root)
        loadCommentFromFirebase()
        commentViewModel.getCommentList().observe(viewLifecycleOwner, {
            val adapter = CommentAdapter(requireContext(), it)
            commentRecyclerView.adapter = adapter
        })
        return root

    }

    private fun initViews(root: View) {
        Log.d("TaiPT4", root.toString())
        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()

        commentRecyclerView = root.findViewById(R.id.bottom_sheet_recycler_comment) as RecyclerView
        commentRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        commentRecyclerView.layoutManager = layoutManager
        commentRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), layoutManager.orientation))
    }

    private fun loadCommentFromFirebase() {
        dialog.show()

        val commentModelList = ArrayList<CommentModel>()
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.selectedFood!!.id!!)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (comment in snapshot.children) {
                        val commentModel = comment.getValue(CommentModel::class.java)
                        commentModelList.add(commentModel!!)
                    }
                    listener.onLoadCommentSuccess(commentModelList)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onLoadCommentFailed(error.message)
                }

            })

    }

    override fun onLoadCommentSuccess(commentList: List<CommentModel>) {
        dialog.dismiss()
        commentViewModel.setCommentList(commentList)
    }

    override fun onLoadCommentFailed(errorMessage: String) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    companion object {
        private var instance: CommentFragment? = null

        fun getInstance(): CommentFragment {
            if (instance == null) {
                instance = CommentFragment()
            }
            return instance!!
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}