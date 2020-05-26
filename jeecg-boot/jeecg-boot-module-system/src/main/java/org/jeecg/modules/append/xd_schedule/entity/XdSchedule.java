package org.jeecg.modules.append.xd_schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 时间表
 * @Author: jeecg-boot
 * @Date:   2019-10-29
 * @Version: V1.0
 */
@Data
@TableName("xd_schedule")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="xd_schedule对象", description="时间表")
public class XdSchedule {
	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
	@ApiModelProperty(value = "id")
	private String id;
	/**day*/
	@Excel(name = "day", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "day")
	private Date day;
	/**week*/
	@Excel(name = "week", width = 15)
    @ApiModelProperty(value = "week")
	private String week;
	/**是否工作日*/
	@Excel(name = "是否工作日", width = 15)
    @ApiModelProperty(value = "是否工作日")
	private String isworking;
	/**周序号*/
	@Excel(name = "周序号", width = 15)
    @ApiModelProperty(value = "周序号")
	private Integer weeknum;
	/**年份*/
	@Excel(name = "年份", width = 15)
    @ApiModelProperty(value = "年份")
	private Integer year;
	/**周序号*/
	@Excel(name = "月份", width = 15)
    @ApiModelProperty(value = "月份")
	private Integer month;
	/**周序号*/
	@Excel(name = "星期几", width = 15)
    @ApiModelProperty(value = "星期几")
	private Integer dayofweek;
	/**范围 自设字段不一定有值*/
	@TableField(exist = false)
	private String weekround;

	/**创建人*/
	@Excel(name = "创建人", width = 15)
	@ApiModelProperty(value = "创建人")
	private String createBy;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建时间")
	private Date createTime;
	/**修改人*/
	@Excel(name = "修改人", width = 15)
	@ApiModelProperty(value = "修改人")
	private String updateBy;
	/**修改时间*/
	@Excel(name = "修改时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "修改时间")
	private Date updateTime;
	/**删除标识0-正常,1-已删除*/
	@Excel(name = "删除标识0-正常,1-已删除", width = 15)
	@ApiModelProperty(value = "删除标识0-正常,1-已删除")
	private Integer delFlag;

    public void init() {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		this.createBy = user.getUsername();
		this.delFlag = 0;
    }
}
