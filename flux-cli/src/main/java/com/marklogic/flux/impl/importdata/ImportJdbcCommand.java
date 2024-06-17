package com.marklogic.flux.impl.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.JdbcImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.JdbcParams;
import com.marklogic.flux.impl.OptionsUtil;
import org.apache.spark.sql.*;

import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read rows via JDBC and write JSON or XML documents to MarkLogic.")
public class ImportJdbcCommand extends AbstractCommand<JdbcImporter> implements JdbcImporter {

    @ParametersDelegate
    private ReadJdbcParams readParams = new ReadJdbcParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected void validateDuringApiUsage() {
        OptionsUtil.validateRequiredOptions(readParams.makeOptions(),
            "url", "Must specify a JDBC URL",
            "query", "Must specify a query"
        );
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return session.read().format("jdbc")
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        return readParams.aggregationParams.applyGroupBy(dataset);
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }

    @Override
    public JdbcImporter from(Consumer<ReadJdbcOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public JdbcImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    public static class ReadJdbcParams extends JdbcParams<JdbcImporter.ReadJdbcOptions> implements JdbcImporter.ReadJdbcOptions {

        @Parameter(names = "--query", required = true,
            description = "A SQL query used to read data from the JDBC data source.")
        private String query;

        @ParametersDelegate
        private AggregationParams aggregationParams = new AggregationParams();

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                "query", query
            );
        }

        @Override
        public JdbcImporter.ReadJdbcOptions query(String query) {
            this.query = query;
            return this;
        }

        @Override
        public JdbcImporter.ReadJdbcOptions groupBy(String groupBy) {
            this.aggregationParams.setGroupBy(groupBy);
            return this;
        }

        @Override
        public ReadJdbcOptions aggregateColumns(String newColumnName, String... columns) {
            this.aggregationParams.addAggregationExpression(newColumnName, columns);
            return this;
        }
    }
}
