package com.lsx.bigtalk.imservice.entity;

import android.text.TextUtils;

import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.DB.entity.PeerEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.Security;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.config.MessageConstant;
import com.lsx.bigtalk.imservice.support.SequenceNumberMaker;
import com.lsx.bigtalk.ui.adapter.album.ImageItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class ImageMessageEntity extends MessageEntity implements Serializable {
    // local path
    private String path = "";
    // remote url
    private String url = "";
    private int loadStatus;
    private static final HashMap<Long, ImageMessageEntity> imageMessageMap = new HashMap<Long, ImageMessageEntity>();

    public ImageMessageEntity() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    private ImageMessageEntity(MessageEntity entity) {
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        sessionKey = entity.getSessionKey();
        content = entity.getContent();
        msgType = entity.getMsgType();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }

    public static ImageMessageEntity parseFromNet(MessageEntity entity) throws JSONException {
        String strContent = entity.getContent();
        if (strContent.startsWith(MessageConstant.IMAGE_MSG_PREFIX)
                && strContent.endsWith(MessageConstant.IMAGE_MSG_SUFFIX)) {
            ImageMessageEntity imageMessage = new ImageMessageEntity(entity);
            imageMessage.setDisplayType(DBConstant.SHOW_TYPE_IMAGE);
            String imageUrl = strContent.substring(MessageConstant.IMAGE_MSG_PREFIX.length());
            imageUrl = imageUrl.substring(0, imageUrl.indexOf(MessageConstant.IMAGE_MSG_SUFFIX));

            JSONObject extraContent = new JSONObject();
            extraContent.put("path", "");
            extraContent.put("url", imageUrl);
            extraContent.put("loadStatus", MessageConstant.IMAGE_UNLOAD);
            String imageContent = extraContent.toString();
            imageMessage.setContent(imageContent);
            imageMessage.setUrl(TextUtils.isEmpty(imageUrl) ? null : imageUrl);
            imageMessage.setContent(strContent);
            imageMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
            imageMessage.setStatus(MessageConstant.MSG_SUCCESS);
            return imageMessage;
        } else {
            throw new RuntimeException("no image type,cause by [start,end] is wrong!");
        }
    }

    public static ImageMessageEntity parseFromDB(MessageEntity entity) {
        if (entity.getDisplayType() != DBConstant.SHOW_TYPE_IMAGE) {
            throw new RuntimeException("parseFromDB, error show type");
        }
        ImageMessageEntity imageMessage = new ImageMessageEntity(entity);
        String originContent = entity.getContent();
        JSONObject extraContent;
        try {
            extraContent = new JSONObject(originContent);
            imageMessage.setPath(extraContent.getString("path"));
            imageMessage.setUrl(extraContent.getString("url"));
            int loadStatus = extraContent.getInt("loadStatus");

            //todo temp solution
            if (loadStatus == MessageConstant.IMAGE_LOADING) {
                loadStatus = MessageConstant.IMAGE_UNLOAD;
            }
            imageMessage.setLoadStatus(loadStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageMessage;
    }

    public static ImageMessageEntity buildForSend(ImageItem item, UserEntity fromUser, PeerEntity peerEntity) {
        ImageMessageEntity msg = new ImageMessageEntity();
        if (new File(item.getImagePath()).exists()) {
            msg.setPath(item.getImagePath());
        } else {
            if (new File(item.getThumbnailPath()).exists()) {
                msg.setPath(item.getThumbnailPath());
            } else {
                msg.setPath(null);
            }
        }

        int nowTime = (int) (System.currentTimeMillis() / 1000);

        msg.setFromId(fromUser.getPeerId());
        msg.setToId(peerEntity.getPeerId());
        msg.setCreated(nowTime);
        msg.setUpdated(nowTime);
        msg.setDisplayType(DBConstant.SHOW_TYPE_IMAGE);
        // content 自动生成的
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT :
                DBConstant.MSG_TYPE_SINGLE_TEXT;
        msg.setMsgType(msgType);

        msg.setStatus(MessageConstant.MSG_SENDING);
        msg.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
        msg.buildSessionKey(true);
        return msg;
    }

    public static ImageMessageEntity buildForSend(String takePhotoSavePath, UserEntity fromUser, PeerEntity peerEntity) {
        ImageMessageEntity imageMessage = new ImageMessageEntity();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        imageMessage.setFromId(fromUser.getPeerId());
        imageMessage.setToId(peerEntity.getPeerId());
        imageMessage.setUpdated(nowTime);
        imageMessage.setCreated(nowTime);
        imageMessage.setDisplayType(DBConstant.SHOW_TYPE_IMAGE);
        imageMessage.setPath(takePhotoSavePath);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
                : DBConstant.MSG_TYPE_SINGLE_TEXT;
        imageMessage.setMsgType(msgType);

        imageMessage.setStatus(MessageConstant.MSG_SENDING);
        imageMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
        imageMessage.buildSessionKey(true);
        return imageMessage;
    }


    public static synchronized void addToImageMessageList(ImageMessageEntity msg) {
        try {
            if (msg != null && msg.getId() != null) {
                imageMessageMap.put(msg.getId(), msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ImageMessageEntity> getImageMessageList() {
        ArrayList<ImageMessageEntity> imageList = new ArrayList<>();
        for (Long aLong : imageMessageMap.keySet()) {
            imageList.add(imageMessageMap.get(aLong));
        }
        // 降序
        Collections.sort(imageList, new Comparator<ImageMessageEntity>() {
            public int compare(ImageMessageEntity image1, ImageMessageEntity image2) {
                Integer a = image1.getUpdated();
                Integer b = image2.getUpdated();
                if (a.equals(b)) {
                    return image2.getId().compareTo(image1.getId());
                }
                return b.compareTo(a);
            }
        });
        return imageList;
    }

    public static synchronized void clearImageMessageList() {
        imageMessageMap.clear();
    }

    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("path", path);
            extraContent.put("url", url);
            extraContent.put("loadStatus", loadStatus);
            return extraContent.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getSendContent() {
        String sendContent = MessageConstant.IMAGE_MSG_PREFIX + url + MessageConstant.IMAGE_MSG_SUFFIX;
        String encryptedSendContent = new String(Security.getInstance().EncryptMsg(sendContent));
        return encryptedSendContent.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * -----------------------set/get------------------------
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
}
