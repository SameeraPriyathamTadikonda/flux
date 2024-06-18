/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.SaveMode;
import com.marklogic.flux.api.WriteFilesOptions;
import picocli.CommandLine;

/**
 * Structured = reuses a Spark data source, where saveMode can vary.
 */
public abstract class WriteStructuredFilesParams<T extends WriteFilesOptions> extends WriteFilesParams<T> {

    @CommandLine.Option(names = "--mode",
        description = "Specifies how data is written if the path already exists. " +
            "See https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information.")
    private SaveMode saveMode = SaveMode.OVERWRITE;

    protected WriteStructuredFilesParams() {
        // For Avro/Parquet/etc files, writing many rows to a single file is acceptable and expected.
        this.fileCount = 1;
    }

    public SaveMode getSaveMode() {
        return saveMode;
    }
}
