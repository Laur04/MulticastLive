package com.lauren.multicastlive.models

sealed class Endpoint(val hasSrtCapabilities: Boolean) {
    class SrtEndpoint : Endpoint(true)
}