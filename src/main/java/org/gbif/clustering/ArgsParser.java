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

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/** Utility to read the named arguments. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ArgsParser {

  static Cluster.ClusterBuilder parse(String[] args) {
    if (args.length == 1) {
      String filePath = args[0];
      if (filePath == null || !filePath.endsWith(".properties")) {
        throw new IllegalArgumentException("A properties file is required");
      }
      return parseProperties(filePath);
    }

    if (args.length != 16) {
      String provided = args.length == 0 ? "no args" : String.join(" ", args);
      throw new IllegalArgumentException("Incorrect configuration provided: " + provided);
    }

    Cluster.ClusterBuilder builder =
        nextCliOption(new Cluster.ClusterBuilder(), Arrays.asList(args));
    log.info("Clustering started with configuration: {}", builder);
    return builder;
  }

  private static Cluster.ClusterBuilder nextCliOption(
      Cluster.ClusterBuilder builder, List<String> list) {
    if (list.isEmpty()) return builder;

    String option = list.get(0);
    List<String> tail = list.subList(1, list.size());
    if (option.startsWith("-")) {
      switch (option) {
        case "--hive-db":
          return nextCliOption(builder.hiveDB(tail.get(0)), tail.subList(1, tail.size()));
        case "--source-table":
          return nextCliOption(builder.sourceTable(tail.get(0)), tail.subList(1, tail.size()));
        case "--hive-table-prefix":
          return nextCliOption(builder.hiveTablePrefix(tail.get(0)), tail.subList(1, tail.size()));
        case "--hbase-table":
          return nextCliOption(builder.hbaseTable(tail.get(0)), tail.subList(1, tail.size()));
        case "--hbase-regions":
          return nextCliOption(
              builder.hbaseRegions(Integer.parseInt(tail.get(0))), tail.subList(1, tail.size()));
        case "--hbase-zk":
          return nextCliOption(builder.hbaseZK(tail.get(0)), tail.subList(1, tail.size()));
        case "--target-dir":
          return nextCliOption(builder.targetDir(tail.get(0)), tail.subList(1, tail.size()));
        case "--hash-count-threshold":
          return nextCliOption(
              builder.hashCountThreshold(Integer.parseInt(tail.get(0))),
              tail.subList(1, tail.size()));
        default:
          throw new IllegalArgumentException("Unknown option " + option);
      }
    } else {
      throw new IllegalArgumentException("Unknown option " + option);
    }
  }

  @SneakyThrows
  private static Cluster.ClusterBuilder parseProperties(String filepath) {
    File pf = new File(filepath);
    if (!pf.exists()) {
      throw new IllegalArgumentException("Cannot find properties file " + filepath);
    }

    Properties properties = new Properties();
    try (FileReader reader = new FileReader(pf)) {
      properties.load(reader);
    }

    Cluster.ClusterBuilder builder =
        new Cluster.ClusterBuilder()
            .hiveDB(properties.getProperty("hiveDB"))
            .sourceTable(properties.getProperty("sourceTable"))
            .hiveTablePrefix(properties.getProperty("hiveTablePrefix"))
            .hbaseTable(properties.getProperty("hbaseTable"))
            .hbaseRegions(Integer.parseInt(properties.getProperty("hbaseRegions")))
            .hbaseZK(properties.getProperty("hbaseZK"))
            .targetDir(properties.getProperty("targetDir"))
            .hashCountThreshold(Integer.parseInt(properties.getProperty("hashCountThreshold")));

    log.info("Clustering started with configuration loaded from properties file: {}", builder);
    return builder;
  }
}
