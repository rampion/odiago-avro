/**
 * Licensed to Odiago, Inc. under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Odiago, Inc.
 * licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.apache.avro.io;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapred.AvroWrapper;

/**
 * Deserializes AvroValue objects within Hadoop.
 *
 * @param <D> The java type of the avro data to deserialize.
 *
 * @see org.apache.avro.io.AvroDeserializer
 */
public class AvroValueDeserializer<D> extends AvroDeserializer<AvroWrapper<D>, D> {
  /**
   * Constructor.
   *
   * @param writerSchema The Avro writer schema for the data to deserialize.
   * @param readerSchema The Avro reader schema for the data to deserialize.
   */
  public AvroValueDeserializer(Schema writerSchema, Schema readerSchema) {
    super(writerSchema, readerSchema);
  }

  /**
   * Creates a new empty <code>AvroValue</code> instance.
   *
   * @return a new empty AvroValue.
   */
  @Override
  protected AvroWrapper<D> createAvroWrapper() {
    return new AvroValue<D>(null);
  }
}
