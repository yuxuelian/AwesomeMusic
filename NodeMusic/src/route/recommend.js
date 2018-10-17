const config = require('../util/config')
const utils = require('../util/utils')
const axios = require('axios')

function recommendInit(apiRoutes, redisClient) {
    // 首页banner数据
    apiRoutes.get('/bannerList', function (request, response) {
        const redisKey = 'bannerList'
        // 从redis中获取数据
        redisClient
            .getObj(redisKey)
            .then((redisValue) => {
                // 缓存存在,直接返回缓存中的数据
                response.json(redisValue)
            })
            .catch((redisErr) => {
                console.log(redisErr)
                // 缓存不存在
                const url = 'https://c.y.qq.com/musichall/fcgi-bin/fcg_yqqhomepagerecommend.fcg'
                const data = Object.assign({}, config.commonParams, {
                    platform: 'h5',
                    needNewCode: 1,
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
                        res.data = qqResData.data.slider
                        // 保存到缓存中
                        redisClient.setObj(redisKey, res, config.redisTTL)
                        // 返回到客户端
                        response.json(res)
                    })
                    .catch((axiosErr) => {
                        console.log(axiosErr)
                        response.json(config.error)
                    })
            })
    })

    // 首页的推荐列表
    apiRoutes.get('/getRecommendList', function (request, response) {
        const url = 'https://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_by_tag.fcg'
        const data = Object.assign({}, config.commonParams, {
            platform: 'yqq',
            hostUin: 0,
            sin: 0,
            ein: 29,
            sortId: 5,
            needNewCode: 0,
            categoryId: 10000000,
            rnd: Math.random(),
            format: 'json'
        })
        axios
            .get(url, {
                headers: config.commonHeaders,
                params: data
            })
            .then((axiosRes) => {
                let qqResData = axiosRes.data
                let res = {}
                res.qqResData = qqResData
                res.code = qqResData.code
                res.message = qqResData.message
                res.data = qqResData.data.list.map((value) => {
                    return {
                        disstid: value.dissid,
                        dissname: value.dissname,
                        name: value.creator.name,
                        imgurl: value.imgurl,
                    }
                })
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })
}

module.exports = recommendInit
