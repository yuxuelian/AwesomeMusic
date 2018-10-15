const config = require('./config')
const utils = require('./utils')
const axios = require('axios')

function search(apiRoutes) {
    // 根据关键字搜索歌曲
    apiRoutes.get('/search', function (request, response) {
        const url = 'https://c.y.qq.com/soso/fcgi-bin/search_for_qq_cp'
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

function getHotSearch(apiRoutes) {
    // 根据关键字搜索歌曲
    apiRoutes.get('/getHotSearch', function (request, response) {
        const url = 'https://c.y.qq.com/splcloud/fcgi-bin/gethotkey.fcg'
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
                res.message = '成功'
                res.data = qqResData.data.hotkey
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })
}

function searchInit(apiRoutes) {
    search(apiRoutes)
    getHotSearch(apiRoutes)
}

module.exports = searchInit
