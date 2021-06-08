const oneChart = (data, title) => {

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
}
const doubleChart = (data, type) => {
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
                var result = `${params[0].data[0]} <br/>`
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
            data: data['in'].map(function (item) {
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

