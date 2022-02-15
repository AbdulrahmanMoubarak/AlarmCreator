package com.training.radioalarm.roomdb.model

import java.io.Serializable

class RadioChannelModel(
    var image_url: String,
    var name: String,
    var uri: String,
    var channel_id: Int,
    var countryCode: String,
    var genre: String
)