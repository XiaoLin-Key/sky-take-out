package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Select;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList =new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //根据日期查询当日已完成营业额
            //select sum(order_amount) from orders where order_time >= begin and order_time < end and status = 5
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.countStatusAndTurnover(map);
            turnover = turnover == null ? 0.0 : turnover;
            //将营业额添加到列表中
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList =new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //根据日期查询当日新增用户数
            //select count(*) from user where create_time >= begin and create_time < end
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);
            //根据日期查询当日用户总量
            //select count(*) from user where create_time <= end
            Integer totalUserCount = userMapper.countByCreateTime(map);
            totalUserCount = totalUserCount == null ? 0 : totalUserCount;
            //将用户总量添加到列表中
            totalUserList.add(totalUserCount);

            map.put("begin", beginTime);
            //根据日期查询当日新增用户数
            //select count(*) from user where create_time >= begin and create_time < end
            Integer newUserCount = userMapper.countByCreateTime(map);
            newUserCount = newUserCount == null ? 0 : newUserCount;
            //将新增用户数添加到列表中
            newUserList.add(newUserCount);
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList =new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer totalValidOrderCount = 0;
        for (LocalDate localDate : dateList) {
            //根据日期查询当日订单数
            //select count(*) from orders where order_time >= begin and order_time < end
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            //根据日期查询当日订单数
            //select count(*) from orders where order_time >= begin and order_time < end
            Integer orderCount = orderMapper.getOrderCount(map);
            orderCount = orderCount == null ? 0 : orderCount;
            //将订单数添加到列表中
            orderCountList.add(orderCount);
            //将订单数累加到总订单数中
            totalOrderCount += orderCount;

            //根据日期查询当日有效订单数
            //select count(*) from orders where order_time >= begin and order_time < end and status = 5
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.getOrderCount(map);
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;
            //将有效订单数添加到列表中
            validOrderCountList.add(validOrderCount);
            //将有效订单数累加到总有效订单数中
            totalValidOrderCount += validOrderCount;
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .validOrderCount(totalValidOrderCount)
                .totalOrderCount(totalOrderCount)
                //订单完成率 = 总有效订单数 / 总订单数
                .orderCompletionRate(totalOrderCount == 0 ? 0.0 : (double) totalValidOrderCount / totalOrderCount)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTopSales(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        Map map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", Orders.COMPLETED);
        List<Map> top10 = orderMapper.getTopSales(map);
        if(top10 != null && top10.size() > 0){
            for (Map map2 : top10) {
                nameList.add((String) map2.get("name"));
                // 1. 先转成 Number 对象
                Number numberVal = (Number) map2.get("number");

                // 2. 如果不为空，调用 intValue() 方法获取 int 值
                Integer number = (numberVal != null) ? numberVal.intValue() : 0;
                //将销量添加到列表中
                numberList.add(number);
            }
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库，获取最近30天的营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);

        //通过poi将数据写入excel文件中
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try{
            //基于模版创建新的excel文件
            XSSFWorkbook excel= new XSSFWorkbook(inputStream);
            //获取第一个sheet页
            XSSFSheet sheet = excel.getSheetAt(0);
            //填充时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            //获取第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //获取第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            //填充明细数据
            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessDataVO2 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessDataVO2.getTurnover());
                row.getCell(3).setCellValue(businessDataVO2.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVO2.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataVO2.getUnitPrice());
                row.getCell(6).setCellValue(businessDataVO2.getNewUsers());
            }
            //通过输出流将excel文件下载到客户端
            ServletOutputStream out= response.getOutputStream();
            excel.write(out);
            //关闭流
            out.close();
            excel.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
