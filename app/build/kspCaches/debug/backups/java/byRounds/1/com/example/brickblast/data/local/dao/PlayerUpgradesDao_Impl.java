package com.example.brickblast.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.brickblast.data.local.entity.PlayerUpgradesEntity;
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
public final class PlayerUpgradesDao_Impl implements PlayerUpgradesDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerUpgradesEntity> __insertionAdapterOfPlayerUpgradesEntity;

  public PlayerUpgradesDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerUpgradesEntity = new EntityInsertionAdapter<PlayerUpgradesEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `player_upgrades` (`id`,`ballDamageLevel`,`startingBallsLevel`,`coinMultiplierLevel`,`criticalHitLevel`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerUpgradesEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBallDamageLevel());
        statement.bindLong(3, entity.getStartingBallsLevel());
        statement.bindLong(4, entity.getCoinMultiplierLevel());
        statement.bindLong(5, entity.getCriticalHitLevel());
      }
    };
  }

  @Override
  public Object upsert(final PlayerUpgradesEntity upgrades,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlayerUpgradesEntity.insert(upgrades);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<PlayerUpgradesEntity> observe() {
    final String _sql = "SELECT * FROM player_upgrades WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"player_upgrades"}, new Callable<PlayerUpgradesEntity>() {
      @Override
      @Nullable
      public PlayerUpgradesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBallDamageLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "ballDamageLevel");
          final int _cursorIndexOfStartingBallsLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "startingBallsLevel");
          final int _cursorIndexOfCoinMultiplierLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "coinMultiplierLevel");
          final int _cursorIndexOfCriticalHitLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "criticalHitLevel");
          final PlayerUpgradesEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpBallDamageLevel;
            _tmpBallDamageLevel = _cursor.getInt(_cursorIndexOfBallDamageLevel);
            final int _tmpStartingBallsLevel;
            _tmpStartingBallsLevel = _cursor.getInt(_cursorIndexOfStartingBallsLevel);
            final int _tmpCoinMultiplierLevel;
            _tmpCoinMultiplierLevel = _cursor.getInt(_cursorIndexOfCoinMultiplierLevel);
            final int _tmpCriticalHitLevel;
            _tmpCriticalHitLevel = _cursor.getInt(_cursorIndexOfCriticalHitLevel);
            _result = new PlayerUpgradesEntity(_tmpId,_tmpBallDamageLevel,_tmpStartingBallsLevel,_tmpCoinMultiplierLevel,_tmpCriticalHitLevel);
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

  @Override
  public Object get(final Continuation<? super PlayerUpgradesEntity> $completion) {
    final String _sql = "SELECT * FROM player_upgrades WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlayerUpgradesEntity>() {
      @Override
      @Nullable
      public PlayerUpgradesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBallDamageLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "ballDamageLevel");
          final int _cursorIndexOfStartingBallsLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "startingBallsLevel");
          final int _cursorIndexOfCoinMultiplierLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "coinMultiplierLevel");
          final int _cursorIndexOfCriticalHitLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "criticalHitLevel");
          final PlayerUpgradesEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpBallDamageLevel;
            _tmpBallDamageLevel = _cursor.getInt(_cursorIndexOfBallDamageLevel);
            final int _tmpStartingBallsLevel;
            _tmpStartingBallsLevel = _cursor.getInt(_cursorIndexOfStartingBallsLevel);
            final int _tmpCoinMultiplierLevel;
            _tmpCoinMultiplierLevel = _cursor.getInt(_cursorIndexOfCoinMultiplierLevel);
            final int _tmpCriticalHitLevel;
            _tmpCriticalHitLevel = _cursor.getInt(_cursorIndexOfCriticalHitLevel);
            _result = new PlayerUpgradesEntity(_tmpId,_tmpBallDamageLevel,_tmpStartingBallsLevel,_tmpCoinMultiplierLevel,_tmpCriticalHitLevel);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
