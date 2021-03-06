/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.batch.extensions.apache.common.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.jeasy.batch.core.field.BeanFieldExtractor;
import org.jeasy.batch.core.field.FieldExtractor;
import org.jeasy.batch.core.marshaller.RecordMarshaller;
import org.jeasy.batch.core.record.Record;
import org.jeasy.batch.core.record.StringRecord;
import org.jeasy.batch.core.util.Utils;

import java.io.StringWriter;

/**
 * Marshals a POJO to CSV format using <a href="http://commons.apache.org/proper/commons-csv/">Apache Common CSV</a>.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class ApacheCommonCsvRecordMarshaller<P> implements RecordMarshaller<Record<P>, StringRecord> {

    public static final char DEFAULT_DELIMITER = ',';
    public static final char DEFAULT_QUALIFIER = '\"';

    private final FieldExtractor<P> fieldExtractor;
    private CSVFormat csvFormat;

    /**
     * Create a new {@link ApacheCommonCsvRecordMarshaller}.
     *
     * @param type   object to marshal
     * @param fields to marshal in order
     */
    public ApacheCommonCsvRecordMarshaller(final Class<P> type, final String... fields) {
        this(new BeanFieldExtractor<>(type, fields));
    }

    /**
     * Create a new {@link ApacheCommonCsvRecordMarshaller}.
     *
     * @param fieldExtractor the field extractor
     */
    public ApacheCommonCsvRecordMarshaller(FieldExtractor<P> fieldExtractor) {
        Utils.checkNotNull(fieldExtractor, "field extractor");
        this.fieldExtractor = fieldExtractor;
        this.csvFormat = CSVFormat.newFormat(DEFAULT_DELIMITER)
                .withQuote(DEFAULT_QUALIFIER)
                .withQuoteMode(QuoteMode.ALL)
                .withRecordSeparator(null);// recordSeparator is forced to null to avoid CSVPrinter to print new lines. New lines are written later by EasyBatch RecordWriter
    }

    @Override
    public StringRecord processRecord(final Record<P> record) throws Exception {
        StringWriter stringWriter = new StringWriter();
        CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat);
        Iterable<Object> iterable = fieldExtractor.extractFields(record.getPayload());
        csvPrinter.printRecord(iterable);
        csvPrinter.flush();
        csvPrinter.close();
        return new StringRecord(record.getHeader(), stringWriter.toString());
    }

    /**
     * Set the delimiter.
     *
     * @param delimiter to use
     */
    public void setDelimiter(char delimiter) {
        this.csvFormat = this.csvFormat.withDelimiter(delimiter);
    }

    /**
     * Set the qualifier.
     *
     * @param qualifier to use
     */
    public void setQualifier(char qualifier) {
        this.csvFormat = this.csvFormat.withQuote(qualifier);
    }

}
