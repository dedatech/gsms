package com.gsms.gsms.infra.exception;

/**
 * 错误码接口
 * 所有业务错误码枚举需实现此接口
 */
public interface ErrorCode {


    
    /**
     * 获取错误码
     */
    int getCode();
    
    /**
     * 获取错误信息
     */
    String getMessage();
}
