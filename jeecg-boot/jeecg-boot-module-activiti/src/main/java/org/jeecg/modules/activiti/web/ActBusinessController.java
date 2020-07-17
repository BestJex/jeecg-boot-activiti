package org.jeecg.modules.activiti.web;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.activiti.entity.ActBusiness;
import org.jeecg.modules.activiti.entity.ActZprocess;
import org.jeecg.modules.activiti.entity.ActivitiConstant;
import org.jeecg.modules.activiti.service.Impl.ActBusinessServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *@author PanMeiCheng
 *@date 2020-04-02
 *@version 1.0
 */
@RestController
@RequestMapping("/actBusiness")
@Slf4j
@Transactional
@Api(tags="流程")
public class ActBusinessController {
    @Autowired
    ActBusinessServiceImpl actBusinessService;
    @Autowired
    ActZprocessServiceImpl actZprocessService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    ISysBaseAPI sysBaseAPI;
    /*添加申请草稿状态*/
    @AutoLog(value = "流程-添加申请草稿状态")
    @ApiOperation(value="流程-添加申请草稿状态", notes="业务表单参数数据一并传过来！")
    @PostMapping(value = "/add")
    public Result add(@ApiParam(value = "流程定义Id" ,required = true) String procDefId,
                      @ApiParam(value = "申请标题" ,required = true) String procDeTitle,
                      @ApiParam(value = "数据表名" ,required = true) String tableName,
                       HttpServletRequest request){
        /*保存业务表单数据到数据库表*/
        String tableId = IdUtil.simpleUUID();
        //如果前端上传了id
        String id = request.getParameter("id");
        if( id != null && !id.equals("")){
            tableId = id;
        }
        boolean isNew = actBusinessService.saveApplyForm(tableId, request);
        ActBusiness actBusiness = new ActBusiness();
        if (isNew){
            // 新增数据 保存至我的申请业务
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String username = sysUser.getUsername();
            actBusiness.setId(UUIDGenerator.generate());
            actBusiness.setUserId(username);
            actBusiness.setTableId(tableId);
            actBusiness.setProcDefId(procDefId);
            String title = request.getParameter(ActivitiConstant.titleKey);
            if (StrUtil.isNotBlank(title)){
                actBusiness.setTitle(title);
            }else {
                actBusiness.setTitle(procDeTitle);
            }
            actBusiness.setTableName(tableName);
            actBusinessService.save(actBusiness);
        } else {
            actBusiness = actBusinessService.getOne(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getTableId,tableId).last("limit 1"));
        }
        Map<String,String> map = new HashMap<>();
        map.put("id",actBusiness.getId());
        return Result.ok(map);
    }
    /*获取业务表单信息*/
    @AutoLog(value = "流程-获取业务表单信息")
    @ApiOperation(value="流程-获取业务表单信息", notes="获取业务表单信息")
    @RequestMapping(value = "/getForm", method = RequestMethod.GET)
    public Result getForm(@ApiParam(value = "业务表数据id" ,required = true)String tableId,
                          @ApiParam(value = "业务表名" ,required = true)String tableName){
        if (StrUtil.isBlank(tableName)){
            return Result.error("参数缺省！");
        }
        Map<String, Object> applyForm = actBusinessService.getApplyForm(tableId, tableName);
        return Result.ok(applyForm);
    }
    /*修改业务表单信息*/
    @AutoLog(value = "流程-修改业务表单信息")
    @ApiOperation(value="流程-修改业务表单信息", notes="业务表单参数数据一并传过来!")
    @RequestMapping(value = "/editForm", method = RequestMethod.POST)
    public Result editForm(@ApiParam(value = "业务表数据id" ,required = true)String id,
                           HttpServletRequest request){
        /*保存业务表单数据到数据库表*/
        actBusinessService.saveApplyForm(id,request);
        return Result.ok();
    }
    /*通过id删除草稿状态申请*/
    @AutoLog(value = "流程-通过id删除草稿状态申请")
    @ApiOperation(value="流程-通过id删除草稿状态申请", notes="通过id删除草稿状态申请")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result delByIds(@ApiParam(value = "流程扩展表id，多个,号相连" ,required = true) String ids){

        for(String id : ids.split(",")){
            ActBusiness actBusiness = actBusinessService.getById(id);
            if(actBusiness.getStatus()!=ActivitiConstant.STATUS_TO_APPLY){
                return Result.error("删除失败, 仅能删除草稿状态的申请");
            }
            // 删除关联业务表
            actBusinessService.deleteBusiness(actBusiness.getTableName(), actBusiness.getTableId());
            actBusinessService.removeById(id);
        }
        return Result.ok("删除成功");
    }
    /*提交申请 启动流程*/
    @AutoLog(value = "流程-提交申请 启动流程")
    @ApiOperation(value="流程-提交申请 启动流程", notes="提交申请 启动流程。")
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public Result apply(ActBusiness act ){
        ActBusiness actBusiness = actBusinessService.getById(act.getId());
        if(actBusiness==null){
            return Result.error("actBusiness表中该id不存在");
        }
        String tableId = actBusiness.getTableId();
        String tableName = actBusiness.getTableName();
        act.setTableId(tableId);
        Map<String, Object> busiData = actBusinessService.getBusiData(tableId, tableName);

        if (MapUtil.isNotEmpty(busiData)&&busiData.get(ActivitiConstant.titleKey)!=null){
            //如果表单里有 标题  更新一下
            actBusiness.setTitle(busiData.get(ActivitiConstant.titleKey)+"");
        }
        String processInstanceId = actZprocessService.startProcess(act);
        actBusiness.setProcInstId(processInstanceId);
        actBusiness.setStatus(ActivitiConstant.STATUS_DEALING);
        actBusiness.setResult(ActivitiConstant.RESULT_DEALING);
        actBusiness.setApplyTime(new Date());
        actBusinessService.updateById(actBusiness);
        //修改业务表的流程字段
        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"启动");
        return Result.ok("操作成功");
    }
    /*撤回申请*/
    @AutoLog(value = "流程-撤回申请")
    @ApiOperation(value="流程-撤回申请", notes="撤回申请")
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public Result<Object> cancel(@ApiParam(value = "流程扩展表id" ,required = true) @RequestParam String id,
                                 @ApiParam(value = "流程实例id" ,required = true) @RequestParam String procInstId,
                                 @ApiParam(value = "撤销理由原因说明" ,required = false) @RequestParam(required = false) String reason){

        if(StrUtil.isBlank(reason)){
            reason = "";
        }
        runtimeService.deleteProcessInstance(procInstId, "canceled-"+reason);
        ActBusiness actBusiness = actBusinessService.getById(id);
        actBusiness.setStatus(ActivitiConstant.STATUS_CANCELED);
        actBusiness.setResult(ActivitiConstant.RESULT_TO_SUBMIT);
        actBusinessService.updateById(actBusiness);
        //修改业务表的流程字段
        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"撤回");
        return Result.ok("操作成功");
    }
    /**/
    @AutoLog(value = "流程-流程列表")
    @ApiOperation(value="流程-流程列表", notes="流程列表，登录用户的流程列表")
    @RequestMapping(value = "/listData" ,method = RequestMethod.GET)
    public Result listData(ActBusiness param, HttpServletRequest request){
        LambdaQueryWrapper<ActBusiness> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ActBusiness::getCreateTime);
        if (StrUtil.isNotBlank(param.getTitle())) queryWrapper.like(ActBusiness::getTitle,param.getTitle());
        if (param.getStatus()!=null) queryWrapper.eq(ActBusiness::getStatus,param.getStatus());
        //多个
        String statuss = request.getParameter("statuss");
        if (StrUtil.isNotBlank(statuss)) {
            queryWrapper.in(ActBusiness::getStatus,StrUtil.split(statuss,","));
        }
        if (param.getResult()!=null) queryWrapper.eq(ActBusiness::getResult,param.getResult());
        String createTime_begin = request.getParameter("createTime_begin");
        if (StrUtil.isNotBlank(createTime_begin))
            queryWrapper.ge(ActBusiness::getCreateTime,createTime_begin);
        String createTime_end = request.getParameter("createTime_end");
        if (StrUtil.isNotBlank(createTime_end))
            queryWrapper.le(ActBusiness::getCreateTime,createTime_end);

        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        queryWrapper.eq(ActBusiness::getUserId,loginUser.getUsername());
        //流程类型
        String type = request.getParameter("type");
        if (StrUtil.isNotBlank(type)){
            List<String> actBusinessIdsByType = actBusinessService.listByTypeApp(type);
            if (actBusinessIdsByType.size()==0){ // 没有符合的 目的是上下面的查询条件也查不到
                queryWrapper.in(ActBusiness::getId, Lists.newArrayList(""));
            }else {
                queryWrapper.in(ActBusiness::getId,actBusinessIdsByType);
            }
        }
        List<ActBusiness> actBusinessList = actBusinessService.list(queryWrapper);

        // 是否需要业务数据
        String needData = request.getParameter("needData");
        actBusinessList.forEach(e -> {
            if(StrUtil.isNotBlank(e.getProcDefId())){
                ActZprocess actProcess = actZprocessService.getById(e.getProcDefId());
                e.setRouteName(actProcess.getRouteName());
                e.setProcessName(actProcess.getName());
            }
            if(ActivitiConstant.STATUS_DEALING.equals(e.getStatus())){
                // 关联当前任务
                List<Task> taskList = taskService.createTaskQuery().processInstanceId(e.getProcInstId()).list();
                if(taskList!=null&&taskList.size()==1){
                    e.setCurrTaskName(taskList.get(0).getName());
                }else if(taskList!=null&&taskList.size()>1){
                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<taskList.size()-1;i++){
                        sb.append(taskList.get(i).getName()+"、");
                    }
                    sb.append(taskList.get(taskList.size()-1).getName());
                    e.setCurrTaskName(sb.toString());
                }
            }
            if (StrUtil.equals(needData,"true")){ // 需要业务数据
                Map<String, Object> applyForm = actBusinessService.getApplyForm(e.getTableId(), e.getTableName());
                e.setDataMap(applyForm);
            }
        });
        return Result.ok(actBusinessList);
    }
}
