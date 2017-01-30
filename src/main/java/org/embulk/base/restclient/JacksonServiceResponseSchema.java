package org.embulk.base.restclient;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

import org.embulk.spi.Column;
import org.embulk.spi.Exec;
import org.embulk.spi.json.JsonParser;
import org.embulk.spi.time.TimestampParser;
import org.embulk.spi.type.Type;
import org.embulk.spi.type.Types;

import org.embulk.base.restclient.record.JacksonFlatValueLocator;
import org.embulk.base.restclient.record.JacksonValueLocator;
import org.embulk.base.restclient.writer.BooleanColumnWriter;
import org.embulk.base.restclient.writer.ColumnWriter;
import org.embulk.base.restclient.writer.DoubleColumnWriter;
import org.embulk.base.restclient.writer.JsonColumnWriter;
import org.embulk.base.restclient.writer.LongColumnWriter;
import org.embulk.base.restclient.writer.StringColumnWriter;
import org.embulk.base.restclient.writer.TimestampColumnWriter;
import org.embulk.base.restclient.writer.SchemaWriter;

/**
 * |JacksonServiceResponseSchema| represents how to locate values in a JSON-based response,
 * and how the values are mapped into Embulk schema.
 */
public final class JacksonServiceResponseSchema
        extends ServiceResponseSchema<JacksonValueLocator>
{
    /**
     * Initializes |JacksonServiceSchema|.
     *
     * |JacksonServiceSchema| must be created through |JacksonServiceSchema.Builder|.
     * This constructor is private to guarantee the restriction.
     */
    protected JacksonServiceResponseSchema(
            ListMultimap<Column, ColumnOptions<JacksonValueLocator>> map)
    {
        super(map);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    @Override
    public SchemaWriter<JacksonValueLocator> createSchemaWriter()
    {
        ImmutableList.Builder<ColumnWriter<JacksonValueLocator>> listBuilder = ImmutableList.builder();
        for (Map.Entry<Column, ColumnOptions<JacksonValueLocator>> entry : entries()) {
            listBuilder.add(createColumnWriter(entry.getKey(), entry.getValue()));
        }
        return new SchemaWriter<JacksonValueLocator>(listBuilder.build());
    }

    public static final class Builder
    {
        private Builder()
        {
            this.mapBuilder = ImmutableListMultimap.builder();
            this.index = 0;
        }

        public synchronized JacksonServiceResponseSchema.Builder add(
                String embulkColumnName,
                Type embulkColumnType)
        {
            mapBuilder.put(new Column(index++, embulkColumnName, embulkColumnType),
                           new ColumnOptions<JacksonValueLocator>(
                               new JacksonFlatValueLocator(embulkColumnName)));
            return this;
        }

        public synchronized JacksonServiceResponseSchema.Builder add(
                String embulkColumnName,
                Type embulkColumnType,
                String embulkColumnTimestampFormat)
        {
            mapBuilder.put(new Column(index++, embulkColumnName, embulkColumnType),
                           new ColumnOptions<JacksonValueLocator>(
                               new JacksonFlatValueLocator(embulkColumnName),
                               embulkColumnTimestampFormat));
            return this;
        }

        public synchronized JacksonServiceResponseSchema.Builder add(
                JacksonValueLocator valueLocator,
                String embulkColumnName,
                Type embulkColumnType)
        {
            mapBuilder.put(new Column(index++, embulkColumnName, embulkColumnType),
                           new ColumnOptions<JacksonValueLocator>(
                               valueLocator));
            return this;
        }

        public synchronized JacksonServiceResponseSchema.Builder add(
                JacksonValueLocator valueLocator,
                String embulkColumnName,
                Type embulkColumnType,
                String embulkColumnTimestampFormat)
        {
            mapBuilder.put(new Column(index++, embulkColumnName, embulkColumnType),
                           new ColumnOptions<JacksonValueLocator>(
                               valueLocator,
                               embulkColumnTimestampFormat));
            return this;
        }

        public JacksonServiceResponseSchema build()
        {
            return new JacksonServiceResponseSchema(mapBuilder.build());
        }

        private final ImmutableListMultimap.Builder<Column, ColumnOptions<JacksonValueLocator>> mapBuilder;
        private int index;
    }

    private ColumnWriter<JacksonValueLocator> createColumnWriter(Column column,
                                                                 ColumnOptions<JacksonValueLocator> columnOptions)
    {
        Type type = column.getType();
        JacksonValueLocator locator = columnOptions.getValueLocator();
        Optional<String> timestampFormat = columnOptions.getTimestampFormat();
        if (type.equals(Types.BOOLEAN)) {
            return new BooleanColumnWriter<JacksonValueLocator>(column, locator);
        }
        else if (type.equals(Types.DOUBLE)) {
            return new DoubleColumnWriter<JacksonValueLocator>(column, locator);
        }
        else if (type.equals(Types.LONG)) {
            return new LongColumnWriter<JacksonValueLocator>(column, locator);
        }
        else if (type.equals(Types.STRING)) {
            return new StringColumnWriter<JacksonValueLocator>(column, locator);
        }
        else if (type.equals(Types.TIMESTAMP)) {
            TimestampParser timestampParser = new TimestampParser(
                Exec.newConfigSource().loadConfig(TimestampParser.Task.class),
                (timestampFormat.isPresent()
                 ? Exec.newConfigSource().set("format", timestampFormat.get())
                 : Exec.newConfigSource()).loadConfig(TimestampParser.TimestampColumnOption.class));
            return new TimestampColumnWriter<JacksonValueLocator>(column, locator, timestampParser);
        }
        else if (type.equals(Types.JSON)) {
            JsonParser jsonParser = new JsonParser();
            return new JsonColumnWriter<JacksonValueLocator>(column, locator, jsonParser);
        }
        else {
            throw new IllegalStateException();
        }
    }
}
