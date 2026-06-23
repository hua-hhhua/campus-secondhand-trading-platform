package com.campus.trade;

import com.campus.trade.entity.User;
import com.campus.trade.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    //1.插入用户数据
    @Test
    public void saveUserTest() {
        // 采集要插入的用户信息
        User user = new User();
        user.setUsername("mybatis_test_user");
        user.setPassword("123456");
        user.setNickname("MyBatis测试用户");
        user.setEmail("mybatis_test@campus.com");
        user.setPhone("13912345678");
        user.setSchoolId(1);
        user.setAvatar("/avatars/mybatis_test.jpg");
        user.setRole(0);
        user.setStatus(1);

        // 插入数据
        // rows 表示执行 insert into 操作返回受影响的行数，大于0表示插入数据成功，否则插入数据失败
        int rows = userMapper.insert(user);
        String result = rows > 0 ? "插入数据成功" : "插入数据失败";
        System.out.println(result + "，受影响行数：" + rows);
    }

    //2.根据id查询某一条数据
    @Test
    public void getUserByIdTest() {
        // QueryWrapper 在 MyBatis-Plus 中用来封装查询条件的操作类
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // 设置查询条件 相当于 where id = 1
        // 参数1：列名称，参数2：值
        qw.eq("id", 1);

        User user = userMapper.selectOne(qw);
        System.out.println("查询结果: " + user);
    }

    //3.根据id修改数据
    public void updateUserTest() {
        User user = new User();
        user.setId(1);  // 修改 id 为 1 的用户
        user.setNickname("修改后的昵称");
        user.setPhone("18888888888");
        user.setEmail("updated@campus.com");

        int rows = userMapper.updateById(user);
        String result = rows > 0 ? "修改成功" : "修改失败";
        System.out.println(result + "，受影响行数：" + rows);
    }

    //4.根据id删除数据
    @Test
    public void deleteUserByIdTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("id", 1);  // 删除 id 为 1 的用户

        int rows = userMapper.delete(qw);
        String result = rows > 0 ? "删除成功" : "删除失败";
        System.out.println(result + "，受影响行数：" + rows);
    }

    //5.不等于查询
    @Test
    public void notEqualsTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // 参数1：列名称，参数2：值
        // ne not equals 不等于 where username <> 'admin'
        qw.ne("username", "admin");

        java.util.List<User> userList = userMapper.selectList(qw);
        System.out.println("查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("user = " + user);
        }
    }

    //6.like模糊查询
    @Test
    public void likeTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // 相当于 where username like '%test%'
        qw.like("username", "test");

        java.util.List<User> userList = userMapper.selectList(qw);
        System.out.println("like 查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("like user = " + user);
        }
    }

    @Test
    public void likeRightTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // likeRight 'mybatis%'
        qw.likeRight("username", "mybatis");

        java.util.List<User> userList = userMapper.selectList(qw);
        System.out.println("likeRight 查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("likeRight user = " + user);
        }
    }

    @Test
    public void likeLeftTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // likeLeft '%user'
        qw.likeLeft("username", "user");

        java.util.List<User> userList = userMapper.selectList(qw);
        System.out.println("likeLeft 查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("likeLeft user = " + user);
        }
    }

    //7.between and区间查询
    @Test
    public void betweenAndTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // 相当于 where id between 1 and 10
        // 参数1：列名称，参数2：区间查询的起始值，参数3：区间查询的终止值
        qw.between("id", 1, 10);

        java.util.List<User> userList = userMapper.selectList(qw);
        System.out.println("between and 查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("between and user = " + user);
        }
    }

    //8.查询结果降序排序
    @Test
    public void orderByDescTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // 相当于 order by id desc
        qw.orderByDesc("id");

        java.util.List<User> userList = userMapper.selectList(qw);
        System.out.println("按 ID 降序查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("order by id desc: " + user);
        }
    }

    //9.and条件查询
    @Test
    public void andConditionUserTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // 相当于 WHERE (status = 1 AND role = 0)
        queryWrapper.eq("status", 1).eq("role", 0);

        java.util.List<User> userList = userMapper.selectList(queryWrapper);
        System.out.println("and 条件查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("and condition user = " + user);
        }
    }

    //10.or条件查询
    /**
     * or 条件测试：用户状态为正常或者用户角色为管理员
     * 相当于： WHERE (status = 1 OR role = 1)
     */
    @Test
    public void orConditionUserTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        // 相当于： WHERE (status = 1 OR role = 1)
        queryWrapper.eq("status", 1).or().eq("role", 1);

        java.util.List<User> userList = userMapper.selectList(queryWrapper);
        System.out.println("or 条件查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("or condition user = " + user);
        }
    }

    //11.复合条件查询
    @Test
    public void complexConditionTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("status", 1)           // 状态正常
                .eq("role", 0)              // 普通用户
                .eq("school_id", 1)         // 学校ID为1
                .orderByDesc("created_at"); // 按注册时间降序

        java.util.List<User> userList = userMapper.selectList(queryWrapper);
        System.out.println("复合条件查询到 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("complex condition user = " + user);
        }
    }

    @Test
    public void findBySchoolTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("school_id", 1)        // 学校ID为1
                .eq("status", 1)            // 状态正常
                .orderByDesc("created_at"); // 按注册时间降序

        java.util.List<User> userList = userMapper.selectList(queryWrapper);
        System.out.println("学校ID为1的用户有 " + userList.size() + " 条数据");
        for (User user : userList) {
            System.out.println("school user = " + user);
        }
    }

    //12.分页查询(加分页拦截器)
    @Test
    public void pageQueryTest() {
        // 创建分页对象
        Page<User> page = new Page<>(1, 5); // 第1页，每页5条

        // 创建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("status", 1)          // 状态正常
                .orderByDesc("created_at"); // 按注册时间降序

        // 执行分页查询
        Page<User> resultPage = userMapper.selectPage(page, queryWrapper);

        // 获取分页数据
        java.util.List<User> userList = resultPage.getRecords();
        long total = resultPage.getTotal();
        long pages = resultPage.getPages();

        System.out.println("总记录数：" + total);
        System.out.println("总页数：" + pages);
        System.out.println("当前页数据：");
        for (User user : userList) {
            System.out.println("user = " + user);
        }
    }

    //13.统计查询
    @Test
    public void countQueryTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("school_id", 1)  // 查询学校ID为1的用户
                .eq("status", 1);      // 状态正常

        Long count = userMapper.selectCount(queryWrapper);
        System.out.println("学校ID为1的正常用户数量：" + count);
    }

    @Test
    public void countAllTest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        Long totalCount = userMapper.selectCount(queryWrapper);
        System.out.println("用户总数量：" + totalCount);
    }
}