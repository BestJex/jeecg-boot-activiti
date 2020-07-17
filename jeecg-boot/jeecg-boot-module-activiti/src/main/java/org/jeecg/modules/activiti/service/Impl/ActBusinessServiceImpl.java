package org.jeecg.modules.activiti.service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.ComboModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.activiti.entity.ActBusiTypeEnum;
import org.jeecg.modules.activiti.entity.ActBusiness;
import org.jeecg.modules.activiti.entity.ActZprocess;
import org.jeecg.modules.activiti.entity.HistoricTaskVo;
import org.jeecg.modules.activiti.mapper.ActBusinessMapper;
import org.jeecg.modules.activiti.service.IActBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 流程业务扩展表
 * @Author: pmc
 * @Date:   2020-03-30
 * @Version: V1.0
 */
@Service
public class ActBusinessServiceImpl extends ServiceImpl<ActBusinessMapper, ActBusiness> implements IActBusinessService {
    @Autowired
    private TaskService taskService;

    @Autowired
    private ActZprocessServiceImpl actZprocessService;

    @Autowired
    ISysBaseAPI sysBaseAPI;

    @Autowired
    private HistoryService historyService;

    public List<ActBusiness> findByProcDefId(String id) {
       return this.list(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getProcDefId,id));
    }
    /**保存业务表单数据到数据库表
     * <br>该方法相对通用，复杂业务单独定制，套路类似
     * @param tableId 业务表中的数据id
     * @return  如果之前数据库没有 返回 true
     * */
    public boolean saveApplyForm(String tableId, HttpServletRequest request) {
        String tableName = request.getParameter("tableName");
        String filedNames = request.getParameter("filedNames");
        Map<String, Object> busiData = this.baseMapper.getBusiData(tableId, tableName);
        String[] fileds = filedNames.split(",");
        if (MapUtil.isEmpty(busiData)){ //没有，新增逻辑
            StringBuilder filedsB = new StringBuilder("id");
            StringBuilder filedsVB = new StringBuilder("'"+tableId+"'");
            for (String filed : fileds) {
                String dbFiled = oConvertUtils.camelToUnderline(filed);
                if(filed != null && !filed.equals("undefined")){
                    if(request.getParameter(filed) != null){
                        filedsB.append(","+dbFiled);
                        filedsVB.append(",'"+request.getParameter(filed)+"'");
                    }else{
                        filedsB.append(","+dbFiled);
                        filedsVB.append(","+request.getParameter(filed));
                    }
                }
            }
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String userName = sysUser.getUsername();
            filedsB.append(","+"create_by");
            filedsVB.append(",'"+userName+"'");
            filedsB.append(","+"create_time");
            filedsVB.append(",'"+ DateUtils.formatDate(new Date(),"yyyy-MM-dd") +"'");
            this.baseMapper.insertBusiData(String.format("INSERT INTO %s (%s) VALUES (%s)",tableName,filedsB.toString(),filedsVB.toString()));
        }else { //有，修改
            StringBuilder setSql = new StringBuilder();
            for (String filed : fileds) {
                if(filed != null && !filed.equals("undefined")){
                    String parameter = request.getParameter(filed);
                    String dbFiled = oConvertUtils.camelToUnderline(filed);
                    if (parameter==null){
                        setSql.append(String.format("%s = null,",dbFiled));
                    }else {
                        setSql.append(String.format("%s = '%s',",dbFiled, parameter));
                    }
                }
            }
            String substring = setSql.substring(0, setSql.length()-1);//去掉最后一个,号
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String userName = sysUser.getUsername();
            substring += (",update_by = " +    "'"+userName+"'");
            substring += (",update_time = " + "'" +DateUtils.formatDate(new Date(),"yyyy-MM-dd")+"'");
            this.baseMapper.updateBusiData(String.format("update %s set %s where id = '%s'",tableName,substring,tableId));
        }
        return MapUtil.isEmpty(busiData);
    }

    public Map<String, Object> getApplyForm(String tableId, String tableName) {
        Map<String, Object> busiData = this.getBusiData(tableId, tableName);
        Object createBy = busiData.get("createBy");
        if (createBy != null){
            String depName = sysBaseAPI.getDepartNamesByUsername(createBy.toString()).get(0);
            busiData.put("createByDept",depName);
            LoginUser userByName = sysBaseAPI.getUserByName(createBy.toString());
            busiData.put("createByName",userByName.getRealname());
            busiData.put("createByAvatar",userByName.getAvatar());
        }
        return busiData;
    }

    public void deleteBusiness(String tableName, String tableId) {
        this.baseMapper.deleteBusiData(tableId,tableName);
    }
    /**
     *通过类型和任务id查找用户id
     * */
    public List<String> findUserIdByTypeAndTaskId(String type, String taskId) {
        return baseMapper.findUserIdByTypeAndTaskId(type, taskId);
    }

    public void insertHI_IDENTITYLINK(String id, String type, String userId, String taskId, String procInstId) {
        this.baseMapper.insertHI_IDENTITYLINK(id, type, userId, taskId, procInstId);
    }

    public List<String> selectIRunIdentity(String taskId, String type) {
       return baseMapper.selectIRunIdentity(taskId,type);
    }
/**修改业务表的流程字段*/
    public void updateBusinessStatus(String tableName, String tableId, String actStatus) {
        try {
            baseMapper.updateBusinessStatus(tableName,tableId,actStatus);
        } catch (Exception e) {
             // 业务表需要有 act_status字段，没有会报错，不管他
            //e.printStackTrace();
            log.warn(e.getMessage());
        }
    }
    /**
     * 获取业务表单数据并驼峰转换
     * */
    public Map<String, Object> getBusiData(String tableId, String tableName) {
        Map<String, Object> busiData = this.baseMapper.getBusiData(tableId, tableName);
        if (busiData==null) return null;
        HashMap<String, Object> map = Maps.newHashMap();
        for (String key : busiData.keySet()) {
            String camelName = oConvertUtils.camelName(key);
            map.put(camelName,busiData.get(key));
        }
        return map;
    }

    public List<String> listByTypeApp(String type) {
        return this.baseMapper.listByTypeApp(type);
    }

    public Map getTipData(HttpServletRequest req) {
        HashMap<String, Object> reMap = Maps.newHashMap();
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        // 当前用户所有的流程
        List<ActBusiness> actlist = this.list(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getUserId, sysUser.getUsername()));
        long cg_count = actlist.stream().filter(act -> act.getStatus() == 0).count();
        reMap.put("cg_count",cg_count);// 草稿数量
        long sq_count = actlist.stream().filter(act -> act.getStatus() != 0).count();
        reMap.put("sq_count",sq_count);// 非草稿数量
        /*代办统计*/
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(sysUser.getUsername());
        List<Task> taskList = query.list(); // 代办列表
        reMap.put("sp_count", taskList.size());// 所有待办数量
        {   //总用印
            List<String> pDIds1 = this.baseMapper.proc_def_idListByType(ActBusiTypeEnum.type_yz);
            long yz_count = taskList.stream().filter(task -> pDIds1.contains(task.getProcessDefinitionId())).count();
            reMap.put("yz_count", yz_count);// 用印 需要处理的数量
            { //印章管理
                List<String> pDIds = this.baseMapper.proc_def_idListByType(ActBusiTypeEnum.type_yz_yzgl);
                long count = taskList.stream().filter(task -> pDIds.contains(task.getProcessDefinitionId())).count();
                reMap.put("yz_yzgl_count", count);// 用印 需要处理的数量
            }
            { //印章借用
                List<String> pDIds = this.baseMapper.proc_def_idListByType(ActBusiTypeEnum.type_yz_yzjy);
                long count = taskList.stream().filter(task -> pDIds.contains(task.getProcessDefinitionId())).count();
                reMap.put("yz_yzjy_count", count);// 用印 需要处理的数量
            }
            { //印章使用
                List<String> pDIds = this.baseMapper.proc_def_idListByType(ActBusiTypeEnum.type_yz_yzsy);
                long count = taskList.stream().filter(task -> pDIds.contains(task.getProcessDefinitionId())).count();
                reMap.put("yz_yzsy_count", count);// 用印 需要处理的数量
            }
            { //业务接待
                List<String> pDIds = this.baseMapper.proc_def_idListByType(ActBusiTypeEnum.type_ywjd);
                long count = taskList.stream().filter(task -> pDIds.contains(task.getProcessDefinitionId())).count();
                reMap.put("ywjd_count", count);// 用印 需要处理的数量
            }
        }
        /**已办 - 应该不需要这个数量，无意义*/
        /*HistoricTaskInstanceQuery queryDone = historyService.createHistoricTaskInstanceQuery().or()
                .taskCandidateUser(sysUser.getUsername()).taskAssignee(sysUser.getUsername()).endOr().finished();
        long yb_count = queryDone.count();
        reMap.put("yb_count", yb_count);// 用印 需要处理的数量*/
        return reMap;
    }

    /**
     *  获取登陆人的已办
     *
     * @param req
     * @param name 流程名
     * @param categoryId 流程类型
     * @param priority 优先级别
     * @return
     */

    public List<HistoricTaskVo> getHistoricTaskVos(HttpServletRequest req, String name, String categoryId, Integer priority) {
        List<HistoricTaskVo> list = new ArrayList<>();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = loginUser.getUsername();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().or().taskCandidateUser(userId).
                taskAssignee(userId).endOr().finished();

        // 多条件搜索
        query.orderByTaskCreateTime().desc();
        if(StrUtil.isNotBlank(name)){
            query.taskNameLike("%"+name+"%");
        }
        if(StrUtil.isNotBlank(categoryId)){
            query.taskCategory(categoryId);
        }
        if(priority!=null){
            query.taskPriority(priority);
        }
        String searchVal = req.getParameter("searchVal");
        if (StrUtil.isNotBlank(searchVal)){
            //搜索标题、申请人
            List<LoginUser> usersByName = sysBaseAPI.getUsersByName(searchVal);
            List<String> uNames = null;
            if (usersByName.size()==0){
                uNames = Lists.newArrayList("");
            }else {
                uNames = usersByName.stream().map(u->u.getUsername()).collect(Collectors.toList());
            }
            List<ActBusiness> businessList = this.list(new LambdaQueryWrapper<ActBusiness>()
                    .like(ActBusiness::getTitle, searchVal) //标题查询
                    .or().in(ActBusiness::getUserId,uNames)
            );
            if (businessList.size()>0){
                // 定义id
                List<String> pids = businessList.stream().filter(act -> act.getProcInstId()!=null).map(act -> act.getProcInstId()).collect(Collectors.toList());
                query.processInstanceIdIn(pids);
            }else {
                query.processInstanceIdIn(Lists.newArrayList(""));
            }
        }
        String type = req.getParameter("type");
        if (StrUtil.isNotBlank(type)){
            List<String> deployment_idList = this.getBaseMapper().deployment_idListByType(type);
            if (deployment_idList.size()==0){
                query.deploymentIdIn(Lists.newArrayList(""));
            }else {
                query.deploymentIdIn(deployment_idList);
            }
        }
        String createTime_end = req.getParameter("createTime_end");
        if(StrUtil.isNotBlank(createTime_end)){
            Date end = DateUtil.parse(createTime_end);
            query.taskCreatedBefore(DateUtil.endOfDay(end));
        }
        List<HistoricTaskInstance> taskList = query.list();
        // 是否需要业务数据
        String needData = req.getParameter("needData");
        // 转换vo
        List<ComboModel> allUser = sysBaseAPI.queryAllUser();
        Map<String, String> userMap = allUser.stream().collect(Collectors.toMap(ComboModel::getUsername, ComboModel::getTitle));
        taskList.forEach(e -> {
            HistoricTaskVo htv = new HistoricTaskVo(e);
            // 关联委托人
            if(StrUtil.isNotBlank(htv.getOwner())){
                htv.setOwner(userMap.get(htv.getOwner()));
            }
            List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForProcessInstance(htv.getProcInstId());
            for(HistoricIdentityLink hik : identityLinks){
                // 关联发起人
                if("starter".equals(hik.getType())&&StrUtil.isNotBlank(hik.getUserId())){
                    htv.setApplyer(userMap.get(hik.getUserId()));
                }
            }
            // 关联审批意见
            List<Comment> comments = taskService.getTaskComments(htv.getId(), "comment");
            if(comments!=null&&comments.size()>0){
                htv.setComment(comments.get(0).getFullMessage());
            }
            // 关联流程信息
            ActZprocess actProcess = actZprocessService.getById(htv.getProcDefId());
            if(actProcess!=null){
                htv.setProcessName(actProcess.getName());
                htv.setRouteName(actProcess.getRouteName());
            }
            // 关联业务key
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(htv.getProcInstId()).singleResult();
            htv.setBusinessKey(hpi.getBusinessKey());
            ActBusiness actBusiness = this.getById(hpi.getBusinessKey());
            if(actBusiness!=null){
                htv.setTableId(actBusiness.getTableId());
                htv.setTableName(actBusiness.getTableName());
                htv.setTitle(actBusiness.getTitle());
                htv.setStatus(actBusiness.getStatus());
                htv.setResult(actBusiness.getResult());
                if (StrUtil.equals(needData,"true")){ // 需要业务数据
                    Map<String, Object> applyForm = this.getApplyForm(actBusiness.getTableId(), actBusiness.getTableName());
                    htv.setDataMap(applyForm);
                }
            }

            list.add(htv);
        });
        return list;
    }
}
