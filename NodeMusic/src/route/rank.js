const config = require('../util/config')
const utils = require('../util/utils')
const axios = require('axios')

function rankListInit(apiRoutes) {
    // 根据关键字搜索歌曲
    apiRoutes.get('/getRankList', function (request, response) {
        const url = 'https://c.y.qq.com/v8/fcg-bin/fcg_myqq_toplist.fcg'
        const data = Object.assign({}, config.commonParams, {
            uin: 0,
            needNewCode: 1,
            platform: 'h5',
            jsonpCallback: '__jp0'
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
                res.message = qqResData.message
                res.data = qqResData.data.topList
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })
}

module.exports = rankListInit
