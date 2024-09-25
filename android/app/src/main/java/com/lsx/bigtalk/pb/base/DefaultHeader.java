package com.lsx.bigtalk.pb.base;


import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.service.support.SequenceNumberMaker;
import com.lsx.bigtalk.logs.Logger;

public class DefaultHeader extends Header {
    private final Logger logger = Logger.getLogger(DefaultHeader.class);

    public DefaultHeader(int serviceId, int commandId) {
        setVersion((short) AppConstant.SysConstant.PROTOCOL_VERSION);
        setFlag((short) AppConstant.SysConstant.PROTOCOL_FLAG);
        setServiceId((short)serviceId);
        setCommandId((short)commandId);
        short seqNo = SequenceNumberMaker.getInstance().make();
        setSeqnum(seqNo);
        setReserved((short)AppConstant.SysConstant.PROTOCOL_RESERVED);

        logger.d("packet#construct Default Header -> serviceId:%d, commandId:%d, seqNo:%d", serviceId, commandId, seqNo);
    }
}
