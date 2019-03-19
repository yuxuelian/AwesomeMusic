package com.kaibo.core.util

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.map
import okhttp3.RequestBody

/**
 * @author kaibo
 * @date 2018/6/28 18:36
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

// Map<String,String>
val stringMapKS = (StringSerializer to StringSerializer).map
// Map<String,Boolean>
val booleanMapKS = (StringSerializer to BooleanSerializer).map
// Map<String,Char>
val charMapKS = (StringSerializer to CharSerializer).map
// Map<String,Byte>
val byteMapKS = (StringSerializer to ByteSerializer).map
// Map<String,Short>
val shortMapKS = (StringSerializer to ShortSerializer).map
// Map<String,Int>
val intMapKS = (StringSerializer to IntSerializer).map
// Map<String,Long>
val longMapKS = (StringSerializer to LongSerializer).map
// Map<String,Float>
val floatMapKS = (StringSerializer to FloatSerializer).map
// Map<String,Double>
val doubleMapKS = (StringSerializer to DoubleSerializer).map
// Map<String,Map<String,String>>
val mapMapKs = (StringSerializer to stringMapKS).map

val json = Json.nonstrict

/// ------------------------json字符串转成对象-----------------------
inline fun <reified T : Any> String.parse(deserializer: DeserializationStrategy<T>): T {
    return json.parse(deserializer, this)
}

inline fun <reified T : Any> String.parseList(list: KSerializer<List<T>>): List<T> {
    return json.parse(list, this)
}

inline fun <reified T : Any> String.parseSet(set: KSerializer<Set<T>>): Set<T> {
    return json.parse(set, this)
}

inline fun <reified K : Any, reified V : Any> String.parseMap(map: KSerializer<Map<K, V>>): Map<K, V> {
    return json.parse(map, this)
}

/// -----------------------------对象转成json字符串-------------------------------
inline fun <reified T : Any> T.stringify(serializer: SerializationStrategy<T>): String {
    return json.stringify(serializer, this)
}

inline fun <reified T : Any> List<T>.stringify(list: KSerializer<List<T>>): String {
    return json.stringify(list, this)
}

inline fun <reified T : Any> Set<T>.stringify(set: KSerializer<Set<T>>): String {
    return json.stringify(set, this)
}

inline fun <reified K : Any, reified V : Any> Map<K, V>.stringify(map: KSerializer<Map<K, V>>): String {
    return json.stringify(map, this)
}

/// ----------------------对象转成 json 请求体------------------------------
inline fun <reified T : Any> T.toJsonRequestBody(serializer: SerializationStrategy<T>): RequestBody {
    return this.stringify(serializer).toJsonRequestBody()
}

inline fun <reified T : Any> List<T>.toJsonRequestBody(list: KSerializer<List<T>>): RequestBody {
    return this.stringify(list).toJsonRequestBody()
}

inline fun <reified T : Any> Set<T>.toJsonRequestBody(set: KSerializer<Set<T>>): RequestBody {
    return this.stringify(set).toJsonRequestBody()
}

inline fun <reified K : Any, reified V : Any> Map<K, V>.toJsonRequestBody(map: KSerializer<Map<K, V>>): RequestBody {
    return this.stringify(map).toJsonRequestBody()
}
