package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCommand implements Command {

    protected static final String MARKLOGIC_CONNECTOR = "marklogic";

    protected final Logger logger = LoggerFactory.getLogger("com.marklogic.newtool");

    @ParametersDelegate
    private CommonParams commonParams = new CommonParams();

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @DynamicParameter(
        names = "-C",
        description = "Specify any key and value to be added to the Spark runtime configuration; e.g. -Cspark.logConf=true."
    )
    private Map<String, String> configParams = new HashMap<>();

    @Override
    public final Optional<Preview> execute(SparkSession session) {
        configParams.entrySet().stream().forEach(entry -> session.conf().set(entry.getKey(), entry.getValue()));

        String host = getConnectionParams().getSelectedHost();
        if (host != null && logger.isInfoEnabled()) {
            logger.info("Will connect to MarkLogic host: {}", host);
        }

        long start = System.currentTimeMillis();

        DataFrameReader reader = session.read();
        Dataset<Row> dataset = loadDataset(session, reader);

        dataset = commonParams.applyParams(dataset);
        if (commonParams.isPreviewRequested()) {
            return Optional.of(commonParams.makePreview(dataset));
        }

        DataFrameWriter<Row> writer = dataset.write();
        applyWriter(session, writer);
        if (logger.isInfoEnabled()) {
            logger.info("Execution time: {}s", (System.currentTimeMillis() - start) / 1000);
        }

        return Optional.empty();
    }

    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader);

    protected abstract void applyWriter(SparkSession session, DataFrameWriter<Row> writer);

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }

    public CommonParams getCommonParams() {
        return commonParams;
    }
}
