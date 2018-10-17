const config = require('../util/config')
const utils = require('../util/utils')
const axios = require('axios')

function filterSinger(singer) {
    let ret = []
    if (!singer) {
        return ''
    }
    singer.forEach((s) => {
        ret.push(s.name)
    })
    return ret.join('·')
}

function songInit(apiRoutes, redisClient) {
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
            needNewCode: 0,
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
                let cdList = qqResData.cdlist[0]
                res.data = {
                    disstid: cdList.disstid,
                    dissname: cdList.dissname,
                    logo: cdList.logo,
                    songList: cdList.songlist.map((value) => {
                        return {
                            songmid: value.songmid,
                            image: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${value.albummid}.jpg?max_age=2592000`,
                            singername: filterSinger(value.singer),
                            songname: value.songname,
                            url: ''
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

    // 点击歌手后获取到的歌手歌曲列表
    apiRoutes.get('/getSingerSongList', function (request, response) {
        const url = 'https://c.y.qq.com/v8/fcg-bin/fcg_v8_singer_track_cp.fcg'
        const data = Object.assign({}, config.commonParams, {
            singermid: request.query.singermid,
            needNewCode: 0,
            platform: 'yqq',
            order: 'listen',
            begin: 0,
            num: 40,
            songstatus: 1,
            jsonpCallback: '__jp4',
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
                res.data = {
                    singerMid: qqResData.data.singer_mid,
                    singerName: qqResData.data.singer_name,
                    singerAvatar: `https://y.gtimg.cn/music/photo_new/T001R300x300M000${qqResData.data.singer_mid}.jpg?max_age=2592000`,
                    songList: qqResData.data.list.map((value) => {
                        const musicData = value.musicData
                        return {
                            songmid: musicData.songmid,
                            image: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${musicData.albummid}.jpg?max_age=2592000`,
                            singername: filterSinger(musicData.singer),
                            songname: musicData.songname,
                            url: ''
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

    // top 排行榜上的歌曲
    apiRoutes.get('/getRankSongList', function (request, response) {
        const url = 'https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg'
        const data = Object.assign({}, config.commonParams, {
            topid: request.query.topid,
            needNewCode: 1,
            uin: 0,
            tpl: 3,
            page: 'detail',
            type: 'top',
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
                res.data = {
                    rankName: qqResData.topinfo.ListName,
                    rankImage: qqResData.topinfo.pic_album,
                    songList: qqResData.songlist.map((value) => {
                        let musicData = value.data
                        return {
                            songmid: musicData.songmid,
                            image: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${musicData.albummid}.jpg?max_age=2592000`,
                            singername: filterSinger(musicData.singer),
                            songname: musicData.songname,
                            url: ''
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

module.exports = songInit