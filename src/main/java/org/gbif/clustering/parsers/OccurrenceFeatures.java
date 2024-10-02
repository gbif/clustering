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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The API to access the dimensions of an occurrence record necessary for clustering. Defined as an
 * interface to be portable across Spark Rows, Avro objects, POJOs etc.
 */
public interface OccurrenceFeatures {
  String getId();

  String getDatasetKey();

  String getSpeciesKey();

  String getTaxonKey();

  String getBasisOfRecord();

  Double getDecimalLatitude();

  Double getDecimalLongitude();

  Integer getYear();

  Integer getMonth();

  Integer getDay();

  String getEventDate();

  String getScientificName();

  String getCountryCode();

  List<String> getTypeStatus();

  String getOccurrenceID();

  List<String> getRecordedBy();

  String getFieldNumber();

  String getRecordNumber();

  String getCatalogNumber();

  List<String> getOtherCatalogNumbers();

  String getInstitutionCode();

  String getCollectionCode();

  default List<String> listIdentifiers() {
    Stream<String> ids =
        Stream.of(
            getOccurrenceID(),
            getFieldNumber(),
            getRecordNumber(),
            getCatalogNumber(),
            getTripleIdentifier(),
            getScopedIdentifier());

    if (getOtherCatalogNumbers() != null) {
      ids = Stream.concat(ids, getOtherCatalogNumbers().stream());
    }

    return ids.filter(Objects::nonNull).collect(Collectors.toList());
  }

  /**
   * @return a triplet identifier of standard form ic:cc:cn when all triplets are present
   */
  default String getTripleIdentifier() {
    String[] codes = {getInstitutionCode(), getCollectionCode(), getCatalogNumber()};
    if (Arrays.stream(codes).noneMatch(Objects::isNull)) {
      return String.join(":", codes);
    } else {
      return null;
    }
  }

  /**
   * @return an identifier of form ic:cn when both are present
   */
  default String getScopedIdentifier() {
    String[] codes = {getInstitutionCode(), getCatalogNumber()};
    if (!Arrays.stream(codes).anyMatch(Objects::isNull)) {
      return String.join(":", codes);
    } else {
      return null;
    }
  }

  /**
   * Allows implementations to declare that the record originates from a sequence repository.
   * Default behaviour is false, meaning that consumers may receive false negatives. This hook was
   * introduced to allow a relaxation of the rules to accommodate the sparse metadata seen in
   * repositories like NCBI.
   *
   * @see <a href="https://github.com/gbif/pipelines/issues/733">Pipelines issue 733</a>
   */
  default boolean isFromSequenceRepository() {
    return false;
  }
}
