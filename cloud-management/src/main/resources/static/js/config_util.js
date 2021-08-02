window.config_util=new function (){
      this.base_uri = ".."
    // this.base_uri = "http://192.168.1.2:8080"
//    this.base_uri = "http://localhost:8080"
    this.init = (init_callback) => {
        layui.use(['table', 'layer', 'form',"laypage", "dropdown"], () => {
            window.table = layui.table;
            window.layer = layui.layer;
            window.form = layui.form;
            window.laypage= layui.laypage;
            window.dropdown = layui.dropdown;
            if (init_callback) {
                init_callback();
            }
        });
    }
}