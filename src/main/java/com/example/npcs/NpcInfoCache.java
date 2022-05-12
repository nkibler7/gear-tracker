package com.example.npcs;

import com.example.GearTrackerExecutor;
import com.github.nkibler7.osrswikiscraper.NpcInfo;
import com.github.nkibler7.osrswikiscraper.NpcInfos;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.IOException;
import java.util.HashMap;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class NpcInfoCache {

  private final ListeningExecutorService executorService;
  private final NpcInfoLoader npcInfoLoader;

  private final HashMap<Integer, NpcInfo> npcInfos = new HashMap<>();

  @Inject
  NpcInfoCache(
      @GearTrackerExecutor ListeningExecutorService executorService, NpcInfoLoader npcInfoLoader) {
    this.executorService = executorService;
    this.npcInfoLoader = npcInfoLoader;
  }

  @Nullable
  public NpcInfo getNpcInfo(int id) {
    return npcInfos.get(id);
  }

  public void fillCache() {
    Futures.addCallback(
        npcInfoLoader.getNpcInfos(),
        new FutureCallback<NpcInfos>() {
          @Override
          public void onSuccess(@Nullable NpcInfos result) {
            if (result == null) {
              log.warn("NpcInfos result was null, so cache cannot be filled.");
              return;
            }

            result
                .getNpcsList()
                .forEach(npcInfo -> npcInfo.getIdsList().forEach(id -> npcInfos.put(id, npcInfo)));
            log.info("Cache filled with {} entries.", npcInfos.size());
          }

          @Override
          public void onFailure(Throwable t) {
            log.warn("Exception occurred while loading NpcInfos.", t);
          }
        },
        executorService);
  }

  public void shutDown() throws IOException {
    npcInfoLoader.closeConnection();
  }
}
