package com.lauren.multicastlive.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lauren.multicastlive.utils.StreamerManager

class PreviewViewModelFactory(private val streamerManager: StreamerManager) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreviewViewModel::class.java)) {
            return modelClass
                .getConstructor(StreamerManager::class.java)
                .newInstance(streamerManager)
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}