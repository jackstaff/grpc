package org.jackstaff.grpc.demo.common.interceptor;

import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.Interceptor;
import org.jackstaff.grpc.StatusRuntimeException;
import org.jackstaff.grpc.demo.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author reco@jackstaff.org
 * @see Interceptor
 * @see org.jackstaff.grpc.demo.common.interceptor.Credential
 */
public class Authorization implements Interceptor {

    static final String AUTHORIZATION = "Authorization";
    Logger logger = LoggerFactory.getLogger(Authorization.class);

    @Override
    public void before(Context context) {
        if (!validate(context, context.getMetadata(AUTHORIZATION))){
            throw new StatusRuntimeException(ErrorCode.PERMISSION_DENIED, context.getMethod().getName()+" No Permission");
        }
    }

    protected boolean validate(Context context, String token){
        //for test deny method, will return false
        if (context.getMethod().getName().equals("deny")) return false;
//        logger.info("Authorization.validate..theToken"+context.getMethod().getName()+","+token);

        //@TODO validate the credential/token
        return ("theToken"+context.getMethod().getName()).equals(token);
    }

}
