const config = require('../util/config')
const utils = require('../util/utils')
const axios = require('axios')

function musicInit(apiRoutes) {
    // 获取歌曲对应的歌词
    apiRoutes.get('/lyric', function (request, response) {
        const url = 'https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg'
        let songmid = request.query.songmid
        const data = Object.assign({}, config.commonParams, {
            format: 'json',
            songmid,
            platform: 'yqq',
            hostUin: 0,
            needNewCode: 0,
            categoryId:10000000,
            pcachetime: new Date().getDate()
        })
        axios
            .get(url, {
                headers: config.commonHeaders,
                params: data
            })
            .then((axiosRes) => {
                let qqResData = utils.jsonpConvertJson(axiosRes.data)
                let res = {}
                res.qqResData = qqResData
                res.code = qqResData.code
                res.message = '成功'
                res.data = qqResData.lyric
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })
}

module.exports = musicInit
