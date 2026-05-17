package com.spring.ai.demo09.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 学生学习助手工具集
 * 提供课程表查询、成绩查询、日期查询等能力
 */
@Component
public class StudentTools {

    // 模拟课程表数据
    private static final Map<String, List<String>> COURSE_TABLE = Map.of(
            "MONDAY", List.of("高等数学", "英语精读", "数据结构"),
            "TUESDAY", List.of("线性代数", "计算机网络", "体育"),
            "WEDNESDAY", List.of("操作系统", "Java 编程", "英语精读"),
            "THURSDAY", List.of("数据结构", "高等数学", "思想政治"),
            "FRIDAY", List.of("计算机网络", "线性代数", "自习")
    );

    // 模拟成绩数据
    private static final Map<String, List<ScoreRecord>> SCORE_DB = Map.of(
            "张三", List.of(
                    new ScoreRecord("高等数学", 92),
                    new ScoreRecord("英语精读", 85),
                    new ScoreRecord("数据结构", 88)
            ),
            "李四", List.of(
                    new ScoreRecord("高等数学", 78),
                    new ScoreRecord("英语精读", 91),
                    new ScoreRecord("数据结构", 83)
            )
    );

    /**
     * 工具1：获取当前日期和星期
     */
    @Tool(description = "获取当前的日期和星期几，用于回答涉及时间的问题")
    public String getCurrentDate() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        String weekDay = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE);
        return today + " " + weekDay;
    }

    /**
     * 工具2：查询指定星期几的课程表
     */
    @Tool(description = "查询指定星期几的课程安排，返回当天的课程列表")
    public String querySchedule(
            @ToolParam(description = "星期几，如 MONDAY、TUESDAY、WEDNESDAY、THURSDAY、FRIDAY") String dayOfWeek) {
        List<String> courses = COURSE_TABLE.get(dayOfWeek.toUpperCase());
        if (courses == null) {
            return "没有找到 " + dayOfWeek + " 的课程安排，可能是周末";
        }
        return dayOfWeek + " 的课程为：" + String.join("、", courses);
    }

    /**
     * 工具3：查询学生成绩
     */
    @Tool(description = "查询指定学生的考试成绩，返回该学生所有科目的分数")
    public String queryScore(
            @ToolParam(description = "学生姓名，如 张三、李四") String studentName) {
        List<ScoreRecord> scores = SCORE_DB.get(studentName);
        if (scores == null) {
            return "未找到学生 " + studentName + " 的成绩记录";
        }
        StringBuilder sb = new StringBuilder(studentName + " 的成绩：\n");
        for (ScoreRecord record : scores) {
            sb.append("- ").append(record.subject).append("：").append(record.score).append(" 分\n");
        }
        return sb.toString();
    }

    record ScoreRecord(String subject, int score) {}
}
