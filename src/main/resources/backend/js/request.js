(function (win) {
  axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

  const service = axios.create({
    baseURL: '/',
    timeout: 10000
  })

  service.interceptors.request.use(function (config) {
    if (config.method === 'get' && config.params) {
      let url = config.url + '?'
      Object.keys(config.params).forEach(function (propName) {
        const value = config.params[propName]
        const part = encodeURIComponent(propName) + '='
        if (value !== null && typeof value !== 'undefined') {
          if (typeof value === 'object') {
            Object.keys(value).forEach(function (key) {
              const params = propName + '[' + key + ']'
              const subPart = encodeURIComponent(params) + '='
              url += subPart + encodeURIComponent(value[key]) + '&'
            })
          } else {
            url += part + encodeURIComponent(value) + '&'
          }
        }
      })
      url = url.slice(0, -1)
      config.params = {}
      config.url = url
    }
    return config
  }, function (error) {
    console.log(error)
    return Promise.reject(error)
  })

  service.interceptors.response.use(function (res) {
    if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {
      localStorage.removeItem('userInfo')
      window.top.location.href = '/backend/page/login/login.html'
    }
    return res.data
  }, function (error) {
    let message = error.message
    if (message === 'Network Error') {
      message = '后端接口连接异常'
    } else if (message.includes('timeout')) {
      message = '系统接口请求超时'
    } else if (message.includes('Request failed with status code')) {
      message = '系统接口 ' + message.substr(message.length - 3) + ' 异常'
    }
    window.ELEMENT.Message({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  })

  win.$axios = service
})(window)
