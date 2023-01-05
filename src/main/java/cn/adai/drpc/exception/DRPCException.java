package cn.adai.drpc.exception;

/**
 * @author: coderAdai
 * @date 2022/08/18 11:55
 * @description: TODO
 */
public class DRPCException extends Exception {
    public DRPCException(String message) {
        super(message);
    }

    public DRPCException(Throwable e) {
        super(e);
    }

    public DRPCException(String message, Throwable e) {
        super(message, e);
    }
    
    public DRPCException(String msgTemplate, Object... args) {
        this(String.format(msgTemplate.replace("{}", "%s"), args));
    }
}
