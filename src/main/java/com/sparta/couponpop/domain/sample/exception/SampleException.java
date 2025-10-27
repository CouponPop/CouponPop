package com.sparta.couponpop.domain.sample.exception;

import com.sparta.couponpop.common.exception.ErrorCode;
import com.sparta.couponpop.common.exception.GlobalException;

public class SampleException extends GlobalException {

    public SampleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
