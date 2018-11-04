const express = require('express')
const bodyParser = require('body-parser')
const createRedisClient = require('./redis')
const recommendInit = require('./route/recommend')
const musicInit = require('./route/music')
const searchInit = require('./route/search')
const singerListInit = require('./route/singer')
const rankListInit = require('./route/rank')
const songInit = require('./route/song')
const app = express()
// 初始化body解析器
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));
const apiRoutes = express.Router()
// 创建redis连接
const redisClient = createRedisClient()
// 初始化路由
recommendInit(apiRoutes, redisClient)
musicInit(apiRoutes, redisClient)
searchInit(apiRoutes, redisClient)
singerListInit(apiRoutes, redisClient)
rankListInit(apiRoutes, redisClient)
songInit(apiRoutes, redisClient)
// 挂载到 /api
app.use('/api', apiRoutes)
// 启动监听
app.listen(3001)
