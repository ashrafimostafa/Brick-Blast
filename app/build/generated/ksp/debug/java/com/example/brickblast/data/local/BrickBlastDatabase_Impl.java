package com.example.brickblast.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.brickblast.data.local.dao.AchievementDao;
import com.example.brickblast.data.local.dao.AchievementDao_Impl;
import com.example.brickblast.data.local.dao.GameSaveDao;
import com.example.brickblast.data.local.dao.GameSaveDao_Impl;
import com.example.brickblast.data.local.dao.HighScoreDao;
import com.example.brickblast.data.local.dao.HighScoreDao_Impl;
import com.example.brickblast.data.local.dao.PlayerStatsDao;
import com.example.brickblast.data.local.dao.PlayerStatsDao_Impl;
import com.example.brickblast.data.local.dao.PlayerUpgradesDao;
import com.example.brickblast.data.local.dao.PlayerUpgradesDao_Impl;
import com.example.brickblast.data.local.dao.PlayerWalletDao;
import com.example.brickblast.data.local.dao.PlayerWalletDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BrickBlastDatabase_Impl extends BrickBlastDatabase {
  private volatile HighScoreDao _highScoreDao;

  private volatile AchievementDao _achievementDao;

  private volatile PlayerStatsDao _playerStatsDao;

  private volatile PlayerUpgradesDao _playerUpgradesDao;

  private volatile PlayerWalletDao _playerWalletDao;

  private volatile GameSaveDao _gameSaveDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `high_scores` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `score` INTEGER NOT NULL, `round` INTEGER NOT NULL, `mode` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `achievements` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `target` INTEGER NOT NULL, `progress` INTEGER NOT NULL, `unlocked` INTEGER NOT NULL, `unlockedAt` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `player_stats` (`id` INTEGER NOT NULL, `highestRound` INTEGER NOT NULL, `totalBricksDestroyed` INTEGER NOT NULL, `totalBallsLaunched` INTEGER NOT NULL, `totalPlayTimeMs` INTEGER NOT NULL, `totalCoinsEarned` INTEGER NOT NULL, `totalGamesPlayed` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `player_upgrades` (`id` INTEGER NOT NULL, `ballDamageLevel` INTEGER NOT NULL, `startingBallsLevel` INTEGER NOT NULL, `coinMultiplierLevel` INTEGER NOT NULL, `criticalHitLevel` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `player_wallet` (`id` INTEGER NOT NULL, `coins` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `game_save` (`id` INTEGER NOT NULL, `round` INTEGER NOT NULL, `score` INTEGER NOT NULL, `totalBalls` INTEGER NOT NULL, `coinsThisSession` INTEGER NOT NULL, `mode` TEXT NOT NULL, `bricksJson` TEXT NOT NULL, `collectablesJson` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `hasActiveSave` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fe1177be8fc9dc063f1e5efebc61cc82')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `high_scores`");
        db.execSQL("DROP TABLE IF EXISTS `achievements`");
        db.execSQL("DROP TABLE IF EXISTS `player_stats`");
        db.execSQL("DROP TABLE IF EXISTS `player_upgrades`");
        db.execSQL("DROP TABLE IF EXISTS `player_wallet`");
        db.execSQL("DROP TABLE IF EXISTS `game_save`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsHighScores = new HashMap<String, TableInfo.Column>(5);
        _columnsHighScores.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScores.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScores.put("round", new TableInfo.Column("round", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScores.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHighScores.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHighScores = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHighScores = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHighScores = new TableInfo("high_scores", _columnsHighScores, _foreignKeysHighScores, _indicesHighScores);
        final TableInfo _existingHighScores = TableInfo.read(db, "high_scores");
        if (!_infoHighScores.equals(_existingHighScores)) {
          return new RoomOpenHelper.ValidationResult(false, "high_scores(com.example.brickblast.data.local.entity.HighScoreEntity).\n"
                  + " Expected:\n" + _infoHighScores + "\n"
                  + " Found:\n" + _existingHighScores);
        }
        final HashMap<String, TableInfo.Column> _columnsAchievements = new HashMap<String, TableInfo.Column>(7);
        _columnsAchievements.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("target", new TableInfo.Column("target", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("progress", new TableInfo.Column("progress", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("unlocked", new TableInfo.Column("unlocked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievements.put("unlockedAt", new TableInfo.Column("unlockedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAchievements = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAchievements = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAchievements = new TableInfo("achievements", _columnsAchievements, _foreignKeysAchievements, _indicesAchievements);
        final TableInfo _existingAchievements = TableInfo.read(db, "achievements");
        if (!_infoAchievements.equals(_existingAchievements)) {
          return new RoomOpenHelper.ValidationResult(false, "achievements(com.example.brickblast.data.local.entity.AchievementEntity).\n"
                  + " Expected:\n" + _infoAchievements + "\n"
                  + " Found:\n" + _existingAchievements);
        }
        final HashMap<String, TableInfo.Column> _columnsPlayerStats = new HashMap<String, TableInfo.Column>(7);
        _columnsPlayerStats.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("highestRound", new TableInfo.Column("highestRound", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("totalBricksDestroyed", new TableInfo.Column("totalBricksDestroyed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("totalBallsLaunched", new TableInfo.Column("totalBallsLaunched", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("totalPlayTimeMs", new TableInfo.Column("totalPlayTimeMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("totalCoinsEarned", new TableInfo.Column("totalCoinsEarned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("totalGamesPlayed", new TableInfo.Column("totalGamesPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayerStats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayerStats = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayerStats = new TableInfo("player_stats", _columnsPlayerStats, _foreignKeysPlayerStats, _indicesPlayerStats);
        final TableInfo _existingPlayerStats = TableInfo.read(db, "player_stats");
        if (!_infoPlayerStats.equals(_existingPlayerStats)) {
          return new RoomOpenHelper.ValidationResult(false, "player_stats(com.example.brickblast.data.local.entity.PlayerStatsEntity).\n"
                  + " Expected:\n" + _infoPlayerStats + "\n"
                  + " Found:\n" + _existingPlayerStats);
        }
        final HashMap<String, TableInfo.Column> _columnsPlayerUpgrades = new HashMap<String, TableInfo.Column>(5);
        _columnsPlayerUpgrades.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerUpgrades.put("ballDamageLevel", new TableInfo.Column("ballDamageLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerUpgrades.put("startingBallsLevel", new TableInfo.Column("startingBallsLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerUpgrades.put("coinMultiplierLevel", new TableInfo.Column("coinMultiplierLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerUpgrades.put("criticalHitLevel", new TableInfo.Column("criticalHitLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayerUpgrades = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayerUpgrades = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayerUpgrades = new TableInfo("player_upgrades", _columnsPlayerUpgrades, _foreignKeysPlayerUpgrades, _indicesPlayerUpgrades);
        final TableInfo _existingPlayerUpgrades = TableInfo.read(db, "player_upgrades");
        if (!_infoPlayerUpgrades.equals(_existingPlayerUpgrades)) {
          return new RoomOpenHelper.ValidationResult(false, "player_upgrades(com.example.brickblast.data.local.entity.PlayerUpgradesEntity).\n"
                  + " Expected:\n" + _infoPlayerUpgrades + "\n"
                  + " Found:\n" + _existingPlayerUpgrades);
        }
        final HashMap<String, TableInfo.Column> _columnsPlayerWallet = new HashMap<String, TableInfo.Column>(2);
        _columnsPlayerWallet.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerWallet.put("coins", new TableInfo.Column("coins", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayerWallet = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayerWallet = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayerWallet = new TableInfo("player_wallet", _columnsPlayerWallet, _foreignKeysPlayerWallet, _indicesPlayerWallet);
        final TableInfo _existingPlayerWallet = TableInfo.read(db, "player_wallet");
        if (!_infoPlayerWallet.equals(_existingPlayerWallet)) {
          return new RoomOpenHelper.ValidationResult(false, "player_wallet(com.example.brickblast.data.local.entity.PlayerWalletEntity).\n"
                  + " Expected:\n" + _infoPlayerWallet + "\n"
                  + " Found:\n" + _existingPlayerWallet);
        }
        final HashMap<String, TableInfo.Column> _columnsGameSave = new HashMap<String, TableInfo.Column>(10);
        _columnsGameSave.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("round", new TableInfo.Column("round", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("totalBalls", new TableInfo.Column("totalBalls", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("coinsThisSession", new TableInfo.Column("coinsThisSession", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("bricksJson", new TableInfo.Column("bricksJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("collectablesJson", new TableInfo.Column("collectablesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSave.put("hasActiveSave", new TableInfo.Column("hasActiveSave", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGameSave = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGameSave = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGameSave = new TableInfo("game_save", _columnsGameSave, _foreignKeysGameSave, _indicesGameSave);
        final TableInfo _existingGameSave = TableInfo.read(db, "game_save");
        if (!_infoGameSave.equals(_existingGameSave)) {
          return new RoomOpenHelper.ValidationResult(false, "game_save(com.example.brickblast.data.local.entity.GameSaveEntity).\n"
                  + " Expected:\n" + _infoGameSave + "\n"
                  + " Found:\n" + _existingGameSave);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "fe1177be8fc9dc063f1e5efebc61cc82", "a4fe1b948bd244942fc28f8f05f4515b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "high_scores","achievements","player_stats","player_upgrades","player_wallet","game_save");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `high_scores`");
      _db.execSQL("DELETE FROM `achievements`");
      _db.execSQL("DELETE FROM `player_stats`");
      _db.execSQL("DELETE FROM `player_upgrades`");
      _db.execSQL("DELETE FROM `player_wallet`");
      _db.execSQL("DELETE FROM `game_save`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(HighScoreDao.class, HighScoreDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AchievementDao.class, AchievementDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlayerStatsDao.class, PlayerStatsDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlayerUpgradesDao.class, PlayerUpgradesDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlayerWalletDao.class, PlayerWalletDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GameSaveDao.class, GameSaveDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public HighScoreDao highScoreDao() {
    if (_highScoreDao != null) {
      return _highScoreDao;
    } else {
      synchronized(this) {
        if(_highScoreDao == null) {
          _highScoreDao = new HighScoreDao_Impl(this);
        }
        return _highScoreDao;
      }
    }
  }

  @Override
  public AchievementDao achievementDao() {
    if (_achievementDao != null) {
      return _achievementDao;
    } else {
      synchronized(this) {
        if(_achievementDao == null) {
          _achievementDao = new AchievementDao_Impl(this);
        }
        return _achievementDao;
      }
    }
  }

  @Override
  public PlayerStatsDao playerStatsDao() {
    if (_playerStatsDao != null) {
      return _playerStatsDao;
    } else {
      synchronized(this) {
        if(_playerStatsDao == null) {
          _playerStatsDao = new PlayerStatsDao_Impl(this);
        }
        return _playerStatsDao;
      }
    }
  }

  @Override
  public PlayerUpgradesDao playerUpgradesDao() {
    if (_playerUpgradesDao != null) {
      return _playerUpgradesDao;
    } else {
      synchronized(this) {
        if(_playerUpgradesDao == null) {
          _playerUpgradesDao = new PlayerUpgradesDao_Impl(this);
        }
        return _playerUpgradesDao;
      }
    }
  }

  @Override
  public PlayerWalletDao playerWalletDao() {
    if (_playerWalletDao != null) {
      return _playerWalletDao;
    } else {
      synchronized(this) {
        if(_playerWalletDao == null) {
          _playerWalletDao = new PlayerWalletDao_Impl(this);
        }
        return _playerWalletDao;
      }
    }
  }

  @Override
  public GameSaveDao gameSaveDao() {
    if (_gameSaveDao != null) {
      return _gameSaveDao;
    } else {
      synchronized(this) {
        if(_gameSaveDao == null) {
          _gameSaveDao = new GameSaveDao_Impl(this);
        }
        return _gameSaveDao;
      }
    }
  }
}
