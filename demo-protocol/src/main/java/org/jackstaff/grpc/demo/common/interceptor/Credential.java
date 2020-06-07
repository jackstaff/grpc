package org.jackstaff.grpc.demo.common.interceptor;

import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author reco@jackstaff.org
 * @see Interceptor
 * @see org.jackstaff.grpc.demo.common.interceptor.Authorization
 */
public class Credential implements Interceptor {
    Logger logger = LoggerFactory.getLogger(Credential.class);

    @Override
    public void before(Context context) {
        context.setMetadata(Authorization.AUTHORIZATION, getCredential(context));
    }

    private String getCredential(Context context){
//        logger.info("Credential.getCredential..theToken"+context.getMethod().getName());
        //@TODO generate credential/token, JWT? UserPwd?
        return "theToken"+context.getMethod().getName();
    }


}