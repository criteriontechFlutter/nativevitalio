package com.criterion.nativevitalio.interfaces

interface AdapterInterface<T> {
    fun onClick(position: Int, data: T)
}