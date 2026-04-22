package com.kimyt.reggie.common;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {Resource.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
    log.error(ex.getMessage());
    if(ex.getMessage().contains("Duplicate entry")){
        String[] spilt=ex.getMessage().split(" ");
        String msg=spilt[2]+"已存在";
        return R.error(msg);

    }
    return R.error("未知错误");

}





}
