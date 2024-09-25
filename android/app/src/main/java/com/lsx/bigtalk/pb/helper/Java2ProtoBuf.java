package com.lsx.bigtalk.pb.helper;


import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.pb.IMBaseDefine;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 */
public class Java2ProtoBuf {
    /**----enum 转化接口--*/
    public static IMBaseDefine.MsgType getProtoMsgType(int msgType){
        switch (msgType){
            case AppConstant.DBConstant.MSG_TYPE_GROUP_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_TEXT;
            case AppConstant.DBConstant.MSG_TYPE_GROUP_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_AUDIO;
            case AppConstant.DBConstant.MSG_TYPE_SINGLE_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_AUDIO;
            case AppConstant.DBConstant.MSG_TYPE_SINGLE_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_TEXT;
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" +msgType);
        }
    }


    public static IMBaseDefine.SessionType getProtoSessionType(int sessionType){
        switch (sessionType){
            case AppConstant.DBConstant.SESSION_TYPE_SINGLE:
                return IMBaseDefine.SessionType.SESSION_TYPE_SINGLE;
            case AppConstant.DBConstant.SESSION_TYPE_GROUP:
                return IMBaseDefine.SessionType.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +sessionType);
        }
    }
}
