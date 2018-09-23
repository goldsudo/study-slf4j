package com.goldsudo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/logTest")
public class LogTestController {
    Logger logger = LoggerFactory.getLogger(LogTestController.class);

    @RequestMapping(value = "/doLog",method = RequestMethod.GET)
    public void doLog(){
        logger.debug("这是一条debug级别的日志");
        logger.info("这是一条info级别的日志");
        logger.warn("这是一条warn级别的日志");
        logger.error("这是一条error级别的日志");
    }
}
