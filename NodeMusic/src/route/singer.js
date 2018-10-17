const config = require('../util/config')
const utils = require('../util/utils')
const axios = require('axios')
const singerBean = require('../util/singerBean')

const HOT_NAME = '热门'
const HOT_SINGER_LENGTH = 10

// 处理歌手数据
function _normalizeSinger(list) {
    let map = {
        hot: {
            title: HOT_NAME,
            items: []
        }
    }
    list.forEach((item, index) => {
        // 前十个歌手归为热门歌手
        if (index < HOT_SINGER_LENGTH) {
            map.hot.items.push(new singerBean({
                title: HOT_NAME,
                name: item.Fsinger_name,
                id: item.Fsinger_mid
            }))
        }
        const title = item.Findex
        // 查看是否存在了改key下的歌手
        if (!map[title]) {
            map[title] = {
                title: title,
                items: []
            }
        }
        // 将歌手添加进去
        map[title].items.push(new singerBean({
            title,
            name: item.Fsinger_name,
            id: item.Fsinger_mid
        }))
    })
    // 为了得到有序列表，我们需要处理 map
    let ret = []
    let hot = []
    for (let key in map) {
        let val = map[key]
        if (val.title.match(/[a-zA-Z]/)) {
            ret.push(val)
        } else if (val.title === HOT_NAME) {
            hot.push(val)
        }
    }
    ret.sort((a, b) => {
        return a.title.charCodeAt(0) - b.title.charCodeAt(0)
    })
    return hot.concat(ret)
}

function singerListInit(apiRoutes) {
    // 根据关键字搜索歌曲
    apiRoutes.get('/getSingerList', function (request, response) {
        const url = 'https://c.y.qq.com/v8/fcg-bin/v8.fcg'
        const data = Object.assign({}, config.commonParams, {
            channel: 'singer',
            page: 'list',
            key: 'all_all_all',
            pagesize: 100,
            pagenum: 1,
            hostUin: 0,
            needNewCode: 0,
            platform: 'yqq'
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
                res.data = _normalizeSinger(qqResData.data.list)
                response.json(res)
            })
            .catch((axiosErr) => {
                console.log(axiosErr)
                response.json(config.error)
            })
    })
}

module.exports = singerListInit
