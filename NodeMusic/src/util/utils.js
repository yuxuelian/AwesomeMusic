function jsonpConvertJson(jsonp) {
    let ret = jsonp
    if (typeof ret === 'string') {
        let startIndex = jsonp.indexOf('(')
        let endIndex = jsonp.lastIndexOf(')')
        return JSON.parse(jsonp.substring(startIndex + 1, endIndex))
    }
    return {}
}

module.exports = {
    jsonpConvertJson
}
