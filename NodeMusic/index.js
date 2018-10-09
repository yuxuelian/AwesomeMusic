// 代理接口
var axios = require('axios')
var express = require('express')
var app = express()
var bodyParser = require('body-parser')
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
var apiRoutes = express.Router()

// 首页banner数据
apiRoutes.get('/bannerList', function (req, res) {
  const url = 'https://c.y.qq.com/musichall/fcgi-bin/fcg_yqqhomepagerecommend.fcg?g_tk=1928093487&inCharset=utf-8&outCharset=utf-8&notice=0&format=jsonp&platform=h5&uin=0&needNewCode=1&jsonpCallback=__jp0'
  axios
    .get(url)
    .then((response) => {
      let ret = response.data
      if (typeof ret === 'string') {
        const reg = /{.*}/
        const matches = ret.match(reg)
        if (matches) {
          ret = JSON.parse(matches[0])
        }
      }
      ret.data=ret.data.slider
      res.json(ret)
    })
    .catch((e) => {
      console.log(e)
    })
})

// 首页的推荐列表
apiRoutes.get('/getDiscList', function (req, res) {
  var url = 'https://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_by_tag.fcg'
  axios
    .get(url, {
      headers: {
        referer: 'https://c.y.qq.com/',
        host: 'c.y.qq.com'
      },
      params: req.query
    })
    .then((response) => {
      res.json(response.data)
    })
    .catch((e) => {
      console.log(e)
    })
})

// 首页推荐点击后的歌曲列表
apiRoutes.get('/getSongList', function (req, res) {
  const url = 'https://c.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg'
  axios
    .get(url, {
      headers: {
        referer: 'https://y.qq.com/',
        host: 'c.y.qq.com'
      },
      params: req.query
    })
    .then((response) => {
      let ret = response.data
      if (typeof ret === 'string') {
        const reg = /{.*}/
        const matches = ret.match(reg)
        if (matches) {
          ret = JSON.parse(matches[0])
        }
      }
      res.json(ret)
    })
    .catch((e) => {
      console.log(e)
    })
})

// 根据歌曲的id获取歌曲的播放地址
apiRoutes.post('/getMusicUrl', function (req, res) {
  var url = 'https://u.y.qq.com/cgi-bin/musicu.fcg'
  let body = {
    req_0: {
      module: 'vkey.GetVkeyServer',
      method: 'CgiGetVkey',
      param: {
        guid: '4715368380',
        songmid: req.body,
        uin: ''
      }
    }
  }
  axios
    .post(url, body)
    .then((response) => {
      res.json(response.data.req_0.data.midurlinfo.map((value) => {
        return {
          purl: `http://182.140.219.30/amobile.music.tc.qq.com/${value.purl}`,
          mid: value.songmid
        }
      }))
    })
    .catch((e) => {
      console.log(e)
    })
})

// 获取歌曲对应的歌词
apiRoutes.get('/lyric', function (req, res) {
  var url = 'https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg'
  axios
    .get(url, {
      headers: {
        referer: 'https://c.y.qq.com/',
        host: 'c.y.qq.com'
      },
      params: req.query
    })
    .then((response) => {
      let ret = response.data
      if (typeof ret === 'string') {
        const reg = /{.*}/
        const matches = ret.match(reg)
        if (matches) {
          ret = JSON.parse(matches[0])
        }
      }
      res.json(ret)
    })
    .catch((e) => {
      console.log(e)
    })
})

// 根据关键字搜索歌曲
apiRoutes.get('/search', function (req, res) {
  var url = 'https://c.y.qq.com/soso/fcgi-bin/search_for_qq_cp'
  axios
    .get(url, {
      headers: {
        referer: 'https://c.y.qq.com/',
        host: 'c.y.qq.com'
      },
      params: req.query
    })
    .then((response) => {
      let ret = response.data
      if (typeof ret === 'string') {
        const reg = /{.*}/
        const matches = ret.match(reg)
        if (matches) {
          ret = JSON.parse(matches[0])
        }
      }
      res.json(ret)
    })
    .catch((e) => {
      console.log(e)
    })
})

app.use('/api', apiRoutes)
app.listen(3000)