package com.searchcentric.tool.gis.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kadme.rest.webtool.tool.AbstractCommand;
import com.kadme.rest.webtool.tool.CommandProperties;
import com.kadme.rest.webtool.tool.ExecutionContext;
import com.kadme.tool.log.Reporter;
import com.searchcentric.tool.gis.Tool;
import com.searchcentric.tool.gis.core.builder.Builder;
import com.searchcentric.tool.gis.core.extractor.Extractor;
import com.searchcentric.tool.gis.model.config.InputConfig;
import com.searchcentric.tool.gis.model.data.ExtractedData;
import com.searchcentric.tool.gis.util.ConnectionFactory;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by undwood on 10.08.16.
 */
public class LoadCommand extends AbstractCommand<Tool> {
    public LoadCommand(Tool tool) {
        super(tool);
    }

    @Override
    protected void execute(CommandProperties cp, Reporter reporter, ExecutionContext executionContext) throws Exception {
        Set<String> loadSet = new HashSet<String>();
        for(String gisProp : cp.getProperties().keySet()) {
            if(gisProp.startsWith("gis.loader.")) {
                loadSet.add(gisProp);
            }
        }
        for(String loadConfProp : loadSet) {
            String configPath = cp.get(loadConfProp, String.class);
            ObjectMapper mapper = new ObjectMapper();
            InputConfig config = mapper.readValue(new File(configPath), InputConfig.class);
            config.source.destinationConfig = config.destination;
            if(config != null && config.destination != null && StringUtils.isNotBlank(config.destination.type)) {
                Extractor ext = (Extractor) Class.forName("com.searchcentric.tool.gis.core.extractor." + config.destination.type + "Extractor").newInstance();
                    if(ext != null) {
                        ExtractedData extData = ext.extract(config.source, reporter);
//                        Builder builder= (Builder) Class.forName("com.searchcentric.tool.gis.core.builder." + config.destination.type + "Builder").newInstance();
//                        builder.build(config.destination, extData, reporter);
                    }
            } else {
                reporter.reportError("Invalid destination configuration. No Type!");
                System.exit(1);
            }

        }

    }

    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getDescription() {
        return "PostGIS loader. Load GIS data to specified PostGIS database from provided sql.";
    }
}
