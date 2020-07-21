<template>
  <div>
    <b-echarts class="a" :option="option" @click="click" style="height: 300px;width: 100%"></b-echarts>
    <index-chart v-if="indexStyle==1"></index-chart>
    <index-bdc v-if="indexStyle==2"></index-bdc>
    <index-task v-if="indexStyle==3"></index-task>
    <div style="width: 100%;text-align: right;margin-top: 20px">
      请选择首页样式：
      <a-radio-group v-model="indexStyle">
        <a-radio :value="1">统计图表</a-radio>
        <a-radio :value="2">统计图表2</a-radio>
        <a-radio :value="3">任务表格</a-radio>
      </a-radio-group>
    </div>
  </div>
</template>

<script>
  import IndexChart from './IndexChart'
  import IndexTask from "./IndexTask"
  import IndexBdc from './IndexBdc'
  import BEcharts from '@/components/append/BEcharts/BEcharts.vue'

  export default {
    name: "Analysis",
    components: {
      IndexChart,
      IndexTask,
      IndexBdc,BEcharts
    },
    data() {
      return {
        indexStyle:1,
        option: {
          title: {
            text: '折线图堆叠'
          },
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['邮件营销', '联盟广告', '视频广告', '直接访问', '搜索引擎']
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          toolbox: {
            feature: {
              saveAsImage: {}
            }
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
          },
          yAxis: {
            type: 'value'
          },
          series: [
            {
              name: '邮件营销',
              type: 'line',
              stack: '总量',
              data: [120, 132, 101, 134, 90, 230, 210]
            },
            {
              name: '联盟广告',
              type: 'line',
              stack: '总量',
              data: [220, 182, 191, 234, 290, 330, 310]
            },
            {
              name: '视频广告',
              type: 'line',
              stack: '总量',
              data: [150, 232, 201, 154, 190, 330, 410]
            },
            {
              name: '直接访问',
              type: 'line',
              stack: '总量',
              data: [320, 332, 301, 334, 390, 330, 320]
            },
            {
              name: '搜索引擎',
              type: 'line',
              stack: '总量',
              data: [820, 932, 901, 934, 1290, 1330, 1320]
            }
          ]
        }

      }
    },
    created() {

    },
    methods: {

      click(param) {
        console.log(param)
      },
    }
  }
</script>