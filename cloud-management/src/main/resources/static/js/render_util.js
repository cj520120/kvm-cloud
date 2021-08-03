window.render_util=new function (){
    this.render_progress=(title, percent)=>{
        const html = `<div class="layui-progress layui-progress-big" lay-showpercent="true" title="${title}">
                          <div class="layui-progress-bar" lay-percent="${percent}%" style="width: ${percent}%;"><span class="layui-progress-text">${percent}%</span></div>
                      </div>`
        return html
    }
    this. get_memory_capacity=(capacity) =>{
        if (capacity >= 1024 * 1024) {
            return (capacity / 1024 / 1024).toFixed(0) + "GB";
        } else if (capacity >= 1024) {
            return (capacity / 1024).toFixed(0) + "MB";
        } else {
            return capacity + "KBytes";
        }
    }
    this. get_disk_capacity=(capacity)=> {
        if (capacity > 1024 * 1024 * 1024 * 1024) {
            return (capacity / 1024 / 1024 / 1024 / 1024).toFixed(2) + "TB";
        } else if (capacity > 1024 * 1024 * 1024) {
            return (capacity / 1024 / 1024 / 1024).toFixed(2) + "GB";
        } else if (capacity > 1024 * 1024) {
            return (capacity / 1024 / 1024).toFixed(2) + "MB";
        } else if (capacity > 1024) {
            return (capacity / 1024).toFixed(2) + "KB";
        } else {
            return capacity + "B";
        }
    }

    this.render_search_host_select=(pSelect) =>{
        let html = '';
        html += data_util.host_hander.load_data().map(item=>{
            return `<option value="${item.id}" >${item.name}</option>`
        })
        pSelect.append(html);
        form.render("select")
    }


    this.render_edit_host_select=(clusterId,pSelect)=> {
        let html = '<option value="0">无限制</option>';
        html += data_util.host_hander.load_data().map(item=>{
            if(clusterId==0 || item.clusterId===clusterId){
                return `<option value="${item.id}" >${item.name}</option>`
            }else{
                return ''
            }
        })
        pSelect.html(html);
        form.render("select")
    }

    this.render_edit_os_category_select=(pSelect)=> {
        const html = data_util.category_hander.load_data().map(item => {
            if (item.id === 0) {
                return ""
            } else {
                return `<option value="${item.id}" >${item.categoryName}</option>`
            }
        });
        pSelect.append(html);
        form.render("select")
    }


    this.render_edit_calculation_scheme_select=(pSelect)=>{
        const html = data_util.calculation_hander.load_data().map(item =>{
            return `<option value="${item.id}" >${item.name}</option>`
        });
        pSelect.html(html);
        form.render("select")
    }

    this.render_edit_template_select=(clusterId,pSelect)=> {
        const html = data_util.template_hander.load_data().map(item => {
            if (item.type != 'Route' && item.type != 'Console' && item.clusterId === clusterId) {
                return `<option value="${item.id}" >${item.name}</option>`
            } else {
                return ''
            }
        });
        pSelect.html(html);
        form.render("select")
    }
    this.render_attach_iso_select=(clusterId,pSelect) =>{
        const html = data_util.template_hander.load_data().map(item => {
            if (item.type === 'ISO' && item.clusterId === clusterId) {
                return `<option value="${item.id}" >${item.name}</option>`
            } else {
                return ''
            }
        });
        pSelect.html(html);
        form.render("select")
    }
    this.render_attach_network_select=(clusterId,pSelect) =>{
        const html = data_util.network_hander.load_data().map(item => {
            return `<option value="${item.id}" >${item.name}</option>`
        });
        pSelect.html(html);
        form.render("select")
    }
    this.render_edit_network_select=(clusterId,pSelect) =>{
        const html = data_util.network_hander.load_data().map(item => {
            if (item.clusterId === clusterId) {
                return `<option value="${item.id}" >${item.name}</option>`
            } else {
                return ''
            }
        });
        pSelect.html(html);
        form.render("select")
    }

    this.render_search_group_select=(pSelect)=>{
        let html = `<option value="-1" >所有</option><option value="0" >默认</option>`;
        html += data_util.group_hander.load_data().map(item=>{
            return `<option value="${item.id}" >${item.name}</option>`
        })
        pSelect.html(html);
        form.render("select")
    }

    this.render_edit_group_select=(pSelect)=>{
        let html = `<option value="0" >默认</option>`;
        html += data_util.group_hander.load_data().map(item=>{
            return `<option value="${item.id}" >${item.name}</option>`
        })
        pSelect.html(html);
        form.render("select")
    }
    this.render_search_cluster_select=(pSelect)=>{
        let html = `<option value="0" >所有</option>`;
        html += data_util.cluster_hander.load_data().map(item=>{
            return `<option value="${item.id}" >${item.name}</option>`
        })
        pSelect.append(html);
        form.render("select")
    }
    this.render_edit_cluster_select=(pSelect)=> {
        const html = data_util.cluster_hander.load_data().map(item => {
            return `<option value="${item.id}" >${item.name}</option>`
        });
        pSelect.append(html);
        form.render("select")
    }
    this.render_search_storage_select=(pSelect)=>{
        let html = `<option value="0" >所有</option>`;
        html += data_util.storage_hander.load_data().map(item=>{
            return `<option value="${item.id}" >${item.name}</option>`
        })
        pSelect.append(html);
        form.render("select")

    }
    this.render_edit_storage_select=(clusterId,pSelect)=> {
        let html = '<option value="0">无限制</option>';
        html += data_util.storage_hander.load_data().map(item=>{
            if(item.clusterId===clusterId){
                return `<option value="${item.id}" >${item.name}</option>`
            }else{
                return ''
            }
        })
        pSelect.html(html);
        form.render("select")
    }

    this.render_search_vm_select=(pSelect)=>{
        let html = `<option value="-1" >所有</option>`;
        html +=`<option value="0" >未挂载</option>`
        html += data_util.vm_hander.load_data().map(item=>{
            return `<option value="${item.id}" >${item.description}</option>`
        })
        pSelect.append(html);
        form.render("select")
    }
    this.render_attach_vm=(clusterId,pSelect) =>{
        const html = data_util.vm_hander.load_data().map(item => {
            if (clusterId === item.clusterId && item.type === 'Guest') {
                return `<option value="${item.id}" >${item.description}</option>`
            } else {
                return ''
            }
        });
        pSelect.append(html);
        form.render("select")
    }
    this.render_status =  (status) =>{
        if (status === "Ready" || status === "Running") {
            return `<span style="color: #27aa5e;border: solid #a9ddbf 1px;padding: 2px 10px;background: #dff2e7;">${status}</span>`
        } else {
            return `<span style="color: #fff;border: solid #a9ddbf 1px;padding: 2px 10px;background: #f15354;">${status}</span>`
        }
    }
}