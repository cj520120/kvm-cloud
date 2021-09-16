<template>
  <el-dialog :close-on-click-modal="false" :close-on-press-escape="false" :title="title" :visible.sync="dialog_visible" center width="600px">

    <el-tabs v-model="activeName">
      <el-tab-pane label="网络" name="first">
        <div  v-loading="loading">
          <div id="network" style="width: 600px;height:400px;"/>
        </div>
      </el-tab-pane>
      <el-tab-pane label="磁盘" name="second">
        <div  v-loading="loading">
        <div id="disk" style="width: 600px;height:400px;"  />
        </div>
      </el-tab-pane>
      <el-tab-pane label="CPU" name="third">
        <div  v-loading="loading">
        <div id="cpu" style="width: 600px;height:400px;"/>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: "Metric",
  data() {
    return {
      id: 0,
      activeName: "first",
      title: "",
      dialog_visible: false ,
      loading:false
    }
  },
  methods: {
    init_data(instance) {
      this.id = instance.id
      this.title = `${instance.description}-运行状态`
      this.dialog_visible = true
      this.load()
    },
    load() {
      this.loading=true
      this.axios_get(`/management/vm/statistics?vmId=${this.id}`).then(res => {
        this.loading=false
        if (res.data.code === 0 && res.data.data) {
          const network_data = {
            in: [],
            out: []
          };
          const disk_data = {
            in: [],
            out: []
          };
          const cpu_data = []
          for (let idx in res.data.data) {
            let row=res.data.data[idx]
            const time = this.parse_date(row.time);
            network_data.in.push([time, (row.receive / 1024).toFixed(2)])
            network_data.out.push([time, (row.send / 1024).toFixed(2)])
            disk_data.in.push([time, (row.read / 1024).toFixed(2)])
            disk_data.out.push([time, (row.write / 1024).toFixed(2)])
            cpu_data.push([time, row.cpu])
          }
          console.log(network_data)
          const disk = echarts.init(document.getElementById('disk'));
          const network = echarts.init(document.getElementById('network'));
          const cpu = echarts.init(document.getElementById('cpu'));
          disk.setOption(this.render_double_chat(disk_data, "disk"));
          network.setOption(this.render_double_chat(network_data, "network"));
          cpu.setOption(this.render_one_chat(cpu_data, "Cpu使用率"));
        }
      })
    },
    render_one_chat(data)   {
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          },
          backgroundColor: '#fff',
          padding: 10,
          textStyle: {
            fontSize: 12,
            color: '#152934',
            lineHeight: 24
          }
        },
        xAxis: {
          type: 'time',
          boundaryGap: false
        },
        yAxis: {
          type: 'value',
          boundaryGap: [0, '100%']
        },
        series: [{
          name: 'Cpu使用率',
          type: 'line',
          symbol: 'none',
          markPoint: {
            label: {
              normal: {
                show: true,
                backgroundColor: '#fff',
                position: 'top',
                color: '#5AAAFA',
                borderColor: 'rgba(90,170,250,0.3)',
                borderWidth: 1,
                padding: 8
              }
            },
            symbol: 'circle',
            itemStyle: {
              normal: {
                borderColor: 'rgba(90,170,250,0.3)',
                borderWidth: 15
              }
            },
            symbolSize: 7,
            data: [{
              type: 'max',
              name: 'Max'
            }]
          },
          lineStyle: {
            normal: {
              color: '#5AAAFA',
              width: 1
            }
          },
          areaStyle: {
            normal: {
              color: '#5AAAFA',
              opacity: 0.5
            }
          },
          connectNulls: false,
          data: data
        }]
      };
    },
    render_double_chat(data, type) {
      const unit = type === 'disk' ? 'KBps' : 'KBps'
      const titles = type === 'disk' ? ['读速率', '写速率'] : ['上行速率', '下行速率']
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          },
          backgroundColor: '#fff',
          padding: 10,
          textStyle: {
            fontSize: 12,
            color: '#152934',
            lineHeight: 24
          },
          extraCssText: 'box-shadow: 0 0 3px rgba(0, 0, 0, 0.3); border-radius: 0;',
          formatter: (params) => {
            let result = `${params[0].data[0]} <br/>`;
            params.map(item => {
              result += `${item.seriesName} : ${isNaN(item.value[1]) ? '-' : item.value[1]} ${unit}</br>`
            })
            return result
          }
        },
        yAxis: [
          {
            splitLine: {
              show: true,
              lineStyle: {
                type: 'dotted',
                color: 'rgba(155, 155, 155, 0.5)'
              }
            },
            axisLine: {
              show: false
            },
            axisLabel: {
              color: '#5A6872',
              fontSize: 11
            },
            axisTick: {show: false},
            type: 'value'
          }
        ],
        xAxis: [{
          type: 'time',   // x轴为 时间轴
          splitLine: {show: false},
          axisLine: {
            lineStyle: {width: 0}
          },
          axisLabel: {
            color: '#5A6872',
            fontSize: 11
          },
          axisTick: {show: false},
          boundaryGap: false,
          data: data.in.map(function (item) {
            return item[0]
          })
        }],
        legend: {data: titles},
        color: ['#41D6C3', '#5AAAFA'],
        series: [
          {
            name: type === 'disk' ? '读速率' : '上行速率',
            type: 'line',
            symbol: 'none',
            markPoint: {
              label: {
                normal: {
                  show: true,
                  backgroundColor: '#fff',
                  position: 'top',
                  color: '#41D6C3',
                  borderColor: 'rgba(65,214,195,0.3)',
                  borderWidth: 1,
                  padding: 8,
                  formatter: `{b}: {c} ${unit}`
                }
              },
              symbol: 'circle',
              itemStyle: {
                normal: {
                  borderColor: 'rgba(65,214,195,0.3)',
                  borderWidth: 15
                }
              },
              symbolSize: 7,
              data: [
                {type: 'max', name: 'Max'}
              ]
            },
            lineStyle: {normal: {color: '#41D6C3', width: 1}},
            areaStyle: {normal: {color: '#41D6C3', opacity: 0.5}},
            data: data['out']
          },
          {
            name: type === 'disk' ? '写速率' : '下行速率',
            type: 'line',
            symbol: 'none',
            markPoint: {
              label: {
                normal: {
                  show: true,
                  backgroundColor: '#fff',
                  position: 'top',
                  color: '#5AAAFA',
                  borderColor: 'rgba(90,170,250,0.3)',
                  borderWidth: 1,
                  padding: 8,
                  formatter: `{b}: {c} ${unit}`
                }
              },
              symbol: 'circle',
              itemStyle: {
                normal: {
                  borderColor: 'rgba(90,170,250,0.3)',
                  borderWidth: 15
                }
              },
              symbolSize: 7,
              data: [
                {type: 'max', name: 'Max'}
              ]
            },
            lineStyle: {normal: {color: '#5AAAFA', width: 1}},
            areaStyle: {normal: {color: '#5AAAFA', opacity: 0.5}},
            connectNulls: false,
            data: data['in']
          }
        ]
      }
    }
  }

}
</script>

<style scoped>

</style>