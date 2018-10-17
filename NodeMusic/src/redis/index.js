const redis = require('redis')
const config = require('./config')

// 扩展方法必须用function  不能用箭头函数 因为箭头函数得不到RedisClient的this引用
redis.RedisClient.prototype.setObj = function (key, obj, ttl) {
    this.set(key, JSON.stringify(obj))
    if (ttl) {
        this.expire(key, ttl)
    }
}

redis.RedisClient.prototype.getObj = function (key) {
    return new Promise((resolve, reject) => {
        this.get(key, (err, value) => {
            if (err) {
                reject(err)
            } else {
                if (value) {
                    resolve(JSON.parse(value))
                } else {
                    reject(err)
                }
            }
        })
    })
}

function createRedisClient() {
    const client = redis.createClient(config.port, config.host)
    client.auth(config.password, () => {
        console.log('认证成功')
    })
    client.on('connect', (res) => {
        console.log('connect')
    })
    client.on('ready', (res) => {
        console.log('ready')
    })
    client.on('error', (err) => {
        console.log('error')
    })
    return client
}

module.exports = createRedisClient
