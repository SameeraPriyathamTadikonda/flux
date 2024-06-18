/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.AvroFilesImporter;
import com.marklogic.flux.api.ReadTabularFilesOptions;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-avro-files",
    abbreviateSynopsis = true,
    description = "Read Avro files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-avro.html, with each row being written " +
    "as a JSON or XML document in MarkLogic."
)
public class ImportAvroFilesCommand extends AbstractImportFilesCommand<AvroFilesImporter> implements AvroFilesImporter {

    @CommandLine.ArgGroup(exclusive = false, heading = READER_OPTIONS_HEADING, multiplicity = "1")
    private ReadAvroFilesParams readParams = new ReadAvroFilesParams();

    @CommandLine.ArgGroup(exclusive = false, heading = WRITER_OPTIONS_HEADING)
    private WriteStructuredDocumentParams writeDocumentParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "avro";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeDocumentParams;
    }

    public static class ReadAvroFilesParams extends ReadFilesParams<ReadTabularFilesOptions> implements ReadTabularFilesOptions {

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark Avro data source option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true. " +
                "Spark configuration options must be defined via '-C'."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.ArgGroup(exclusive = false)
        private AggregationParams aggregationParams = new AggregationParams();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public ReadTabularFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
            return this;
        }

        @Override
        public ReadTabularFilesOptions groupBy(String columnName) {
            aggregationParams.setGroupBy(columnName);
            return this;
        }

        @Override
        public ReadTabularFilesOptions aggregateColumns(String newColumnName, String... columns) {
            aggregationParams.addAggregationExpression(newColumnName, columns);
            return this;
        }
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        return readParams.aggregationParams.applyGroupBy(dataset);
    }

    @Override
    public AvroFilesImporter from(Consumer<ReadTabularFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AvroFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public AvroFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeDocumentParams);
        return this;
    }
}
