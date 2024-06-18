/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.JsonLinesFilesExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.WriteFilesOptions;
import com.marklogic.flux.api.WriteSparkFilesOptions;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "export-json-lines-files",
    abbreviateSynopsis = true,
    description = "Read rows via Optic from MarkLogic and write them to JSON Lines files on a local filesystem, HDFS, or S3."
)
public class ExportJsonLinesFilesCommand extends AbstractExportRowsToFilesCommand<JsonLinesFilesExporter> implements JsonLinesFilesExporter {

    @CommandLine.ArgGroup(exclusive = false, heading = WRITER_OPTIONS_HEADING)
    private WriteJsonFilesParams writeParams = new WriteJsonFilesParams();

    public static class WriteJsonFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @CommandLine.Option(
            names = "-P",
            description = "Specify any Spark JSON option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-json.html; e.g. -Pcompression=bzip2."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> get() {
            return additionalOptions;
        }

        @Override
        public WriteSparkFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
            return this;
        }
    }

    @Override
    protected WriteStructuredFilesParams<? extends WriteFilesOptions> getWriteFilesParams() {
        return writeParams;
    }

    @Override
    protected String getWriteFormat() {
        return "json";
    }

    @Override
    public JsonLinesFilesExporter from(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public JsonLinesFilesExporter from(String opticQuery) {
        return this.from(options -> options.opticQuery(opticQuery));
    }

    @Override
    public JsonLinesFilesExporter to(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public JsonLinesFilesExporter to(String path) {
        return this.to(options -> options.path(path));
    }
}
