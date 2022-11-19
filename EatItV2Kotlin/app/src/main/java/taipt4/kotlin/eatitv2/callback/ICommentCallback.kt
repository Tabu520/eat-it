package taipt4.kotlin.eatitv2.callback

import taipt4.kotlin.eatitv2.model.CommentModel

interface ICommentCallback {

    fun onLoadCommentSuccess(commentList: List<CommentModel>)
    fun onLoadCommentFailed(errorMessage: String)
}