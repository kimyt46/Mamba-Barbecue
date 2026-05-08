function loginApi(data) {
  return $axios({
    url: "/user/login",
    method: "post",
    data,
  });
}

function sendEmailCodeApi(data) {
  return $axios({
    url: "/user/sendMsg",
    method: "post",
    data,
    silent: true,
  });
}

function loginoutApi() {
  return $axios({
    url: "/user/loginout",
    method: "post",
  });
}
