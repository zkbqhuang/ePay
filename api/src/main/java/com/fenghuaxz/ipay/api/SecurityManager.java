package com.fenghuaxz.ipay.api;

import com.fenghuaxz.rpcframework.annotations.Rpc;

@Rpc
public interface SecurityManager {

    boolean isLocked();

    boolean tryLock(String base64RSAPublicKey, String securityCode);
}
