package com.kaibo.core.util

/**
 * @author kaibo
 * @date 2018/6/29 17:09
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

/**
 * 判断字符串是否是合法的手机号
 */
fun CharSequence.isPhoneValid(): Boolean {
    return Regex(pattern = """^[1][34578][0-9]{9}${'$'}""").matches(this)
}

/**
 * 判断验证码是否合法
 */
fun CharSequence.isVerCodeValid(): Boolean {
    return Regex("""^[0-9]{6}${'$'}""").matches(this)
}

/**
 * 判断密码是否合法
 */
fun CharSequence.isPwdValid(): Boolean {
    return this.length in 6..16
}

/**
 * 判断名字是否合法
 */
fun CharSequence.isUserNameValid(): Boolean {
    return this.length in 2..6
}

/**
 * 判断身份证号码是否合法
 */
fun CharSequence.isIdCardValid(): Boolean {
    if (this.length != 18) {
        return false
    }
    // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
    val regularExpression = """^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]${'$'}"""
    //假设18位身份证号码:41000119910101123X  410001 19910101 123X
    //^开头
    //[1-9] 第一位1-9中的一个              4
    //\d{5} 五位数字                      10001（前六位省市县地区）
    //(18|19|20)                         19（现阶段可能取值范围18xx-20xx年）
    //\d{2}                              91（年份）
    //((0[1-9])|(10|11|12))              01（月份）
    //(([0-2][1-9])|10|20|30|31)         01（日期）
    //\d{3} 三位数字                      123（第十七位奇数代表男，偶数代表女
    //[0-9Xx] 0123456789Xx其中的一个      X（第十八位为校验值
    //$结尾
    if (this.matches(Regex(regularExpression))) {
        var sum = 0
        //前十七位加权因子
        val idCardWi = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
        idCardWi.forEachIndexed { index, item ->
            sum += this[index].toInt2() * item
        }
        //这是除以11后，可能产生的11位余数对应的验证码
        val idCardY = arrayOf('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2')
        return idCardY[sum % 11] == this[17].toUpperCase()
    }
    return false
}