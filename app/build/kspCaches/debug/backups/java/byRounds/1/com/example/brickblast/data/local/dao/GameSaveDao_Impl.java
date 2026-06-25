package com.example.brickblast.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.brickblast.data.local.entity.GameSaveEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GameSaveDao_Impl implements GameSaveDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GameSaveEntity> __insertionAdapterOfGameSaveEntity;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public GameSaveDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGameSaveEntity = new EntityInsertionAdapter<GameSaveEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `game_save` (`id`,`round`,`score`,`totalBalls`,`coinsThisSession`,`mode`,`bricksJson`,`collectablesJson`,`timestamp`,`hasActiveSave`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GameSaveEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRound());
        statement.bindLong(3, entity.getScore());
        statement.bindLong(4, entity.getTotalBalls());
        statement.bindLong(5, entity.getCoinsThisSession());
        statement.bindString(6, entity.getMode());
        statement.bindString(7, entity.getBricksJson());
        statement.bindString(8, entity.getCollectablesJson());
        statement.bindLong(9, entity.getTimestamp());
        final int _tmp = entity.getHasActiveSave() ? 1 : 0;
        statement.bindLong(10, _tmp);
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM game_save WHERE id = 1";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final GameSaveEntity save, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGameSaveEntity.insert(save);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final Continuation<? super GameSaveEntity> $completion) {
    final String _sql = "SELECT * FROM game_save WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GameSaveEntity>() {
      @Override
      @Nullable
      public GameSaveEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRound = CursorUtil.getColumnIndexOrThrow(_cursor, "round");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfTotalBalls = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBalls");
          final int _cursorIndexOfCoinsThisSession = CursorUtil.getColumnIndexOrThrow(_cursor, "coinsThisSession");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfBricksJson = CursorUtil.getColumnIndexOrThrow(_cursor, "bricksJson");
          final int _cursorIndexOfCollectablesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "collectablesJson");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfHasActiveSave = CursorUtil.getColumnIndexOrThrow(_cursor, "hasActiveSave");
          final GameSaveEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpRound;
            _tmpRound = _cursor.getInt(_cursorIndexOfRound);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final int _tmpTotalBalls;
            _tmpTotalBalls = _cursor.getInt(_cursorIndexOfTotalBalls);
            final int _tmpCoinsThisSession;
            _tmpCoinsThisSession = _cursor.getInt(_cursorIndexOfCoinsThisSession);
            final String _tmpMode;
            _tmpMode = _cursor.getString(_cursorIndexOfMode);
            final String _tmpBricksJson;
            _tmpBricksJson = _cursor.getString(_cursorIndexOfBricksJson);
            final String _tmpCollectablesJson;
            _tmpCollectablesJson = _cursor.getString(_cursorIndexOfCollectablesJson);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpHasActiveSave;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasActiveSave);
            _tmpHasActiveSave = _tmp != 0;
            _result = new GameSaveEntity(_tmpId,_tmpRound,_tmpScore,_tmpTotalBalls,_tmpCoinsThisSession,_tmpMode,_tmpBricksJson,_tmpCollectablesJson,_tmpTimestamp,_tmpHasActiveSave);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<GameSaveEntity> observe() {
    final String _sql = "SELECT * FROM game_save WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"game_save"}, new Callable<GameSaveEntity>() {
      @Override
      @Nullable
      public GameSaveEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRound = CursorUtil.getColumnIndexOrThrow(_cursor, "round");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfTotalBalls = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBalls");
          final int _cursorIndexOfCoinsThisSession = CursorUtil.getColumnIndexOrThrow(_cursor, "coinsThisSession");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfBricksJson = CursorUtil.getColumnIndexOrThrow(_cursor, "bricksJson");
          final int _cursorIndexOfCollectablesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "collectablesJson");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfHasActiveSave = CursorUtil.getColumnIndexOrThrow(_cursor, "hasActiveSave");
          final GameSaveEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpRound;
            _tmpRound = _cursor.getInt(_cursorIndexOfRound);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final int _tmpTotalBalls;
            _tmpTotalBalls = _cursor.getInt(_cursorIndexOfTotalBalls);
            final int _tmpCoinsThisSession;
            _tmpCoinsThisSession = _cursor.getInt(_cursorIndexOfCoinsThisSession);
            final String _tmpMode;
            _tmpMode = _cursor.getString(_cursorIndexOfMode);
            final String _tmpBricksJson;
            _tmpBricksJson = _cursor.getString(_cursorIndexOfBricksJson);
            final String _tmpCollectablesJson;
            _tmpCollectablesJson = _cursor.getString(_cursorIndexOfCollectablesJson);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpHasActiveSave;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasActiveSave);
            _tmpHasActiveSave = _tmp != 0;
            _result = new GameSaveEntity(_tmpId,_tmpRound,_tmpScore,_tmpTotalBalls,_tmpCoinsThisSession,_tmpMode,_tmpBricksJson,_tmpCollectablesJson,_tmpTimestamp,_tmpHasActiveSave);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
