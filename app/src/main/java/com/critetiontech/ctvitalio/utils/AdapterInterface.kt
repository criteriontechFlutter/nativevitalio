package com.critetiontech.vitalioCorporate.interfaces

interface AdapterInterface<T> {
    fun onClick(position: Int, data: T)
}