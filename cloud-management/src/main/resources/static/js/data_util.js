window.data_util = new function () {
    this.host_hander = new data_handler({uri: config_util.base_uri+'/management/host'})
    this.category_hander = new data_handler({uri: config_util.base_uri+'/management/os/category'})
    this.calculation_hander = new data_handler({uri: config_util.base_uri+'/management/calculation/scheme'})
    this.template_hander = new data_handler({uri: config_util.base_uri+'/management/template'})
    this.network_hander = new data_handler({uri: config_util.base_uri+'/management/network'})
    this.group_hander = new data_handler({uri: config_util.base_uri+'/management/group'})
    this.cluster_hander = new data_handler({uri: config_util.base_uri+'/management/cluster'})
    this.storage_hander = new data_handler({uri: config_util.base_uri+'/management/storage'})
    this.vm_hander = new data_handler({uri: config_util.base_uri+'/management/vm'})
    this.rules_hander = new data_handler({uri: config_util.base_uri+'/management/rules'})

    this.get_rule_name_by_id = (ruleId) => {
        if (ruleId == 0) {
            return "-";
        }
        const data = this.rules_hander.load_data();
        for (let idx in data) {
            if (data[idx].id === ruleId) {
                return data[idx].name
            }
        }
        return "--"
    }
    this.get_host_name_by_id = (hostId) => {
        if (hostId == 0) {
            return "";
        }
        const data = this.host_hander.load_data();
        for (let idx in data) {
            if (data[idx].id === hostId) {
                return data[idx].name
            }
        }
        return "--"
    }
    this.get_os_category_name_by_id = (id) => {
        let os_category = this.category_hander.load_data();
        for (let idx in os_category) {
            if (os_category[idx].id === id) {
                return os_category[idx].categoryName
            }
        }
        return "--"
    }
    this.find_calculation_by_id = (id) => {
        let find_data = null;
        let calculation = this.calculation_hander.load_data();
        $.each(calculation, function (idx, row) {
            if (row.id == id) {
                find_data = row;
            }
        })
        return find_data
    }
    this.find_template_by_id = (id) => {
        let find_data = undefined;
        let template = this.template_hander.load_data();
        $.each(template, function (idx, data) {
            if (data.id === id) {
                find_data = data;
            }
        })
        return find_data;
    }
    this.get_template_name_by_id = (templateId) => {
        if (templateId == 0) {
            return "-";
        }
        let template = this.template_hander.load_data();
        for (let idx in template) {
            if (template[idx].id === templateId) {
                return template[idx].name
            }
        }
        return "模版已删除"
    }
    this.find_network_by_id = (id) => {
        let find_data = undefined
        let network = this.network_hander.load_data();
        $.each(network, function (idx, data) {
            if (data.id === id) {
                find_data = data;
            }
        })
        return find_data;
    }
    this.find_group_by_id = (id) => {
        let find_data = null;
        let group = this.group_hander.load_data();
        $.each(group, function (idx, row) {
            if (row.id == id) {
                find_data = row;
            }
        })
        return find_data
    }

    this.find_group_name_by_id = (id) => {
        let find_data = '默认';
        let group = this.group_hander.load_data();
        $.each(group, function (idx, row) {
            if (row.id == id) {
                find_data = row.name;
            }
        })
        return find_data
    }

    this.find_cluster_by_id = (id) => {
        let find_data = null;
        let cluster = this.cluster_hander.load_data();
        $.each(cluster, function (idx, row) {
            if (row.id == id) {
                find_data = row;
            }
        })
        return find_data
    }
    this.get_cluster_name_by_id = (clusterId) => {
        let cluster = this.cluster_hander.load_data();
        for (let idx in cluster) {
            if (cluster[idx].id === clusterId) {
                return cluster[idx].name
            }
        }
        return "--"
    }
    this.get_storage_name_by_id = (storageId) => {
        let storage = this.storage_hander.load_data();
        for (let idx in storage) {
            if (storage[idx].id === storageId) {
                return storage[idx].name
            }
        }
        return "--"
    }
    this.find_storage_by_id = (id) => {
        let find_data = undefined;
        let storage = this.storage_hander.load_data();
        $.each(storage, function (idx, data) {
            if (data.id === id) {
                find_data = data;
            }
        })
        return find_data;
    }


    this.find_vm_by_id = (id) => {
        let find_data = null;
        let vm = this.vm_hander.load_data();
        $.each(vm, function (idx, row) {
            if (row.id == id) {
                find_data = row;
            }
        })
        return find_data
    }
    this.get_vm_name_by_id = (id) => {
        if (id === 0) {
            return ""
        }
        let vm = this.vm_hander.load_data();
        for (let idx in vm) {
            if (vm[idx].id === id) {
                return vm[idx].description
            }
        }
        return "--"
    }
}

