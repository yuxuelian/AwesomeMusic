function jsonpConvertJson(jsonp) {
    let ret = jsonp
    if (typeof ret === 'string') {
        const reg = /{.*}/
        const matches = ret.match(reg)
        if (matches) {
            return JSON.parse(matches[0])
        }
    }
    return {}
}

module.exports = {
    jsonpConvertJson
}
