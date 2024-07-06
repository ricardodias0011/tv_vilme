package com.nest.nestplay.model

import android.content.Context
import android.os.Parcelable
import com.nest.nestplay.utils.Common
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
fun parseM3uPlaylist(playlistContent: String, context: Context): List<ChannelTVModel> {
    val channels = mutableListOf<ChannelTVModel>()
    val lines = playlistContent.trim().split("\n")

    var currentTvgId = ""
    var currentTvgName = ""
    var currentTvgLogoUrl = ""
    var currentChannelName = ""
    var currentStreamUrl = ""
    var currentGroupTitle = ""

    try{

        val maxLines = minOf(lines.size, 7000)

        for (i in 0 until maxLines) {
            val line = lines[i].trim()
            if (line.isBlank()) {
                continue
            }

            if (line.startsWith("#EXTINF")) {
                val tvgIdPattern = Regex("tvg-id=\"([^\"]+)\"")
                val tvgNamePattern = Regex("tvg-name=\"([^\"]+)\"")
                val tvgLogoUrlPattern = Regex("tvg-logo=\"([^\"]+)\"")
                val groupTitlePattern = Regex("group-title=\"([^\"]+)\"")

                currentTvgId = tvgIdPattern.find(line)?.groupValues?.get(1) ?: ""
                currentTvgName = tvgNamePattern.find(line)?.groupValues?.get(1) ?: ""
                currentTvgLogoUrl = tvgLogoUrlPattern.find(line)?.groupValues?.get(1) ?: ""
                currentGroupTitle = groupTitlePattern.find(line)?.groupValues?.get(1) ?: ""

                val nextLineIndex = lines.indexOf(line) + 1
                if (nextLineIndex < lines.size) {
                    currentChannelName = lines[nextLineIndex].trim()
                }
            } else if (line.startsWith("http")) {
                currentStreamUrl = line
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
    }catch (e: Exception){
        Common.errorModal(context, "Erro ao carregar canais", "Não foi possível carregar lista de canais devido a um error desconhecido.")
    }

    return channels
}