data class MemeResponse(
    val url: String,
    val title: String? = null,
    val author: String? = null,
    val subreddit: String? = null
) {
    val shareText: String
        get() = """
            Check out this meme from r/$subreddit!
            "$title" by u/$author
        """.trimIndent()
}
