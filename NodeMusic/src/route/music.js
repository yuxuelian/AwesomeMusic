const config = require('../util/config')
const utils = require('../util/utils')
const axios = require('axios')

function musicInit(apiRoutes) {
    // 获取歌曲对应的歌词
    apiRoutes.get('/lyric', function (request, response) {
        const url = 'https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg'
        axios
            .get(url, {
                headers: config.commonHeaders,
                params: request.query
            })
            .then((axiosRes) => {
                let qqResData = utils.jsonpConvertJson(axiosRes.data)
                let res = {}
                res.qqResData = qqResData
                res.code = qqResData.code
                res.message = qqResData.message
                res.data = qqResData
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })
}

module.exports = musicInit
