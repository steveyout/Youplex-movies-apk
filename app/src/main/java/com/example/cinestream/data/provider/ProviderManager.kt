package com.example.cinestream.data.provider

data class Provider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true
)

object ProviderManager {

    val providers: List<Provider> = listOf(
        Provider(
            id = "vidlink",
            name = "Echo",
            baseUrl = "https://vidlink.pro",
            enabled = true
        ),
        Provider(
            id = "vidsrc_vip",
            name = "Nova",
            baseUrl = "https://vidsrc.to",
            enabled = true
        ),
        Provider(
            id = "cinemaos",
            name = "Velocity",
            baseUrl = "https://cinemaos.tech",
            enabled = true
        ),
        Provider(
            id = "vidking",
            name = "Apex",
            baseUrl = "https://www.vidking.net",
            enabled = true
        ),
        Provider(
            id = "videasy",
            name = "Pulse",
            baseUrl = "https://player.videasy.net",
            enabled = true
        ),
        Provider(
            id = "embedsu",
            name = "Zenith",
            baseUrl = "https://embed.su",
            enabled = true
        ),
        Provider(
            id = "vidsrc_me",
            name = "Cipher",
            baseUrl = "https://vidsrc.me",
            enabled = true
        ),
        Provider(
            id = "autoembed",
            name = "Nebula",
            baseUrl = "https://player.autoembed.cc",
            enabled = true
        ),
        Provider(
            id = "vidsrc_cc",
            name = "Orion",
            baseUrl = "https://vidsrc.cc",
            enabled = true
        ),
        Provider(
            id = "twoembed",
            name = "Titan",
            baseUrl = "https://www.2embed.cc",
            enabled = true
        ),
        Provider(
            id = "multiembed",
            name = "Spectre",
            baseUrl = "https://multiembed.mov",
            enabled = true
        ),
        Provider(
            id = "rivestream",
            name = "Eclipse",
            baseUrl = "https://rivestream.org",
            enabled = true
        )
    )

    const val DEFAULT_PROVIDER_ID = "vidlink"

    fun getBrand(): String = "youplex"

    /**
     * Helper to build the embed URL based on media type (movie or tv)
     */
    fun getEmbedUrl(
        providerId: String,
        type: String, // "movie" or "tv"
        tmdbId: String,
        season: Int = 1,
        episode: Int = 1,
        progressSeconds: Long = 0
    ): String {
        val cleanType = if (type.lowercase() == "tv") "tv" else "movie"
        val selected = providers.find { it.id == providerId } ?: providers.first()

        return when (selected.id) {
            "vidlink" -> {
                if (cleanType == "movie") "https://vidlink.pro/movie/$tmdbId"
                else "https://vidlink.pro/tv/$tmdbId/$season/$episode"
            }
            "vidsrc_vip" -> {
                if (cleanType == "movie") "https://vidsrc.to/embed/movie/$tmdbId"
                else "https://vidsrc.to/embed/tv/$tmdbId/$season/$episode"
            }
            "cinemaos" -> {
                if (cleanType == "movie") "https://cinemaos.tech/player/$tmdbId?theme=e50914&autoPlay=true"
                else "https://cinemaos.tech/player/$tmdbId/$season/$episode?theme=e50914&autoPlay=true"
            }
            "vidking" -> {
                if (cleanType == "movie") "https://www.vidking.net/embed/movie/$tmdbId?color=e50914"
                else "https://www.vidking.net/embed/tv/$tmdbId/$season/$episode?color=e50914&nextEpisode=true"
            }
            "videasy" -> {
                if (cleanType == "movie") "https://player.videasy.net/movie/$tmdbId"
                else "https://player.videasy.net/tv/$tmdbId/$season/$episode"
            }
            "embedsu" -> {
                if (cleanType == "movie") "https://embed.su/embed/movie/$tmdbId"
                else "https://embed.su/embed/tv/$tmdbId/$season/$episode"
            }
            "vidsrc_me" -> {
                if (cleanType == "movie") "https://vidsrc.me/embed/movie?tmdb=$tmdbId"
                else "https://vidsrc.me/embed/tv?tmdb=$tmdbId&season=$season&episode=$episode"
            }
            "autoembed" -> {
                if (cleanType == "movie") "https://player.autoembed.cc/embed/movie/$tmdbId"
                else "https://player.autoembed.cc/embed/tv/$tmdbId/$season/$episode"
            }
            "vidsrc_cc" -> {
                if (cleanType == "movie") "https://vidsrc.cc/v2/embed/movie/$tmdbId"
                else "https://vidsrc.cc/v2/embed/tv/$tmdbId/$season/$episode"
            }
            "twoembed" -> {
                if (cleanType == "movie") "https://www.2embed.cc/embed/$tmdbId"
                else "https://www.2embed.cc/embedtv/$tmdbId&s=$season&e=$episode"
            }
            "multiembed" -> {
                if (cleanType == "movie") "https://multiembed.mov/directstream.php?video_id=$tmdbId&tmdb=1"
                else "https://multiembed.mov/directstream.php?video_id=$tmdbId&tmdb=1&s=$season&e=$episode"
            }
            "rivestream" -> {
                if (cleanType == "movie") "https://rivestream.org/embed?type=movie&id=$tmdbId"
                else "https://rivestream.org/embed?type=tv&id=$tmdbId&season=$season&episode=$episode"
            }
            else -> {
                if (cleanType == "movie") "https://vidlink.pro/movie/$tmdbId"
                else "https://vidlink.pro/tv/$tmdbId/$season/$episode"
            }
        }
    }
}

