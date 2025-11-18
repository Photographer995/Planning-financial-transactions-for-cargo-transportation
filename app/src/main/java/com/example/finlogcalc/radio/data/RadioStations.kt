package com.example.finlogcalc.radio.data

import androidx.annotation.DrawableRes
import com.example.finlogcalc.R

data class RadioStation(
    val id: String,
    val name: String,
    val url: String,
    val imageUrl: String, // Added imageUrl
    val frequency: String, // Added frequency
    @DrawableRes val imageRes: Int,
    @DrawableRes val logoRes: Int,
    val genre: String? = null,
    val description: String? = null
)

val sampleRadioStations = listOf(
    // Сохраняем Phonk как просили
    RadioStation(
        id = "phonk-night",
        name = "Phonk Night",
        url = "https://radiorecord.hostingradio.ru/phonk96.aacp",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q1,
        logoRes = R.drawable.q1,
        description = "Темные вибрации ночного города",
        genre = "Hip-Hop / Rap"
    ),

    // Новый список станций
    RadioStation(
        id = "pershy-kanal",
        name = "Pershy Kanal",
        url = "https://stream2.datacenter.by/1kanal",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q2,
        logoRes = R.drawable.q2,
        genre = "World Music"
    ),
    RadioStation(
        id = "kanal-kultura",
        name = "Kanal Kultura",
        url = "http://stream2.datacenter.by:8000/kultura",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q3,
        logoRes = R.drawable.q3,
        genre = "Classical"
    ),
    RadioStation(
        id = "radio-stalitsa",
        name = "Radio Stalitsa",
        url = "https://radiostalica.by:443/radio_en.php",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q4,
        logoRes = R.drawable.q4,
        genre = "Pop"
    ),
    RadioStation(
        id = "radius-fm",
        name = "Radius FM",
        url = "https://stream2.datacenter.by/radiusfm_main",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q5,
        logoRes = R.drawable.q5,
        genre = "Pop"
    ),
    RadioStation(
        id = "radio-mir",
        name = "Radio Mir",
        url = "https://stream2.datacenter.by/radiusfm_main",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q6,
        logoRes = R.drawable.q6,
        genre = "World Music"
    ),
    RadioStation(
        id = "humor-fm-by",
        name = "Yumor v Belarusi FM",
        url = "http://live.humorfm.by:8443/veseloe",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q7,
        logoRes = R.drawable.q7,
        genre = "Pop"
    ),
    RadioStation(
        id = "novoe-radio",
        name = "Novoe Radio",
        url = "https://live.novoeradio.by:444/live/novoeradio_aac128/playlist.m3u8",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q8,
        logoRes = R.drawable.q8,
        genre = "Pop"
    ),
    RadioStation(
        id = "alpha-radio",
        name = "Alpha Radio",
        url = "http://live.alpha.by:7000/tuner_brest",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q9,
        logoRes = R.drawable.q9,
        genre = "Pop"
    ),
    RadioStation(
        id = "radio-unistar",
        name = "Radio Unistar",
        url = "http://live.alpha.by:7000/tuner_brest",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q10,
        logoRes = R.drawable.q10,
        genre = "Pop"
    ),
    RadioStation(
        id = "minskaya-volna",
        name = "Minskaya Volna",
        url = "https://top-radio.org/belarus/minsk-wave-974",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q11,
        logoRes = R.drawable.q11,
        genre = "Pop"
    ),
    RadioStation(
        id = "radio-rocks-fm",
        name = "Radio Rocks FM",
        url = "http://top-radio.org/belarus/minsk-wave-974",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q12,
        logoRes = R.drawable.q12,
        genre = "Rock"
    ),
    RadioStation(
        id = "legendy-fm",
        name = "Legendy FM",
        url = "http://live.legendy.by:8000/legendyfm",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q13,
        logoRes = R.drawable.q13,
        genre = "Pop"
    ),
    RadioStation(
        id = "radio-relax",
        name = "Radio Relax",
        url = "http://live.humorfm.by:8000/relax-high",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q14,
        logoRes = R.drawable.q14,
        genre = "Chill-Out / Lounge"
    ),
    RadioStation(
        id = "center-fm",
        name = "Center FM",
        url = "http://guzei.com/online_radio/listen.php?online_radio_id=19199",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q15,
        logoRes = R.drawable.q15,
        genre = "Pop"
    ),
    RadioStation(
        id = "radio-minsk",
        name = "Radio Minsk",
        url = "https://stream.radiojar.com/zv9u3eapn3quv",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q16,
        logoRes = R.drawable.q16,
        genre = "Pop"
    ),
    RadioStation(
        id = "narodnoe-radio",
        name = "Narodnoe Radio",
        url = "https://live.novoeradio.by:444/live/narodnoeradio_aac128/icecast.audio",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q17,
        logoRes = R.drawable.q17,
        genre = "Folk / Acoustic"
    ),
    RadioStation(
        id = "kompas-fm",
        name = "Kompas FM",
        url = "https://radio.mil.by/live",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q18,
        logoRes = R.drawable.q18,
        genre = "Pop"
    ),
    RadioStation(
        id = "europa-plus-belarus",
        name = "Europa Plus Belarus",
        url = "https://hls-01-regions.emgsound.ru/11_msk/playlist.m3u8",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q19,
        logoRes = R.drawable.q19,
        genre = "Pop"
    ),
    RadioStation(
        id = "russkoe-radio-by",
        name = "Russkoe Radio v Belarusi",
        url = "https://hls-01-regions.emgsound.ru/11_msk/playlist.m3u8",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q20,
        logoRes = R.drawable.q20,
        genre = "Pop"
    ),
    RadioStation(
        id = "energy-fm",
        name = "Energy FM",
        url = "https://stream2.datacenter.by/energy",
        imageUrl = "",
        frequency = "",
        imageRes = R.drawable.q21,
        logoRes = R.drawable.q21,
        genre = "Electronic / Dance"
    )
)
