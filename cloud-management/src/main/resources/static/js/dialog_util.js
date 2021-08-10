const dialog_util = new function (){
    this.show_modify = (elem, title, icon, method, uri, init_callback, response_callback) => {
        this.show_custom_modify(elem, title, icon, method, uri, init_callback, () => formArray2Data($("form[name='modify']").serializeArray()), response_callback)
    };
    this.show_custom_modify = (elem, title, icon, method, uri, init_callback, request_data_init_func, response_callback) => {

        layer.open({
            type: 1,
            title: `<i class="layui-icon ${icon}"></i>&nbsp;${title}`,
            content: $(elem).html(),
            btn: ['确定', '取消'],
            success: function (index, layero) {
                form.render();
                if (init_callback) {
                    init_callback()
                }
                form.render();
            },
            yes: function (index, layero) {
                layer.close(index);
                index = layer.load(1);
                $.ajax({
                    url: uri,
                    method: method,
                    data: request_data_init_func(),
                    headers: {"X-CLOUD-TOKEN": getToken()},
                    async: true,
                    success: function (response) {
                        layer.close(index);
                        if (response.code === 401) {
                            go_login_page();
                            return;
                        }
                        response_callback(response)
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        layer.close(index);
                        dialog_util.show_tool_tip('处理数据失败,请检查网络或联系管理员');
                    }
                })
            }
        })
    };
  this.show_view = (elem, title, icon, callback) => {
    layer.open({
      type: 1,
      title: `<i class="layui-icon ${icon}"></i>&nbsp;${title}`,
      content: $(elem).html(),
      btn: ['确定'],
      success: function (index, layero) {
        form.render();
        callback()
        form.render();
      }
    })
  };
  this.show_confirm_dialog = (title, method, uri, params, response_callback) => {
    layer.confirm(title, {icon: 3, title: '提示'}, function (index) {
      layer.close(index);
      index = layer.load(1);
      $.ajax({
        url: uri,
        dataType: "json",
        method: method,
        data: params,
        headers: {"X-CLOUD-TOKEN": getToken()},
        async: true,
        success: function (response) {
          layer.close(index);
          if (response.code === 401) {
            go_login_page();
            return;
          }
          response_callback(response);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            layer.close(index);
            dialog_util.show_tool_tip('处理数据失败,请检查网络或联系管理员');
        }
      });
    });
  };
  this.show_tool_tip = (msg) => {
    layer.msg(msg);
  }
};
