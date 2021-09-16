window.config_util=new function (){
    this.base_uri = `//${window.location.host}`
    this.init = (init_callback) => {
        layui.use(['table', 'layer', 'form', "laypage", "dropdown", 'tree'], () => {
            window.table = layui.table;
            window.layer = layui.layer;
            window.form = layui.form;
            window.laypage = layui.laypage;
            window.dropdown = layui.dropdown;
            window.tree = layui.tree;
            if (init_callback) {
                init_callback();
            }
        });
    }
}