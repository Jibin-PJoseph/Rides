package com.ibm.rides.domain.mapper

interface NullableListDataMapper<I, O> {

    fun map(list: List<I>?): List<O>
}