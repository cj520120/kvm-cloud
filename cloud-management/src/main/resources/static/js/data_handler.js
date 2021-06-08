const data_handler = function (config) {
  this.uri = config.uri;
  this.elem = config.elem;
  this.cache = undefined;
  this.idName = config.idName;
  this.refresh = () => {
    this.cache = undefined;
  };
  this.load_data = () => {
    if (!this.cache) {
      let params = this.elem ? formArray2Data($(this.elem).serializeArray()) : {};
      $.ajax({
        url: this.uri,
        method: 'GET',
        data: params,
        headers: {"X-CLOUD-TOKEN": getToken()},
        async: false,
        success: (response) => {
          if (response.code === 401) {
            go_login_page();

          } else if (response.code === 0) {
            this.cache = response.data;
          } else {
            dialog_util.show_tool_tip('获取数据失败，错误码:' + response.code + '。错误信息:' + response.message);
          }
        },
        error: function (jqXHR, textStatus, errorThrown) {
          dialog_util.show_tool_tip('获取数据失败,请检查网络或联系管理员');
        }
      });
    }
    return this.cache;
  };
  this.modify = (data) => {
    $.each(this.cache, (idx, row) => {
      if (data[this.idName] === row[this.idName]) {
        this.cache.splice(idx, 1, data);
      }
    });
  };
  this.remove = (data) => {
    $.each(this.cache, (idx, row) => {
      if (data[this.idName] === row[this.idName]) {
        this.cache.splice(idx, 1);
      }
    });
  };
  this.append = (data) => {
    this.cache.push(data);
  };

};
