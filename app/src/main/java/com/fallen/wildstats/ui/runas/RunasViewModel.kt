package com.fallen.wildstats.ui.inicio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RunasViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Runas"
    }
    val text: LiveData<String> = _text
}