package org.jeecg.modules.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

/**
 * 备份数据库 定时任务
 * 1.需要配置到定时任务，自行配置；2.需要配置 MysqlPath 环境变量，或修改代码指定路径
 * @Author Scott
 */
@Slf4j
public class CopyDbJob implements Job {

	@Value(value = "${jeecg.path.upload}")
	private String uploadpath;
	@Value(value = "${spring.datasource.dynamic.datasource.master.username}")
	private String username;
	@Value(value = "${spring.datasource.dynamic.datasource.master.password}")
	private String password;
	@Value(value = "${spring.datasource.dynamic.datasource.master.ip}")
	private String host;
	@Value(value = "${spring.datasource.dynamic.datasource.master.dbname}")
	private String exportDatabaseName;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		log.info(String.format(" 数据库备份 普通定时任务,开始!  时间:" + DateUtils.getTimestamp()));
		Runtime runtime = Runtime.getRuntime();
		String command = getExportCommand();
		// 这里其实是在命令窗口中执行的 command 命令行
		try {
			Process exec = runtime.exec(command);
		} catch (Exception e) {
			log.error(" 数据库备份 普通定时任务,错误!",e);
		}
		log.info(String.format(" 数据库备份 普通定时任务,结束!  时间:" + DateUtils.getTimestamp()));
	}
	private String getExportCommand() {
		StringBuffer command = new StringBuffer();
		String port = "3306";// 使用的端口号
		String date = DateUtils.getDate("yyyy-MM-dd--hhmmss");
		String exportPath = uploadpath+"/dbSql";// 导入的目标文件所在的位置
		String path = exportPath+"/"+exportDatabaseName+date+".sql";// 导入的目标文件所在的位置
		File filePath = new File(exportPath);
		if (!filePath.exists()){
			filePath.mkdirs();
		}
		File file = new File(exportPath);
		if (!filePath.exists()){
			file.mkdir();
		}
		//String MysqlPath = "C:/Program Files/MySQL/MySQL Server 5.7/bin/"; //路径是mysql中
		String MysqlPath = ""; //路径是mysql中    需要配置path环境变量
		// 注意哪些地方要空格，哪些不要空格
		command.append(MysqlPath).append("mysqldump -u").append(username).append(" -p").append(password)// 密码是用的小p，而端口是用的大P。
				.append(" -h").append(host).append(" -P").append(port).append(" ").append(exportDatabaseName)
				.append(" -r ").append(path);
		log.info(command.toString());
		return command.toString();
	}
}
