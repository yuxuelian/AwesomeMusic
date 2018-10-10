const config = require('./config')
const utils = require('./utils')
const axios = require('axios')

function recommendInit(apiRoutes) {
    // 首页banner数据
    apiRoutes.get('/bannerList', function (request, response) {
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
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
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

    // 首页推荐点击后的歌曲列表
    apiRoutes.get('/getRecommendSongList', function (request, response) {
        const url = 'https://c.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg'
        const data = Object.assign({}, config.commonParams, {
            disstid: request.query.disstid,
            type: 1,
            json: 1,
            utf8: 1,
            onlysong: 0,
            platform: 'yqq',
            hostUin: 0,
            needNewCode: 0
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
                let retData = qqResData.cdlist[0]
                res.data = {
                    disstid: retData.disstid,
                    dissname: retData.dissname,
                    desc: retData.desc,
                    songlist: retData.songlist.map((value) => {
                        return {
                            albumname: value.albumname,
                            songname: value.songname,
                            songmid: value.songmid,
                            singer: value.singer.map((value) => {
                                return value.name
                            }).join('·')
                        }
                    })
                }
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })
}

module.exports = recommendInit
