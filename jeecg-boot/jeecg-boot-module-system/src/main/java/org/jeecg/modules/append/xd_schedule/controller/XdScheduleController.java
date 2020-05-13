package org.jeecg.modules.append.xd_schedule.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.append.xd_schedule.entity.XdSchedule;
import org.jeecg.modules.append.xd_schedule.service.IXdScheduleService;
import org.jeecg.modules.append.xd_schedule.service.impl.XdScheduleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

 /**
 * @Description: 时间表
 * @Author: jeecg-boot
 * @Date:   2019-10-29
 * @Version: V1.0
 */
@Slf4j
@Api(tags="时间表")
@RestController
@RequestMapping("/xd_schedule/xdSchedule")
public class XdScheduleController extends JeecgController<XdSchedule, IXdScheduleService> {
	@Autowired
	private XdScheduleServiceImpl xdScheduleService;
	
	/**
	 * 分页列表查询
	 *
	 * @param xdSchedule
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "时间表-分页列表查询")
	@ApiOperation(value="时间表-分页列表查询", notes="时间表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(XdSchedule xdSchedule,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<XdSchedule> queryWrapper = QueryGenerator.initQueryWrapper(xdSchedule, req.getParameterMap());
		Page<XdSchedule> page = new Page<XdSchedule>(pageNo, pageSize);
		queryWrapper.orderByAsc("DAY");
		IPage<XdSchedule> pageList = xdScheduleService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * 添加
	 *
	 * @param xdSchedule
	 * @return
	 */
	@AutoLog(value = "时间表-添加")
	@ApiOperation(value="时间表-添加", notes="时间表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody XdSchedule xdSchedule) {
		xdScheduleService.save(xdSchedule);
		return Result.ok("添加成功！");
	}
	
	/**
	 * 编辑
	 *
	 * @param xdSchedule
	 * @return
	 */
	@AutoLog(value = "时间表-编辑")
	@ApiOperation(value="时间表-编辑", notes="时间表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody XdSchedule xdSchedule) {
		xdScheduleService.updateById(xdSchedule);
		return Result.ok("编辑成功!");
	}
	
	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "时间表-通过id删除")
	@ApiOperation(value="时间表-通过id删除", notes="时间表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		xdScheduleService.removeById(id);
		return Result.ok("删除成功!");
	}
	
	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "时间表-批量删除")
	@ApiOperation(value="时间表-批量删除", notes="时间表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.xdScheduleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "时间表-通过id查询")
	@ApiOperation(value="时间表-通过id查询", notes="时间表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		XdSchedule xdSchedule = xdScheduleService.getById(id);
		return Result.ok(xdSchedule);
	}

  /**
   * 导出excel
   *
   * @param request
   * @param xdSchedule
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, XdSchedule xdSchedule) {
      return super.exportXls(request, xdSchedule, XdSchedule.class, "时间表");
  }

  /**
   * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      return super.importExcel(request, response, XdSchedule.class);
  }
  /**
   * 获取时间范围
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/getDateRange")
  public Result<?> getDateRange(HttpServletRequest request, HttpServletResponse response) {
	  String weeknum = request.getParameter("weeknum");
	  String dateRange = xdScheduleService.getDateRange(weeknum, "yyyy/MM/dd", "--");
	  return Result.ok(dateRange);
  }
  /**通过api同步国家法定节假日*/
  @RequestMapping(value = "/aotuData")
  public Result<?> aotuData(HttpServletRequest request, HttpServletResponse response) {
	  String yyyy = request.getParameter("yyyy");
	  String body = request.getParameter("body");
	  xdScheduleService.aotuData(yyyy,body);
	  return Result.ok();
  }

}
