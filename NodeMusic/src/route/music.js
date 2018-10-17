const config = require('../util/config')
const utils = require('../util/utils')
const axios = require('axios')

function musicInit(apiRoutes) {
    // 根据歌曲的id获取歌曲的播放地址
    apiRoutes.post('/getMusicUrl', function (request, response) {
        const url = 'https://u.y.qq.com/cgi-bin/musicu.fcg'
        let body = {
            req_0: {
                module: 'vkey.GetVkeyServer',
                method: 'CgiGetVkey',
                param: {
                    guid: '4715368380',
                    songmid: request.body,
                    uin: ''
                }
            }
        }
        axios
            .post(url, body)
            .then((axiosRes) => {
                let qqResData = axiosRes.data
                let res = {}
                res.qqResData = qqResData
                res.code = qqResData.code
                res.message = qqResData.message
                res.data = qqResData.req_0.data.midurlinfo.map((value) => {
                    return {
                        purl: `http://182.140.219.30/amobile.music.tc.qq.com/${value.purl}`,
                        mid: value.songmid
                    }
                })
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })

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
