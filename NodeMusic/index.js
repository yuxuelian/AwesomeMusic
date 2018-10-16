const redis = require('redis')
const client = redis.createClient(6379, '118.126.108.24')
client.on('ready', (res) => {
    console.log('连接成功')
})
client.auth('Yuxuelian-520', () => {
    console.log('认证成功')
})
client.set('hello', JSON.stringify({name: '王开波', age: 23}))
client.get('hello', function (err, value) {
    if (!err) {
        console.log(JSON.parse(value))
    }
})

const express = require('express')
const bodyParser = require('body-parser')
const recommendInit = require('./recommend')
const musicInit = require('./music')
const searchInit = require('./search')
const singerListInit = require('./singer')
const rankListInit = require('./rank')
const app = express()
// 初始化body解析器
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));
const apiRoutes = express.Router()
// 初始化路由
recommendInit(apiRoutes)
musicInit(apiRoutes)
searchInit(apiRoutes)
singerListInit(apiRoutes)
rankListInit(apiRoutes)
// 挂载到 /api
app.use('/api', apiRoutes)
// 启动监听
app.listen(3000)
