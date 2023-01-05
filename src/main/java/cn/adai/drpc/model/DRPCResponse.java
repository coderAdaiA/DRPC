package cn.adai.drpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: coderAdai
 * @date 2022/08/18 11:39
 * @description: TODO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DRPCResponse {
    private boolean success;
    private boolean isBusinessException;
    private boolean isList;
    private String resultType;
    private String resultValue;
    private String message;

    public static DRPCResponse makeSuccessResult(String resultType, String resultValue) {
        return new DRPCResponse(true, false, false, resultType, resultValue, "success");
    }

    public static DRPCResponse makeSuccessListResult(String resultType, String resultValue) {
        return new DRPCResponse(true, false, true, resultType, resultValue, "success");
    }

    public static DRPCResponse makeFailResult(String reason) {
        return new DRPCResponse(false, false, false, null, null, reason);
    }

    public static DRPCResponse makeFailResult(String reason, boolean isBusinessException) {
        return new DRPCResponse(false, isBusinessException, false, null, null, reason);
    }
}
