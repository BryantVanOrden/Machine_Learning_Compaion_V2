package np.com.bryant.myapp.model


data class UserModel(
    var id: String = "",
    var email: String = "",
    var username: String = "",
    var profilePic: String = "",
    var favoriteArticles: MutableList<CustomArticle> = mutableListOf()
)




