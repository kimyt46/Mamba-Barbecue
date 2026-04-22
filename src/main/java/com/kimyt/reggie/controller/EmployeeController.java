package com.kimyt.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimyt.reggie.entity.Employee;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.common.BaseContext;
import com.kimyt.reggie.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;



    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        String password=employee.getPassword();

        password=DigestUtils.md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Employee::getUsername,employee.getUsername());


        Employee emp=employeeService.getOne(queryWrapper);

        if(emp==null){
            return R.error("登录失败");

        }

        if(!emp.getPassword().equals(password)){

            return R.error("登录失败");

        }

        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }

        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);


    }

@PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){

        request.getSession().removeAttribute("employee");
        return R.success("退出成功");

}

@PostMapping("/register")
    public R<String> register(@RequestBody Employee employee){
        log.info("员工注册，注册信息：{}", employee.toString());

        // 1. 验证必填字段
        if (StringUtils.isEmpty(employee.getUsername())) {
            return R.error("用户名不能为空");
        }
        if (StringUtils.isEmpty(employee.getPassword())) {
            return R.error("密码不能为空");
        }
        if (StringUtils.isEmpty(employee.getName())) {
            return R.error("姓名不能为空");
        }

        // 2. 验证密码长度
        if (employee.getPassword().length() < 6) {
            return R.error("密码长度不能少于6位");
        }

        // 3. 检查用户名是否已存在
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        long count = employeeService.count(queryWrapper);
        if (count > 0) {
            return R.error("用户名已存在");
        }

        // 4. 设置默认值
        employee.setPassword(DigestUtils.md5DigestAsHex(employee.getPassword().getBytes()));
        employee.setStatus(1); // 激活状态

        // 5. 处理公共字段（注册时没有登录用户）
        // 临时设置当前用户ID为0，表示系统自动创建
        BaseContext.setCurrentId(0L);

        // 6. 保存员工信息
        employeeService.save(employee);

        log.info("员工注册成功，用户名：{}", employee.getUsername());
        return R.success("注册成功");
    }

@PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){

        log.info("新增员工，员工信息为：{}",employee.toString());

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        Long empID=(Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empID);

        employee.setUpdateUser(empID);

        employee.setCreateTime(LocalDateTime.now());

        employee.setUpdateTime(LocalDateTime.now());

        employeeService.save(employee);
        log.info("新增成功，用户信息为：{}",employee.toString());

        return R.success("新增成功");


}

@GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        Page pageInfo=new Page(page,pageSize);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        employee.setUpdateTime(LocalDateTime.now());
        Long empID=(Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empID);
        employeeService.updateById(employee);
        return R.success("修改成功");


    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){

        log.info("根据id查询员工信息： ");
        Employee employee=employeeService.getById(id);
        if (employee!=null){
            return R.success(employee);

        }

        return R.error("没有查到信息");



    }





}
