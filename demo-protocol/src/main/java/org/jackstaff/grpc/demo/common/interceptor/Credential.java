package org.jackstaff.grpc.demo.common.interceptor;

import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.interceptor.Interceptor;

/**
 * @author reco@jackstaff.org
 * @see org.jackstaff.grpc.interceptor.Interceptor
 * @see org.jackstaff.grpc.demo.common.interceptor.Authorization
 */
public class Credential implements Interceptor {

    @Override
    public boolean before(Context context) throws Exception {
        context.setMetadata(Authorization.AUTHORIZATION, getCredential(context));
        return true;
    }

    private String getCredential(Context context){
        //@TODO generate credential/token, JWT? UserPwd?
        return "theToken"+context.getMethod().getName();
    }


}