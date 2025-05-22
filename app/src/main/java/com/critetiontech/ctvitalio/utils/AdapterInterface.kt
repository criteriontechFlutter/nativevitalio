package com.critetiontech.ctvitalio.interfaces

interface AdapterInterface<T> {
    fun onClick(position: Int, data: T)
}