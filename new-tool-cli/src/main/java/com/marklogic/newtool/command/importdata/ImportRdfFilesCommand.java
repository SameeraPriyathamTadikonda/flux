package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.CompressionType;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;
import java.util.function.Supplier;

@Parameters(commandDescription = "Read RDF data from local, HDFS, and S3 files and write the data as managed triples documents in MarkLogic.")
public class ImportRdfFilesCommand extends AbstractImportFilesCommand {

    @ParametersDelegate
    private ReadRdfFilesParams readParams = new ReadRdfFilesParams();

    @ParametersDelegate
    private WriteTriplesDocumentsParams writeDocumentParams = new WriteTriplesDocumentsParams();

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected Supplier<Map<String, String>> getWriteParams() {
        return writeDocumentParams;
    }

    public static class ReadRdfFilesParams extends ReadFilesParams {

        @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
        private CompressionType compression;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_FILES_TYPE, "rdf",
                Options.READ_FILES_COMPRESSION, compression != null ? compression.name() : null
            );
        }
    }

    public static class WriteTriplesDocumentsParams extends WriteDocumentParams {

        @Parameter(names = "--graph", description = "Specify the graph URI for each triple not already associated with a graph. If not set, " +
            "triples will be added to the default MarkLogic graph - http://marklogic.com/semantics#default-graph . ")
        private String graph;

        @Parameter(names = "--graphOverride", description = "Specify the graph URI for each triple to be included in, " +
            "even if is already associated with a graph.")
        private String graphOverride;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.WRITE_GRAPH, graph,
                Options.WRITE_GRAPH_OVERRIDE, graphOverride
            );
        }
    }
}
