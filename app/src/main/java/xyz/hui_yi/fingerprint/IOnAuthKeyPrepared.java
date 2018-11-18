package xyz.hui_yi.fingerprint;

/**
 * Created by LiFen on 2018/3/25.
 */

public interface IOnAuthKeyPrepared {
    void onResult(String passwordDigestUsed, boolean isSuccess);
}