package org.jackstaff.grpc.demo.common.interceptor;

import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.Interceptor;

/**
 * @author reco@jackstaff.org
 * @see Interceptor
 * @see org.jackstaff.grpc.demo.common.interceptor.Authorization
 */
public class Credential implements Interceptor {

    @Override
    public void before(Context context) throws Exception {
        context.setMetadata(Authorization.AUTHORIZATION, getCredential(context));
    }

    private String getCredential(Context context){
        //@TODO generate credential/token, JWT? UserPwd?
        return "theToken"+context.getMethod().getName();
    }


}