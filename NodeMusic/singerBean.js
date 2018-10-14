module.exports = class SingerBean {
    constructor({title, id, name}) {
        this.id = id
        this.title = title
        this.name = name
        this.avatar = `https://y.gtimg.cn/music/photo_new/T001R300x300M000${id}.jpg?max_age=2592000`
    }
}
