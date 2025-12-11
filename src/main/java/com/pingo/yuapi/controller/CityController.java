package com.pingo.yuapi.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 城市管理相关接口
 */
@CrossOrigin
@RestController
@RequestMapping("/cities")
public class CityController {

    /**
     * 获取已开通城市列表
     */
    @GetMapping("/available")
    public List<String> getAvailableCities() {
        // TODO: 这里应该从数据库或配置文件中获取已开通城市
        // 暂时返回更多模拟数据
        return Arrays.asList(
                // 直辖市
                "北京市",
                "天津市",
                "上海市", 
                "重庆市",
                // 广东省
                "广州市",
                "深圳市",
                "东莞市",
                "佛山市",
                "惠州市",
                "中山市",
                "珠海市",
                "汕头市",
                "江门市",
                // 河北省
                "保定市",
                "廊坊市",
                "石家庄市",
                "唐山市",
                "秦皇岛市",
                "邯郸市",
                "邢台市",
                "张家口市",
                "承德市",
                "沧州市",
                "衡水市",
                // 江苏省
                "南京市",
                "苏州市",
                "无锡市",
                "常州市",
                "镇江市",
                "南通市",
                "泰州市",
                "扬州市",
                "盐城市",
                "连云港市",
                "徐州市",
                "淮安市",
                "宿迁市",
                // 浙江省
                "杭州市",
                "宁波市",
                "温州市",
                "嘉兴市",
                "湖州市",
                "绍兴市",
                "金华市",
                "衢州市",
                "舟山市",
                "台州市",
                "丽水市",
                // 山东省
                "济南市",
                "青岛市",
                "淄博市",
                "枣庄市",
                "东营市",
                "烟台市",
                "潍坊市",
                "济宁市",
                "泰安市",
                "威海市",
                "日照市",
                "临沂市",
                "德州市",
                "聊城市",
                "滨州市",
                "菏泽市"
        );
    }
}