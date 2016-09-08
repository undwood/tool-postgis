package com.searchcentric.tool.gis;

import com.kadme.rest.webtool.tool.AbstractTool;
import com.kadme.rest.webtool.tool.CommandLineToolHelper;
import com.searchcentric.tool.gis.command.LoadCommand;

/**
 * Created by undwood on 10.08.16.
 */
public class Tool extends AbstractTool {

    @Override
    protected void defineCommands() {
        addCommand(new LoadCommand(this));

    }

    @Override
    public String getOntologyNamespace() {
        return "EMPTY";
    }

    public static void main(String[] args) {
        CommandLineToolHelper.run(new Tool(), args);
    }
}
