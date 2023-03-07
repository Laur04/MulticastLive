package com.lauren.multicastlive.models

enum class EndpointType(val id: Int) {
    SRT(0);

    companion object {
        fun fromId(id: Int): EndpointType = values().first { it.id == id }
    }
}
