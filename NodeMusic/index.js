const express = require('express')
const bodyParser = require('body-parser')
const recommendInit = require('./recommend')
const musicInit = require('./music')
const searchInit = require('./search')
const app = express()
// 初始化body解析器
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));
const apiRoutes = express.Router()
// 初始化路由
recommendInit(apiRoutes)
musicInit(apiRoutes)
searchInit(apiRoutes)
// 挂载到 /api
app.use('/api', apiRoutes)
// 启动监听
app.listen(3000)
