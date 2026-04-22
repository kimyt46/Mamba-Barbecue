package com.kimyt.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimyt.reggie.entity.Employee;
import com.kimyt.reggie.mapper.EmployeeMapper;
import com.kimyt.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends  ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
