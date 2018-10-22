module.exports = {
    //redisTTL 单位是秒
    redisTTL: 24 * 60 * 60,
    commonParams: {
        g_tk: 1928093487,
        inCharset: 'utf-8',
        outCharset: 'utf-8',
        notice: 0,
        format: 'jsonp'
    },
    commonHeaders: {
        referer: 'https://c.y.qq.com/',
        host: 'c.y.qq.com'
    },
    error: {
        code: -1,
        message: '失败',
        data: null
    }
}
