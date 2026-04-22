(function (win) {
  axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
  // 鍒涘缓axios瀹炰緥
  const service = axios.create({
    // axios涓姹傞厤缃湁baseURL閫夐」锛岃〃绀鸿姹俇RL鍏叡閮ㄥ垎
    baseURL: '/',
    // 瓒呮椂
    timeout: 10000
  })
  // request鎷︽埅鍣?
  service.interceptors.request.use(config => {
    // 鏄惁闇€瑕佽缃?token
    // const isToken = (config.headers || {}).isToken === false
    // if (getToken() && !isToken) {
    //   config.headers['Authorization'] = 'Bearer ' + getToken() // 璁╂瘡涓姹傛惡甯﹁嚜瀹氫箟token 璇锋牴鎹疄闄呮儏鍐佃嚜琛屼慨鏀?
    // }
    // get璇锋眰鏄犲皠params鍙傛暟
    if (config.method === 'get' && config.params) {
      let url = config.url + '?';
      for (const propName of Object.keys(config.params)) {
        const value = config.params[propName];
        var part = encodeURIComponent(propName) + "=";
        if (value !== null && typeof(value) !== "undefined") {
          if (typeof value === 'object') {
            for (const key of Object.keys(value)) {
              let params = propName + '[' + key + ']';
              var subPart = encodeURIComponent(params) + "=";
              url += subPart + encodeURIComponent(value[key]) + "&";
            }
          } else {
            url += part + encodeURIComponent(value) + "&";
          }
        }
      }
      url = url.slice(0, -1);
      config.params = {};
      config.url = url;
    }
    return config
  }, error => {
      Promise.reject(error)
  })

  // 鍝嶅簲鎷︽埅鍣?
  service.interceptors.response.use(res => {
      console.log('---鍝嶅簲鎷︽埅鍣?--',res)
      if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 杩斿洖鐧诲綍椤甸潰
        window.top.location.href = '/front/page/login.html'
      }
      return res.data
    },
    error => {
      let { message } = error;
      if (message == "Network Error") {
        message = "鍚庣鎺ュ彛杩炴帴寮傚父";
      }
      else if (message.includes("timeout")) {
        message = "绯荤粺鎺ュ彛璇锋眰瓒呮椂";
      }
      else if (message.includes("Request failed with status code")) {
        message = "绯荤粺鎺ュ彛" + message.substr(message.length - 3) + "寮傚父";
      }
      window.vant.Notify({
        message: message,
        type: 'warning',
        duration: 5 * 1000
      })
      //window.top.location.href = '/front/page/no-wify.html'
      return Promise.reject(error)
    }
  )
  win.$axios = service
})(window);


