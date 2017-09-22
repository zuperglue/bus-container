package com.zuperglue;

import java.io.IOError;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zuperglue on 17-06-02.
 */

@ControllerAdvice
@RestController
public class AppExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public void handleIllegalArgumentException(IllegalArgumentException e,HttpServletResponse response)throws IOException{
        log.error("IllegalArgumentException",e);
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(value = IOError.class)
    public void handleBaseException(IOError e ,HttpServletResponse response)throws IOException{
        log.error("IOError",e);
        response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(),e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = InternalError.class)
    public void handleInternalError(InternalError e ,HttpServletResponse response)throws IOException{
        log.error("InternalError",e);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public void handleException(Exception e, HttpServletResponse response) throws IOException{
        log.error("Exception",e);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage());
    }
    //Hello


}
