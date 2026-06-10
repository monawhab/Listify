package com.listify.presentation.productlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

class SearchDebounce {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    fun update(q: String) { _query.value = q }

    fun debouncedFlow() = _query.debounce(300L).distinctUntilChanged()
}
