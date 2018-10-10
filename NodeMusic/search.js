const config = require('./config')
const utils = require('./utils')
const axios = require('axios')

function searchInit(apiRoutes) {
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

module.exports = searchInit
