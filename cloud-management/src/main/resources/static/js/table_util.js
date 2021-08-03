window.table_util = new function () {
    this.config = null;
    this.filter_text = "";
    this.render = (config) => {
        this.config = config;
        let template = `<table class="layui-table view-table" lay-size="sm" lay-skin="line"><colgroup>`;
        if(this.config.checkbox){
            template+=`<col width="30px"/>`
        }
        template += config.cols.map((item, index) => {
            if (item.width) {
                return `<col width="${item.width}"/>`
            } else {
                return `<col/>`
            }
        }).join('')
        template += "<col/>"
        template += '</colgroup><thead><tr>'
        if(this.config.checkbox){
            template+=`<th><input type="checkbox" class="check-all"/></th>`
        }
        template += this.config.cols.map((item, index) => {
            return `<th>${item.title}</th>`
        }).join('');
        template += '<th>&nbsp;</th></tr></thead><tbody id="tbody"></tbody></table>'
        if(config.page){
            template += `<div id="page"></div>`
        }
        $(config.elem).html(template)

        this.refresh();
    };
    this.render_row = (data) => {
        let html = this.config.cols.map((col, index) => {
            let td = "";
            if (col.render) {
                td = col.render(data)
            } else {
                td = data[col.name]
            }
            return `<td>${td}</td>`
        }).join('');
        if(this.config.checkbox){
            html=`<td><input type="checkbox" data-id="${data[this.config.idName]}" class="table-child" /></td>${html}`
        }
        const json = JSON.stringify(data);
        const button = `<a  onclick="table_util.render_menu(this)" data='${json}' class="layui-btn layui-btn-xs" lay-event="more"><i class="layui-icon layui-icon-more" style="font-size: 16px;" style="float: right;"></i></a>`;
        return `${html}<td>${button}</td>`
    };
    this.render_item = (data) => {
        const html = this.render_row(data);
        return `<tr id="tr_${data[this.config.idName]}">${html}</tr>`;
    };
    this.load_init_data=()=>{
        const data = this.config.handler.load_data();
        const table_data= data.filter(item => {
            if (!this.filter_text || this.filter_text === '') {
                return true;
            } else {
                let bFind = false;

                this.config.cols.filter(col => col.filter).map((col, index) => {
                    let val = '';
                    if (col.render) {
                        val = col.render(item)
                    } else {
                        val = item[col.name]
                    }
                    bFind |= val.toString().toLowerCase().indexOf(this.filter_text.toLowerCase()) >= 0;
                })
                return bFind;
            }

        })
        return table_data;
    }
    this.refresh = () => {
        $("#page").html('')
        let table_data = this.load_init_data();
        if(this.config.page){
            window.laypage.render({
                elem: 'page'
                ,count: table_data.length
                ,limit: 10
                ,limits: [10, 20, 30, 40, 50]
                ,curr:1
                ,layout: ['count', 'prev', 'page', 'next', 'limit', 'skip']
                ,jump: (obj)=>{
                    const table_data=this.load_init_data();
                    this.refresh_page(table_data,(obj.curr-1)*obj.limit,obj.limit)
                }
            });
        }else{
            this.refresh_page(table_data,0,table_data.length)
        }
    };
    this.refresh_page=(table_data,start,limit)=>{
        document.querySelector('#tbody').innerHTML = table_data.map((item, index) => {
            if(index>=start&&index<(start+limit)){
                return table_util.render_item(item)
            }else{
                return ''
            }
        }).join("");
        if(this.config.checkbox){
            this.update_check_box()
            $(`${this.config.elem} input[type=checkbox]`).on('change',  (event) =>{
                this.on_checkbox_changed(event)
            })
            this.config.checkbox_callback(this.get_selected_rows())
        }
    }
    this.on_checkbox_changed=(event)=>{
        if(event.target.classList.contains("table-child")){
            let checked=true
            $(`${this.config.elem} .table-child`).each(function (index,element){
                checked &= element.checked
            });
            $(`${this.config.elem} .check-all`).prop("checked",Boolean(checked))
        }else {
            $(`${this.config.elem} .table-child`).prop("checked", event.target.checked)
        }
        if(this.config.checkbox_callback){
            this.config.checkbox_callback(this.get_selected_rows())
        }
    }
    this.get_selected_rows =()=>{
        let ids=[]
        $(`${this.config.elem} .table-child`).each(function (index,element) {
            if($(element).prop("checked")){
                ids.push($(element).attr("data-id"))
            }
        })
        return ids;
    }
    this.update_check_box=()=>{
        let checked=false;
        $(`${this.config.elem} .table-child`).each(function (index,element) {
            checked&=$(element).prop("checked")
        })
        $(`${this.config.elem} .check-all`).prop("checked",Boolean(checked))
    }
    this.render_menu = (pThis) => {
        const data = JSON.parse($(pThis).attr("data"));
        const menu_data = [];
        this.config.menu.filter(col => {
            return col.show(data)
        }).map((col, index) => {
            const menu = {
                title: col.title,
                id: data[this.config.idName],
                data: data,
                click: col.click,
                templet: `<div class="menu_div" style="width: 80px;"><span>${col.title}</span><i class="layui-icon ${col.icon}" style="float: right;"></i></div>`

            };
            menu_data.push(menu)
        })
        if(menu_data.length>0){
            dropdown.render({
                elem: pThis,
                show: true,
                data: menu_data,
                click: function (menudata) {
                    if(menudata.data){
                        menudata.click(menudata.data)
                    }
                }
            })
        }
    };
    this.modify = (data) => {
        this.config.handler.modify(data)
        $('#tr_' + data[this.config.idName]).html(this.render_row(data))
        if(this.config.checkbox_callback){
            $(`${this.config.elem} #tr_${data[this.config.idName]} input[type=checkbox]`).on('change',  (event) =>{
                this.on_checkbox_changed(event)
            })
            this.update_check_box()
            this.config.checkbox_callback(this.get_selected_rows())
        }
    };
    this.append = function (data) {
        this.config.handler.append(data)
        $('#tbody').append(this.render_item(data))
        if(this.config.checkbox_callback){
            $(`${this.config.elem} #tr_${data[this.config.idName]} input[type=checkbox]`).on('change',  (event) =>{
                this.on_checkbox_changed(event)
            })
            this.update_check_box()
            this.config.checkbox_callback(this.get_selected_rows())
        }
    };
    this.remove = (data) => {
        this.config.handler.remove(data)
        $("#tr_" + data[this.config.idName]).remove()
        this.update_check_box()
    }
};
