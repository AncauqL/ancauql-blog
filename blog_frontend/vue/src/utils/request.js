import axios from 'axios'

// 创建一个新的axios实例
const request = axios.create({
    baseURL: 'http://localhost:9999', // 后端接口基础地址
    timeout: 5000
})

// request 拦截器，可以自请求发送前对请求做一些处理，比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
// 设置默认Content-Type为JSON格式（适用于大多数POST/PUT请求）
    config.headers['Content-Type'] = 'application/json;charset=utf-8';

//config.headers['token'] = user.token
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
        return res;
    }, error => {
        return Promise.reject(error)
    }
)

export default request
