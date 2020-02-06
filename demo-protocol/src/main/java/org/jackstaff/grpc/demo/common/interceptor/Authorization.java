package org.jackstaff.grpc.demo.common.interceptor;

import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.Interceptor;

/**
 * @author reco@jackstaff.org
 * @see Interceptor
 * @see org.jackstaff.grpc.demo.common.interceptor.Credential
 */
public class Authorization implements Interceptor {

    static final String AUTHORIZATION = "Authorization";

    @Override
    public void before(Context context) throws Exception {
        if (!validate(context, context.getMetadata(AUTHORIZATION))){
            throw new SecurityException("NO Permission "+context.getType().getName()+":"+context.getMethod().getName());
        }
    }

    protected boolean validate(Context context, String token){
        //for test deny method, will return false
        if (context.getMethod().getName().equals("deny")) return false;

        //@TODO validate the credential/token
        return ("theToken"+context.getMethod().getName()).equals(token);
    }

}
