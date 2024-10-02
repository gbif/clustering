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
package org.gbif.clustering;

import lombok.AllArgsConstructor;
import org.apache.spark.Partitioner;
import scala.Tuple2;

/** Partitions by the prefix on the given key extracted from the given key. */
@AllArgsConstructor
class SaltPrefixPartitioner extends Partitioner {
  private final int numPartitions;

  @Override
  public int getPartition(Object key) {
    String k = ((Tuple2<String, String>) key)._1;
    return Integer.parseInt(k.substring(0, k.indexOf(":")));
  }

  @Override
  public int numPartitions() {
    return numPartitions;
  }
}
