package github.cloudtour.exception;

import github.cloudtour.enums.RpcErrorMessageEnum;

/**
 * 自定义Rpc异常
 * @author cloudtour
 * @version 1.0
 */
public class RpcException extends RuntimeException{


    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }

}
