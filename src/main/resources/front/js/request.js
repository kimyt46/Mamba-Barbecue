(function (win) {
  axios.defaults.headers["Content-Type"] = "application/json;charset=utf-8";

  const service = axios.create({
    baseURL: "/",
    timeout: 10000,
  });

  service.interceptors.request.use(
    (config) => {
      if (config.method === "get" && config.params) {
        let url = config.url + "?";
        for (const propName of Object.keys(config.params)) {
          const value = config.params[propName];
          const part = encodeURIComponent(propName) + "=";
          if (value !== null && typeof value !== "undefined") {
            if (typeof value === "object") {
              for (const key of Object.keys(value)) {
                const params = propName + "[" + key + "]";
                const subPart = encodeURIComponent(params) + "=";
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
      return config;
    },
    (error) => Promise.reject(error)
  );

  service.interceptors.response.use(
    (res) => {
      if (res.data.code === 0 && res.data.msg === "NOTLOGIN") {
        window.top.location.href = "/front/page/login.html?v=20260507-frontfix5";
        return;
      }
      return res.data;
    },
    (error) => {
      if (error && error.config && error.config.silent) {
        return Promise.reject(error);
      }

      let { message } = error;
      if (message === "Network Error") {
        message = "后端接口连接异常";
      } else if (message.includes("timeout")) {
        message = "系统接口请求超时";
      } else if (message.includes("Request failed with status code")) {
        message = "系统接口 " + message.slice(-3) + " 异常";
      }

      window.vant.Notify({
        message: message,
        type: "warning",
        duration: 5 * 1000,
      });

      return Promise.reject(error);
    }
  );

  win.$axios = service;
})(window);
