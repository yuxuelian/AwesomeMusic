package com.kaibo.core.util

import java.util.*
import kotlin.collections.Map.Entry

/**
 * @author:Administrator
 * @date:2018/4/13 0013 下午 1:32
 * GitHub:
 * email:
 * description:
 */

/**
 * 获取  LinkedHashMap  第一个添加的元素
 * 时间复杂度O(1)
 * @param map
 * @param <K>
 * @param <V>
 * @return
 */
fun <K, V> LinkedHashMap<K, V>.getFirstEntity(): Entry<K, V> {
    return this.entries.iterator().next()
}

/**
 * 获取  LinkedHashMap  中最后添加的元素(反射)
 * 时间复杂度O(1)
 * @param map
 * @param <K>
 * @param <V>
 * @return
 */
fun <K, V> LinkedHashMap<K, V>.getLastEntity(): Entry<K, V> {
    val tail = this.javaClass.getDeclaredField("tail")
    tail.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return tail.get(this) as Entry<K, V>
}
