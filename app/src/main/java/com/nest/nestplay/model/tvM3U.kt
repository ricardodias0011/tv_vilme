package com.nest.nestplay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChannelTVModel(
    val tvgId: String?,
    val tvgName: String?,
    val tvgLogoUrl: String?,
    val channelName: String?,
    val streamUrl: String?,
    val groupTitlePattern: String?
): Parcelable

data class ListChannelTVModel(
    val list: MutableList<ChannelTVModel> = mutableListOf(),
    val title: String = ""
)
fun parseM3uPlaylist(playlistContent: String): List<ChannelTVModel> {
    val channels = mutableListOf<ChannelTVModel>()
    val lines = playlistContent.trim().split("\n")

    var currentTvgId = ""
    var currentTvgName = ""
    var currentTvgLogoUrl = ""
    var currentChannelName = ""
    var currentStreamUrl = ""
    var currentGroupTitle = ""
    for (line in lines) {
        val trimmedLine = line.trim()
        if (trimmedLine.isBlank()) {
            continue
        }

        if (trimmedLine.startsWith("#EXTINF")) {
            val tvgIdPattern = Regex("tvg-id=\"([^\"]+)\"")
            val tvgNamePattern = Regex("tvg-name=\"([^\"]+)\"")
            val tvgLogoUrlPattern = Regex("tvg-logo=\"([^\"]+)\"")
            val groupTitlePattern = Regex("group-title=\"([^\"]+)\"")

            currentTvgId = tvgIdPattern.find(trimmedLine)?.groupValues?.get(1) ?: ""
            currentTvgName = tvgNamePattern.find(trimmedLine)?.groupValues?.get(1) ?: ""
            currentTvgLogoUrl = tvgLogoUrlPattern.find(trimmedLine)?.groupValues?.get(1) ?: ""
            currentGroupTitle= groupTitlePattern.find(trimmedLine)?.groupValues?.get(1) ?: ""
            if (lines.indexOf(trimmedLine) + 1 < lines.size) {
                currentChannelName = lines[lines.indexOf(trimmedLine) + 1].trim()
            }
        } else if (trimmedLine.startsWith("http")) {
            currentStreamUrl = trimmedLine

            val channel = ChannelTVModel(
                currentTvgId,
                currentTvgName,
                currentTvgLogoUrl,
                currentChannelName,
                currentStreamUrl,
                currentGroupTitle
            )
            channels.add(channel)
            currentTvgId = ""
            currentTvgName = ""
            currentTvgLogoUrl = ""
            currentChannelName = ""
            currentStreamUrl = ""
            currentGroupTitle = ""
        }
    }

    return channels
}