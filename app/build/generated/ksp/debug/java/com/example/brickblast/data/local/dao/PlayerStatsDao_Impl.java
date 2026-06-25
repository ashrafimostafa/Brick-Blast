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
import com.example.brickblast.data.local.entity.PlayerStatsEntity;
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
public final class PlayerStatsDao_Impl implements PlayerStatsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerStatsEntity> __insertionAdapterOfPlayerStatsEntity;

  public PlayerStatsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerStatsEntity = new EntityInsertionAdapter<PlayerStatsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `player_stats` (`id`,`highestRound`,`totalBricksDestroyed`,`totalBallsLaunched`,`totalPlayTimeMs`,`totalCoinsEarned`,`totalGamesPlayed`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerStatsEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHighestRound());
        statement.bindLong(3, entity.getTotalBricksDestroyed());
        statement.bindLong(4, entity.getTotalBallsLaunched());
        statement.bindLong(5, entity.getTotalPlayTimeMs());
        statement.bindLong(6, entity.getTotalCoinsEarned());
        statement.bindLong(7, entity.getTotalGamesPlayed());
      }
    };
  }

  @Override
  public Object upsert(final PlayerStatsEntity stats,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlayerStatsEntity.insert(stats);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<PlayerStatsEntity> observe() {
    final String _sql = "SELECT * FROM player_stats WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"player_stats"}, new Callable<PlayerStatsEntity>() {
      @Override
      @Nullable
      public PlayerStatsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHighestRound = CursorUtil.getColumnIndexOrThrow(_cursor, "highestRound");
          final int _cursorIndexOfTotalBricksDestroyed = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBricksDestroyed");
          final int _cursorIndexOfTotalBallsLaunched = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBallsLaunched");
          final int _cursorIndexOfTotalPlayTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPlayTimeMs");
          final int _cursorIndexOfTotalCoinsEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCoinsEarned");
          final int _cursorIndexOfTotalGamesPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "totalGamesPlayed");
          final PlayerStatsEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHighestRound;
            _tmpHighestRound = _cursor.getInt(_cursorIndexOfHighestRound);
            final long _tmpTotalBricksDestroyed;
            _tmpTotalBricksDestroyed = _cursor.getLong(_cursorIndexOfTotalBricksDestroyed);
            final long _tmpTotalBallsLaunched;
            _tmpTotalBallsLaunched = _cursor.getLong(_cursorIndexOfTotalBallsLaunched);
            final long _tmpTotalPlayTimeMs;
            _tmpTotalPlayTimeMs = _cursor.getLong(_cursorIndexOfTotalPlayTimeMs);
            final long _tmpTotalCoinsEarned;
            _tmpTotalCoinsEarned = _cursor.getLong(_cursorIndexOfTotalCoinsEarned);
            final int _tmpTotalGamesPlayed;
            _tmpTotalGamesPlayed = _cursor.getInt(_cursorIndexOfTotalGamesPlayed);
            _result = new PlayerStatsEntity(_tmpId,_tmpHighestRound,_tmpTotalBricksDestroyed,_tmpTotalBallsLaunched,_tmpTotalPlayTimeMs,_tmpTotalCoinsEarned,_tmpTotalGamesPlayed);
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
  public Object get(final Continuation<? super PlayerStatsEntity> $completion) {
    final String _sql = "SELECT * FROM player_stats WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlayerStatsEntity>() {
      @Override
      @Nullable
      public PlayerStatsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHighestRound = CursorUtil.getColumnIndexOrThrow(_cursor, "highestRound");
          final int _cursorIndexOfTotalBricksDestroyed = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBricksDestroyed");
          final int _cursorIndexOfTotalBallsLaunched = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBallsLaunched");
          final int _cursorIndexOfTotalPlayTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPlayTimeMs");
          final int _cursorIndexOfTotalCoinsEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCoinsEarned");
          final int _cursorIndexOfTotalGamesPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "totalGamesPlayed");
          final PlayerStatsEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHighestRound;
            _tmpHighestRound = _cursor.getInt(_cursorIndexOfHighestRound);
            final long _tmpTotalBricksDestroyed;
            _tmpTotalBricksDestroyed = _cursor.getLong(_cursorIndexOfTotalBricksDestroyed);
            final long _tmpTotalBallsLaunched;
            _tmpTotalBallsLaunched = _cursor.getLong(_cursorIndexOfTotalBallsLaunched);
            final long _tmpTotalPlayTimeMs;
            _tmpTotalPlayTimeMs = _cursor.getLong(_cursorIndexOfTotalPlayTimeMs);
            final long _tmpTotalCoinsEarned;
            _tmpTotalCoinsEarned = _cursor.getLong(_cursorIndexOfTotalCoinsEarned);
            final int _tmpTotalGamesPlayed;
            _tmpTotalGamesPlayed = _cursor.getInt(_cursorIndexOfTotalGamesPlayed);
            _result = new PlayerStatsEntity(_tmpId,_tmpHighestRound,_tmpTotalBricksDestroyed,_tmpTotalBallsLaunched,_tmpTotalPlayTimeMs,_tmpTotalCoinsEarned,_tmpTotalGamesPlayed);
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
