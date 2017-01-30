package org.embulk.base.restclient.writer;

import org.embulk.spi.Column;
import org.embulk.spi.PageBuilder;

import org.embulk.base.restclient.record.ServiceRecord;
import org.embulk.base.restclient.record.ServiceValue;
import org.embulk.base.restclient.record.ValueLocator;

public abstract class ColumnWriter<T extends ValueLocator>
{
    protected ColumnWriter(Column column, T valueLocator)
    {
        this.column = column;
        this.valueLocator = valueLocator;
    }

    public abstract void writeColumnResponsible(ServiceRecord<T> record, PageBuilder pageBuilderToLoad);

    protected final Column getColumnResponsible()
    {
        return column;
    }

    protected final ServiceValue pickupValueResponsible(ServiceRecord<T> record)
    {
        return record.getValue(valueLocator);
    }

    private final Column column;
    private final T valueLocator;
}
