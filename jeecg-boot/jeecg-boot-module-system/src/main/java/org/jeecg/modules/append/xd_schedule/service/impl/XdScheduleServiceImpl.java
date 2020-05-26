package org.jeecg.modules.append.xd_schedule.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.append.xd_schedule.entity.XdSchedule;
import org.jeecg.modules.append.xd_schedule.mapper.XdScheduleMapper;
import org.jeecg.modules.append.xd_schedule.service.IXdScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 时间表
 * @Author: jeecg-boot
 * @Date:   2019-10-29
 * @Version: V1.0
 */
@Service
@Slf4j
public class XdScheduleServiceImpl extends ServiceImpl<XdScheduleMapper, XdSchedule> implements IXdScheduleService {
    @Autowired
    RedisUtil redisUtil;
    /**单例 缓存，避免多次重复查*/
    private List<XdSchedule> ScheduleList;
    /**有效期*/
    private Date ScheduleListDate = new Date();

    public List<XdSchedule> getDays() {
        if (CollectionUtil.isEmpty(ScheduleList)||ScheduleListDate.getTime()<new Date().getTime()){
            ScheduleList = this.list(new QueryWrapper<XdSchedule>().orderByAsc("day"));
            ScheduleListDate = DateUtils.getDate(new Date().getTime()+60*1000);//缓存一分
        }
        return this.ScheduleList;
    }

    /**
     * 获取weeknum 的日期
     * @param weeknum
     * @return
     */
    public List<XdSchedule> getDays(String weeknum) {
        getDays();
        List<XdSchedule> weekFilter = ScheduleList.stream().filter(o -> StrUtil.equals(o.getWeeknum().intValue() + "", weeknum)).collect(Collectors.toList());
        return weekFilter;
    }
    /**
     * 获取 yyyyMM 月的时间
     */
    public List<XdSchedule> getDays(Integer yyyyMM) {
        getDays();
        if (yyyyMM==null){
            return ScheduleList;
        }
       return ScheduleList.stream().filter(d->{
            String formatDate = DateUtils.formatDate(d.getDay(),"yyyyMM");
            return formatDate.equals(yyyyMM+"");
        }).collect(Collectors.toList());
    }
    /**
     * 获取 weeknum 周的时间范围
     * @param weeknum 周序号
     * @param pattern 时间格式
     * @param span 间隔字符
     */
    public String getDateRange(String weeknum, String pattern, String span) {
        try {
            List<XdSchedule> weekFilter = getDays(weeknum);
            Date start = weekFilter.get(0).getDay();
            Date end = weekFilter.get(weekFilter.size()-1).getDay();
            return String.format("%s %s %s",DateUtils.formatDate(start,pattern),span,DateUtils.formatDate(end,pattern));
        } catch (Exception e) {
            log.warn("获取周时间范围 : "+e.getMessage());
            return String.format("请检查时间表预设数据是否正确：%s 周",weeknum);
        }


    }
/**
 * 生成国家法定节假日
 * */
    public void aotuData(String yyyy,String body) {
        if (StrUtil.isBlank(yyyy)){
            throw new RuntimeException("年份不能为空");
        }
        /*包含所有节假日、休息日*/
        log.info("请求获取到的body数据 ： "+body);
        JSONObject apiData = null;
        if (StrUtil.isNotBlank(body)) {
            try {
                apiData = JSON.parseObject(body);
            } catch (Exception e) {
                throw new RuntimeException("节假日的数据有误！");
            }
        }


        //生成该年的数据信息
        int year = Convert.toInt(yyyy);//定义一个字段，接收输入的年份
        Calendar calendar = new GregorianCalendar();//定义一个日历，变量作为年初
        Calendar calendarEnd = new GregorianCalendar();//定义一个日历，变量作为年末
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);//设置年初的日期为1月1日
        calendarEnd.set(Calendar.YEAR, year);
        calendarEnd.set(Calendar.MONTH, 11);
        calendarEnd.set(Calendar.DAY_OF_MONTH, 31);//设置年末的日期为12月31日

        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        ArrayList<XdSchedule> addDays = Lists.newArrayList();
        while(calendar.getTime().getTime()<=calendarEnd.getTime().getTime()){//用一整年的日期循环
            Date date = calendar.getTime();
            String format = sf.format(date);

            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);//第几周
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);//第几天
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);//一周中第几周
            int month = calendar.get(Calendar.MONTH)+1;//月份
            //是否工作日
            boolean isworking = dayOfWeek!=1&&dayOfWeek!=7;//周末休息
            if (StrUtil.isNotBlank(body)){
                Integer dayType = apiData.getInteger(format);
                if (dayType!=null){ //1法定调休，2法定放假
                    if (dayType==1) isworking = true;
                    if (dayType==2) isworking = false;
                }
            }
            String weekNum;
            String week;
            if (dayOfWeek==1){
                //星期天 按国内习惯，计入上一周
                weekOfYear -= 1;
            }
            yyyy = year+"";//还原
            //若选择的日期是月份12但是返回的周为1   计入下一年的第一周  yyyy+1
            if (month==12&&weekOfYear==1){
                yyyy = (year+1)+"";
            }
            //若选择的日期是月份1但是返回的周很大,一年有50多个周    计入上一年的最后一周 yyyy-1
            if (month==1&&weekOfYear>50){
                yyyy= (year-1)+"";
            }
            if (weekOfYear<10){
                week = String.format("%s年0%s周",yyyy,weekOfYear);
                weekNum = String.format("%s0%s",yyyy,weekOfYear);
            }else {
                week = String.format("%s年%s周",yyyy,weekOfYear);
                weekNum = String.format("%s%s",yyyy,weekOfYear);
            }
            XdSchedule xdSchedule = new XdSchedule();
            xdSchedule.init();
            xdSchedule.setWeek(week);
            xdSchedule.setWeeknum(Convert.toInt(weekNum));
            xdSchedule.setYear(Convert.toInt(year));
            xdSchedule.setMonth(Convert.toInt(month));
            if (dayOfWeek==1){
                xdSchedule.setDayofweek(Convert.toInt(7));
            }else {
                xdSchedule.setDayofweek(Convert.toInt(dayOfWeek-1));
            }
            xdSchedule.setDay(date);
            xdSchedule.setCreateTime(new Date());
            xdSchedule.setIsworking(isworking?"1":"0");
            addDays.add(xdSchedule);

            calendar.add(Calendar.DAY_OF_MONTH, 1);//日期+1
        }
        this.remove(new LambdaQueryWrapper<XdSchedule>().likeRight(XdSchedule::getYear,year));
        this.saveBatch(addDays);

    }



}
