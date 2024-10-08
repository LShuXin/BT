package com.lsx.bigtalk.storage.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.lsx.bigtalk.storage.db.entity.MessageEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table Message.
*/
public class MessageDao extends AbstractDao<MessageEntity, Long> {

    public static final String TABLENAME = "Message";

    /**
     * Properties of entity MessageEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MsgId = new Property(1, int.class, "msgId", false, "MSG_ID");
        public final static Property FromId = new Property(2, int.class, "fromId", false, "FROM_ID");
        public final static Property ToId = new Property(3, int.class, "toId", false, "TO_ID");
        public final static Property SessionKey = new Property(4, String.class, "sessionKey", false, "SESSION_KEY");
        public final static Property Content = new Property(5, String.class, "content", false, "CONTENT");
        public final static Property MsgType = new Property(6, int.class, "msgType", false, "MSG_TYPE");
        public final static Property DisplayType = new Property(7, int.class, "displayType", false, "DISPLAY_TYPE");
        public final static Property Status = new Property(8, int.class, "status", false, "STATUS");
        public final static Property Created = new Property(9, int.class, "created", false, "CREATED");
        public final static Property Updated = new Property(10, int.class, "updated", false, "UPDATED");
    }


    public MessageDao(DaoConfig config) {
        super(config);
    }
    
    public MessageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'Message' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'MSG_ID' INTEGER NOT NULL ," + // 1: msgId
                "'FROM_ID' INTEGER NOT NULL ," + // 2: fromId
                "'TO_ID' INTEGER NOT NULL ," + // 3: toId
                "'SESSION_KEY' TEXT NOT NULL ," + // 4: sessionKey
                "'CONTENT' TEXT NOT NULL ," + // 5: content
                "'MSG_TYPE' INTEGER NOT NULL ," + // 6: msgType
                "'DISPLAY_TYPE' INTEGER NOT NULL ," + // 7: displayType
                "'STATUS' INTEGER NOT NULL ," + // 8: status
                "'CREATED' INTEGER NOT NULL ," + // 9: created
                "'UPDATED' INTEGER NOT NULL );"); // 10: updated
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_Message_STATUS ON Message" +
                " (STATUS);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_Message_CREATED ON Message" +
                " (CREATED);");
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_Message_MSG_ID_SESSION_KEY ON Message" +
                " (MSG_ID,SESSION_KEY);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'Message'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, MessageEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getMsgId());
        stmt.bindLong(3, entity.getFromId());
        stmt.bindLong(4, entity.getToId());
        stmt.bindString(5, entity.getSessionKey());
        stmt.bindString(6, entity.getContent());
        stmt.bindLong(7, entity.getMsgType());
        stmt.bindLong(8, entity.getDisplayType());
        stmt.bindLong(9, entity.getStatus());
        stmt.bindLong(10, entity.getCreated());
        stmt.bindLong(11, entity.getUpdated());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset) ? null : cursor.getLong(offset);
    }    

    /** @inheritdoc */
    @Override
    public MessageEntity readEntity(Cursor cursor, int offset) {
        MessageEntity entity = new MessageEntity( //
            cursor.isNull(offset) ? null : cursor.getLong(offset), // id
            cursor.getInt(offset + 1), // msgId
            cursor.getInt(offset + 2), // fromId
            cursor.getInt(offset + 3), // toId
            cursor.getString(offset + 4), // sessionKey
            cursor.getString(offset + 5), // content
            cursor.getInt(offset + 6), // msgType
            cursor.getInt(offset + 7), // displayType
            cursor.getInt(offset + 8), // status
            cursor.getInt(offset + 9), // created
            cursor.getInt(offset + 10) // updated
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, MessageEntity entity, int offset) {
        entity.setId(cursor.isNull(offset) ? null : cursor.getLong(offset));
        entity.setMsgId(cursor.getInt(offset + 1));
        entity.setFromId(cursor.getInt(offset + 2));
        entity.setToId(cursor.getInt(offset + 3));
        entity.setSessionKey(cursor.getString(offset + 4));
        entity.setContent(cursor.getString(offset + 5));
        entity.setMsgType(cursor.getInt(offset + 6));
        entity.setDisplayType(cursor.getInt(offset + 7));
        entity.setStatus(cursor.getInt(offset + 8));
        entity.setCreated(cursor.getInt(offset + 9));
        entity.setUpdated(cursor.getInt(offset + 10));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(MessageEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(MessageEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
