/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.clustering.parsers;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.gbif.clustering.model.ClusteringRelationshipConfig;

@Slf4j
@SuppressWarnings("all")
public class ClusteringService implements Serializable {

  private final Connection connection;
  private final ClusteringRelationshipConfig config;
  private final Retry retry;

  private ClusteringService(Connection connection, ClusteringRelationshipConfig config) {
    this.connection = connection;
    this.config = config;
    this.retry =
        Retry.of(
            "clusteringCall",
            RetryConfig.custom()
                .maxAttempts(config.getRetryMaxAttempts())
                .intervalFunction(
                    IntervalFunction.ofExponentialBackoff(
                        Duration.ofSeconds(config.getRetryDuration())))
                .build());
  }

  public static ClusteringService create(
      Connection connection, ClusteringRelationshipConfig config) {
    return new ClusteringService(connection, config);
  }

  public boolean isClustered(Long gbifId) {

    Supplier<Boolean> fn =
        () -> {
          try (Table table =
              connection.getTable(TableName.valueOf(config.getRelationshipTableName()))) {
            Scan scan = new Scan();
            scan.setBatch(1);
            scan.addFamily(Bytes.toBytes("o"));
            int salt = Math.abs(gbifId.toString().hashCode()) % config.getRelationshipTableSalt();
            scan.setRowPrefixFilter(Bytes.toBytes(salt + ":" + gbifId + ":"));
            ResultScanner s = table.getScanner(scan);
            Result row = s.next();
            return row != null;
          } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
          }
        };

    return retry.executeSupplier(fn);
  }
}
