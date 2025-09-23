package com.azzahid.hof.ui.providers

import androidx.compose.runtime.compositionLocalOf
import com.azzahid.hof.ui.viewmodel.factory.ViewModelFactory

val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> {
    error("ViewModelFactory not provided")
}