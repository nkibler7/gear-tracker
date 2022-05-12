package com.example.npcs;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.asynchttpclient.Dsl.asyncHttpClient;

import com.example.GearTrackerExecutor;
import com.github.nkibler7.osrswikiscraper.NpcInfos;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

@ThreadSafe
final class NpcInfoLoader {

  private static final String NPC_INFOS_BINARYPB =
      "https://raw.githubusercontent.com/nkibler7/osrs-wiki-scraper/main/out/npc_infos.binarypb";

  private final ListeningExecutorService executorService;
  private final AsyncHttpClient client;

  @Nullable
  @GuardedBy("this")
  private ListenableFuture<Response> pendingResponse = null;

  @Inject
  NpcInfoLoader(@GearTrackerExecutor ListeningExecutorService executorService) {
    this.executorService = executorService;
    client = asyncHttpClient();
  }

  public synchronized ListenableFuture<NpcInfos> getNpcInfos() {
    if (pendingResponse == null) {
      pendingResponse =
          JdkFutureAdapters.listenInPoolThread(
              client.prepareGet(NPC_INFOS_BINARYPB).execute(), executorService);
    }

    return Futures.transform(pendingResponse, NpcInfoLoader::parseResponse, directExecutor());
  }

  public synchronized void closeConnection() throws IOException {
    client.close();
  }

  private static NpcInfos parseResponse(Response response) {
    try {
      return NpcInfos.parseFrom(response.getResponseBodyAsByteBuffer());
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
