import axios from 'axios'

// 后端服务地址：markdown 渲染、图片上传回显等都从这里取，改地址只改这一处
export const API_BASE = 'http://localhost:9999'

/** 站内上传的图片存相对路径（/uploads/...），展示时统一走这里拼接后端地址 */
export function resolveAsset(url) {
    if (url && url.startsWith('/uploads/')) {
        return API_BASE + url
    }
    return url
}

// 创建一个新的axios实例
const request = axios.create({
    baseURL: API_BASE,
    timeout: 15000
})

// request 拦截器，可以自请求发送前对请求做一些处理，比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    // FormData（文件上传）必须让浏览器自动生成 multipart 边界，不能强制 JSON
    if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8'
    }

    const token = localStorage.getItem('blog_token')
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
}, error => {
    return Promise.reject(error)
});

// 响应拦截器（接口响应后的处理）作用：统一处理响应数据
request.interceptors.response.use(
    response => {
        let res = response.data;

// 兼容服务端返回的字符串类型数据（如果后端返回JSON字符串，自动解析为对象）
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res && res.code === '401') {
            localStorage.removeItem('blog_token')
            localStorage.removeItem('blog_user')
        }
        return res;
    }, error => {
        return Promise.reject(error)
    }
)

export default request
