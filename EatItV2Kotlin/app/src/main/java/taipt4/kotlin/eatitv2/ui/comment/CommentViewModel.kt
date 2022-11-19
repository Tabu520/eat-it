package taipt4.kotlin.eatitv2.ui.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import taipt4.kotlin.eatitv2.model.CommentModel

class CommentViewModel : ViewModel() {

    private var commentListMutableLiveData: MutableLiveData<List<CommentModel>> = MutableLiveData()

    fun setCommentList(listComment: List<CommentModel>) {
        commentListMutableLiveData.value = listComment
    }

    fun getCommentList(): MutableLiveData<List<CommentModel>> {
        return commentListMutableLiveData
    }

}