package org.jeecg.modules.activiti.entity;

/**
 * 流程业务字典
 * @author PanMeiCheng
 * @version 1.0
 * @date 2020/7/3
 */
public interface ActBusiTypeEnum {
    /**用章类型*/
    String type_yz = "A01A03A01";
    /**印章管理*/
    String type_yz_yzgl = "A01A03A01A03";
    /**印章借用*/
    String type_yz_yzjy = "A01A03A01A02";
    /**印章使用*/
    String type_yz_yzsy = "A01A03A01A01";
    /**业务接待*/
    String type_ywjd = "A01A03A02A01";
}
