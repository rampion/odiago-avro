// (c) Copyright 2011 Odiago, Inc.

package org.apache.hadoop.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestAvroSequenceFile {
  // Disable checkstyle for this variable.  It must be public to work with JUnit @Rule.
  // CHECKSTYLE:OFF
  @Rule
  public TemporaryFolder mTempDir = new TemporaryFolder();
  // CHECKSTYLE:ON

  /** Tests that reading and writing avro data works. */
  @Test
  @SuppressWarnings("unchecked")
  public void testReadAvro() throws IOException {
    Path sequenceFilePath = new Path(new File(mTempDir.getRoot(), "output.seq").getPath());

    writeSequenceFile(sequenceFilePath, AvroKey.class, AvroValue.class,
        Schema.create(Schema.Type.STRING), Schema.create(Schema.Type.INT),
        new AvroKey<CharSequence>("one"), new AvroValue<Integer>(1),
        new AvroKey<CharSequence>("two"), new AvroValue<Integer>(2));

    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    AvroSequenceFile.Reader.Options options = new AvroSequenceFile.Reader.Options()
        .withFileSystem(fs)
        .withInputPath(sequenceFilePath)
        .withKeySchema(Schema.create(Schema.Type.STRING))
        .withValueSchema(Schema.create(Schema.Type.INT))
        .withConfiguration(conf);
    SequenceFile.Reader reader = new AvroSequenceFile.Reader(options);

    AvroKey<CharSequence> key = new AvroKey<CharSequence>();
    AvroValue<Integer> value = new AvroValue<Integer>();

    // Read the first record.
    key = (AvroKey<CharSequence>) reader.next(key);
    assertNotNull(key);
    assertEquals("one", key.datum().toString());
    value = (AvroValue<Integer>) reader.getCurrentValue(value);
    assertNotNull(value);
    assertEquals(1, value.datum().intValue());

    // Read the second record.
    key = (AvroKey<CharSequence>) reader.next(key);
    assertNotNull(key);
    assertEquals("two", key.datum().toString());
    value = (AvroValue<Integer>) reader.getCurrentValue(value);
    assertNotNull(value);
    assertEquals(2, value.datum().intValue());

    assertNull("Should be no more records.", reader.next(key));
  }

  /** Tests that reading and writing avro records without a reader schema works. */
  @Test
  @SuppressWarnings("unchecked")
  public void testReadAvroWithoutReaderSchemas() throws IOException {
    Path sequenceFilePath = new Path(new File(mTempDir.getRoot(), "output.seq").getPath());

    writeSequenceFile(sequenceFilePath, AvroKey.class, AvroValue.class,
        Schema.create(Schema.Type.STRING), Schema.create(Schema.Type.INT),
        new AvroKey<CharSequence>("one"), new AvroValue<Integer>(1),
        new AvroKey<CharSequence>("two"), new AvroValue<Integer>(2));

    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    AvroSequenceFile.Reader.Options options = new AvroSequenceFile.Reader.Options()
        .withFileSystem(fs)
        .withInputPath(sequenceFilePath)
        .withConfiguration(conf);
    SequenceFile.Reader reader = new AvroSequenceFile.Reader(options);

    AvroKey<CharSequence> key = new AvroKey<CharSequence>();
    AvroValue<Integer> value = new AvroValue<Integer>();

    // Read the first record.
    key = (AvroKey<CharSequence>) reader.next(key);
    assertNotNull(key);
    assertEquals("one", key.datum().toString());
    value = (AvroValue<Integer>) reader.getCurrentValue(value);
    assertNotNull(value);
    assertEquals(1, value.datum().intValue());

    // Read the second record.
    key = (AvroKey<CharSequence>) reader.next(key);
    assertNotNull(key);
    assertEquals("two", key.datum().toString());
    value = (AvroValue<Integer>) reader.getCurrentValue(value);
    assertNotNull(value);
    assertEquals(2, value.datum().intValue());

    assertNull("Should be no more records.", reader.next(key));
  }

  /** Tests that reading and writing ordinary Writables still works. */
  @Test
  @SuppressWarnings("unchecked")
  public void testReadWritables() throws IOException {
    Path sequenceFilePath = new Path(new File(mTempDir.getRoot(), "output.seq").getPath());

    writeSequenceFile(sequenceFilePath, Text.class, IntWritable.class, null, null,
        new Text("one"), new IntWritable(1),
        new Text("two"), new IntWritable(2));

    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    AvroSequenceFile.Reader.Options options = new AvroSequenceFile.Reader.Options()
        .withFileSystem(fs)
        .withInputPath(sequenceFilePath)
        .withConfiguration(conf);
    SequenceFile.Reader reader = new AvroSequenceFile.Reader(options);

    Text key = new Text();
    IntWritable value = new IntWritable();

    // Read the first record.
    assertTrue(reader.next(key));
    assertEquals("one", key.toString());
    reader.getCurrentValue(value);
    assertNotNull(value);
    assertEquals(1, value.get());

    // Read the second record.
    assertTrue(reader.next(key));
    assertEquals("two", key.toString());
    reader.getCurrentValue(value);
    assertNotNull(value);
    assertEquals(2, value.get());

    assertFalse("Should be no more records.", reader.next(key));
  }

  /**
   * Writes a sequence file of records.
   *
   * @param file The target file path.
   * @param keySchema The schema of the key if using Avro, else null.
   * @param valueSchema The schema of the value if using Avro, else null.
   * @param records <i>key1</i>, <i>value1</i>, <i>key2</i>, <i>value2</i>, ...
   */
  private void writeSequenceFile(Path file, Class<?> keyClass, Class<?> valueClass,
      Schema keySchema, Schema valueSchema, Object... records) throws IOException {
    // Make sure the key/value records have an even size.
    if (0 != records.length % 2) {
      throw new IllegalArgumentException("Expected a value for each key record.");
    }

    // Open a AvroSequenceFile writer.
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    AvroSequenceFile.Writer.Options options = new AvroSequenceFile.Writer.Options()
        .withFileSystem(fs)
        .withConfiguration(conf)
        .withOutputPath(file);
    if (null != keySchema) {
      options.withKeySchema(keySchema);
    } else {
      options.withKeyClass(keyClass);
    }
    if (null != valueSchema) {
      options.withValueSchema(valueSchema);
    } else {
      options.withValueClass(valueClass);
    }
    SequenceFile.Writer writer = new AvroSequenceFile.Writer(options);

    // Write some records.
    for (int i = 0; i < records.length; i += 2) {
      writer.append(records[i], records[i + 1]);
    }

    // Close the file.
    writer.close();
  }
}