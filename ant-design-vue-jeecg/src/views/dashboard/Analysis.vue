<template>
  <div>
    <b-echarts :option="option" @click="click" style="height: 300px;width: 100%"></b-echarts>
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
            text: '百度echarts 封装案例'
          },
          xAxis: {
            type: 'category',
            data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            data: [80, 932, 901, 934, 1290, 1330, 1320],
            type: 'line'
          }]
        }

      }
    },
    created() {

    },
    methods: {

      click(param) {
        console.log(param)
      }
    }
  }
</script>