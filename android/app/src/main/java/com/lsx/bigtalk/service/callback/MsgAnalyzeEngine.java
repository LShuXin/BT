package com.lsx.bigtalk.service.callback;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import org.json.JSONException;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.MessageEntity;
import com.lsx.bigtalk.Security;
import com.lsx.bigtalk.service.entity.ImageMessageEntity;
import com.lsx.bigtalk.service.entity.RichTextMessageEntity;
import com.lsx.bigtalk.service.entity.TextMessageEntity;
import com.lsx.bigtalk.pb.helper.ProtoBuf2JavaBean;
import com.lsx.bigtalk.pb.IMBaseDefine;


public class MsgAnalyzeEngine {

    public static MessageEntity convertStringToTextOrImageMessage(MessageEntity msg, String strContent) {
        if (TextUtils.isEmpty(strContent.trim())) {
            return null;
        }
        msg.setContent(strContent);

        if (strContent.startsWith(AppConstant.MessageConstant.IMAGE_MSG_PREFIX)
                && strContent.endsWith(AppConstant.MessageConstant.IMAGE_MSG_SUFFIX)) {
            try {
                return ImageMessageEntity.parseFromNet(msg);
            } catch (JSONException e) {
                return null;
            }
        } else {
            return TextMessageEntity.parseFromNet(msg);
        }
    }

    public static String analyzeMessageDisplayType(String content) {
        String finalRes = content;
        String originContent = content;
        while (!originContent.isEmpty()) {
            int nStart = originContent.indexOf(AppConstant.MessageConstant.IMAGE_MSG_PREFIX);
            if (nStart < 0) {
                // no image prefix found
                break;
            } else {
                String subContentString = originContent.substring(nStart);
                int nEnd = subContentString.indexOf(AppConstant.MessageConstant.IMAGE_MSG_SUFFIX);
                if (nEnd < 0) {
                    // no image suffix found
                    break;
                } else {
                    String pre = originContent.substring(0, nStart);

                    originContent = subContentString.substring(nEnd
                            + AppConstant.MessageConstant.IMAGE_MSG_SUFFIX.length());

                    if (!TextUtils.isEmpty(pre) || !TextUtils.isEmpty(originContent)) {
                        finalRes = AppConstant.DBConstant.DISPLAY_FOR_RICH_TEXT;
                    } else {
                        finalRes = AppConstant.DBConstant.DISPLAY_FOR_IMAGE;
                    }
                }
            }
        }
        return finalRes;
    }

    private static List<MessageEntity> analyzeMessageDisplayContent(MessageEntity msg) {
        List<MessageEntity> msgList = new ArrayList<>();
        String originContent = msg.getContent();
        while (!TextUtils.isEmpty(originContent)) {
            int nStart = originContent.indexOf(AppConstant.MessageConstant.IMAGE_MSG_PREFIX);
            if (nStart < 0) {
                // no image prefix found
                String strSplitString = originContent;

                MessageEntity entity = convertStringToTextOrImageMessage(msg, strSplitString);
                if (null != entity) {
                    msgList.add(entity);
                }
                originContent = "";
            } else {
                String subContentString = originContent.substring(nStart);
                int nEnd = subContentString.indexOf(AppConstant.MessageConstant.IMAGE_MSG_SUFFIX);
                if (nEnd < 0) {
                    // no image suffix found
                    String strSplitString = originContent;

                    MessageEntity entity = convertStringToTextOrImageMessage(msg,strSplitString);
                    if (null != entity) {
                        msgList.add(entity);
                    }

                    originContent = "";
                } else {
                    // image found
                    String pre = originContent.substring(0, nStart);
                    MessageEntity entity1 = convertStringToTextOrImageMessage(msg, pre);
                    if (null != entity1) {
                        msgList.add(entity1);
                    }

                    String matchString = subContentString.substring(0, nEnd
                            + AppConstant.MessageConstant.IMAGE_MSG_SUFFIX.length());

                    MessageEntity entity2 = convertStringToTextOrImageMessage(msg,matchString);
                    if (null != entity2) {
                        msgList.add(entity2);
                    }

                    originContent = subContentString.substring(nEnd
                            + AppConstant.MessageConstant.IMAGE_MSG_SUFFIX.length());
                }
            }
        }

        return msgList;
    }

    public static MessageEntity analyzeMessage(IMBaseDefine.MsgInfo msgInfo) {
       MessageEntity messageEntity = new MessageEntity();
       messageEntity.setCreated(msgInfo.getCreateTime());
       messageEntity.setUpdated(msgInfo.getCreateTime());
       messageEntity.setFromId(msgInfo.getFromSessionId());
       messageEntity.setMsgId(msgInfo.getMsgId());
       messageEntity.setMsgType(ProtoBuf2JavaBean.getJavaMsgType(msgInfo.getMsgType()));
       messageEntity.setStatus(AppConstant.MessageConstant.MSG_SUCCESS);
       messageEntity.setContent(msgInfo.getMsgData().toStringUtf8());
       String message = new String(Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));
       messageEntity.setContent(message);
       
       if (!TextUtils.isEmpty(message)) {
           List<MessageEntity> msgList = analyzeMessageDisplayContent(messageEntity);
           if (msgList.size() > 1) {
               return new RichTextMessageEntity(msgList);
           } else if (msgList.isEmpty()) {
              return TextMessageEntity.parseFromNet(messageEntity);
           } else {
               return msgList.get(0);
           }
       } else {
           return TextMessageEntity.parseFromNet(messageEntity);
       }
    }

}
