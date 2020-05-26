<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">

          <a-col :md="4" :sm="8">
            <a-form-item label="开始日期">
              <a-date-picker v-model="queryParam.day_begin" />
            </a-form-item>
          </a-col>
          <a-col :md="4" :sm="8">
            <a-form-item label="结束日期">
              <a-date-picker v-model="queryParam.day_end" />
            </a-form-item>
          </a-col>
        <a-col :md="4" :sm="8">
            <a-form-item label="是否工作日">
              <a-select v-model="queryParam.isworking" placeholder="请选择">
                <a-select-option value="">请选择</a-select-option>
                <a-select-option value="1">是</a-select-option>
                <a-select-option value="0">否</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="周序号">
              <a-input placeholder="请输入周序号" v-model="queryParam.weeknum"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8" >
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <!--<a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>-->
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <!--<a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>-->
      <a-button type="primary" icon="download" @click="handleExportXls('时间表')">导出</a-button>
     <!-- <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>-->
      <a-button type="primary" icon="setting" @click="visibleAotu = true">一键生成</a-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="batchDel"><a-icon type="delete"/>删除</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /></a-button>
      </a-dropdown>
    </div>

    <a-modal title="选择年份" v-model="visibleAotu" @ok="aotuData()">
      <a-spin tip="数据操作中,请稍后。。。" :spinning="spinning">
        <a-alert message="将会删除所选年份数据并重新生成国家法定节假日、调休，来源于国务院网站：http://www.gov.cn/zhengce 如有错误，请根据实际手动设置修改！"></a-alert>
        <div style="height: 4px"></div>
        <a-select v-model="aotuYear" placeholder="选择年份" style="width: 100%">
          <a-select-option v-for="year in years" :key="year" :value="year">{{year}}年</a-select-option>
        </a-select>
      </a-spin>
    </a-modal>
    <!-- table区域-begin -->
    <div>
      <!--<div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>-->
<!-- :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"-->
      <a-table
        ref="table"
        size="middle"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"

        @change="handleTableChange">

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>

          <!--<a-divider type="vertical" />
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>-->
        </span>

      </a-table>
    </div>
    <!-- table区域-end -->

    <!-- 表单区域 -->
    <xdSchedule-modal ref="modalForm" @ok="modalFormOk"></xdSchedule-modal>
  </a-card>

</template>

<script>
  import XdScheduleModal from './XdScheduleModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { deleteAction, getAction,downFile } from '@/api/manage'
  import { getHolidaysByYear } from '@/config/holidays'
  import axios from 'axios'
  import moment from 'moment'
  export default {
    name: "XdScheduleList",
    mixins:[JeecgListMixin],
    components: {
      XdScheduleModal
    },
    data () {
      return {
        years:[],
        aotuYear:moment().format("YYYY"),
        visibleAotu:false,
        description: '时间表管理页面',
        // 表头
        columns: [
          {
            title: '#',
            dataIndex: '',
            key:'rowIndex',
            width:60,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
           },
		   {
            title: 'day',
            align:"center",
            dataIndex: 'day'
           },
		   {
            title: 'week',
            align:"center",
            dataIndex: 'week'
           },
		   {
            title: '星期几',
            align:"center",
            dataIndex: 'dayofweek'
           },
		   {
            title: '是否工作日',
            align:"center",
            dataIndex: 'isworking'
           },
		   {
            title: '周序号',
            align:"center",
            dataIndex: 'weeknum'
           },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' },
          }
        ],
		url: {
          list: "/xd_schedule/xdSchedule/list",
          delete: "/xd_schedule/xdSchedule/delete",
          deleteBatch: "/xd_schedule/xdSchedule/deleteBatch",
          exportXlsUrl: "xd_schedule/xdSchedule/exportXls",
          importExcelUrl: "xd_schedule/xdSchedule/importExcel",
          aotuData: "xd_schedule/xdSchedule/aotuData",
          holidayApi: "http://www.easybots.cn/api/holiday.php",
       },
        spinning: false,//加载
      }
  },
  computed: {
    importExcelUrl: function(){
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
    }
  },
    created: function () {
      let thisY = moment().format("YYYY");
      var ys = [];
      for (let i = thisY*1+1; i >= 2019 ; i--) {
        ys.push(i);
      }
      this.years = ys
    },
    methods: {
      loadData(arg) {
        if(!this.url.list){
          this.$message.error("请设置url.list属性!")
          return
        }
        //加载数据 若传入参数1则加载第一页的内容
        if (arg === 1) {
          this.ipagination.current = 1;
        }
        var params = this.getQueryParams();//查询条件
        if (params.day_begin)params.day_begin = params.day_begin.format("YYYY-MM-DD")
        if (params.day_end)params.day_end = params.day_end.format("YYYY-MM-DD")
        this.loading = true;
        getAction(this.url.list, params).then((res) => {
          if (res.success) {
            this.dataSource = res.result.records;
            this.ipagination.total = res.result.total;
          }
          if(res.code===510){
            this.$message.warning(res.message)
          }
          this.loading = false;
        })
      },
      /*一键同步国家法定节假日*/
      aotuData: function () {
        var _this = this;
        _this.spinning = true;
        let yyyy = _this.aotuYear;
        let holidaysByYear = getHolidaysByYear(yyyy);
        if (holidaysByYear) {
          let body = JSON.stringify(holidaysByYear);
          console.log(body)
          getAction(this.url.aotuData, {yyyy:_this.aotuYear,body:body}).then((res) => {
            if (res.success) {
              console.log(res)
              this.$message.success(res.message)
              _this.visibleAotu = false;
              _this.loadData();
            }
            if(res.code===510){
              this.$message.warning(res.message)
            }
            if(res.code===500){
              this.$message.error(res.message)
            }
            _this.spinning = false;
          })

        } else {
          _this.$message.error(`没有${yyyy}年度节假日配置！`)
          _this.spinning = false;
        }



      },
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>