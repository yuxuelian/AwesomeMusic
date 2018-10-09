package com.kaibo.core.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaibo.core.http.BaseBean
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author kaibo
 * @date 2018/6/28 18:36
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

//val gson = GsonBuilder()
//        .excludeFieldsWithoutExposeAnnotation()
//        .create()

val gson = Gson()

/**
 * 对象Json
 */
inline fun <reified T> String.fromJsonObject(): T {
    return gson.fromJson(this, T::class.java)
}

/**
 * 数组Json
 */
inline fun <reified T> String.fromJsonArray(): List<T> {
    return gson.fromJson(this, object : TypeToken<ArrayList<T>>() {}.type)
}

/**
 * JSon数据是包裹的JsonObject
 */
inline fun <reified T> String.fromWrapJsonObject(): BaseBean<T> {
    val type = ParameterizedTypeImpl(BaseBean::class.java, arrayOf(T::class.java))
    return gson.fromJson(this, type)
}

/**
 * JSon数据是包裹的JsonArray
 */
inline fun <reified T> String.fromWrapJsonArray(): BaseBean<List<T>> {
    val listType = ParameterizedTypeImpl(List::class.java, arrayOf(T::class.java))
    val type = ParameterizedTypeImpl(BaseBean::class.java, arrayOf(listType))
    return gson.fromJson(this, type)
}

/**
 * 对象转换成Json字符串
 */
fun Any.toJson(): String {
    return gson.toJson(this)
}

class ParameterizedTypeImpl(private val raw: Class<*>, private val args: Array<Type>) : ParameterizedType {
    override fun getRawType(): Type {
        return raw
    }

    override fun getOwnerType(): Type? {
        return null
    }

    override fun getActualTypeArguments(): Array<Type> {
        return args
    }
}
