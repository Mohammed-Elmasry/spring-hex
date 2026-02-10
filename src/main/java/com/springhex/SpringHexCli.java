package com.springhex;

import com.springhex.command.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "spring-hex",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "CLI tool for generating hexagonal architecture scaffolding for Spring projects",
    subcommands = {
        InitCommand.class,
        MakeMediatorCommand.class,
        MakeCommandCommand.class,
        MakeQueryCommand.class,
        MakeRepositoryCommand.class,
        MakeRequestCommand.class,
        MakeResponseCommand.class,
        MakeControllerCommand.class,
        MakeEntityCommand.class,
        MakeModelCommand.class,
        MakeMapperCommand.class,
        MakeAggregateCommand.class,
        MakeValueObjectCommand.class,
        MakeEventCommand.class,
        MakeModuleCommand.class,
        MakePortCommand.class,
        MakeAdapterCommand.class,
        MakeCrudCommand.class,
        MakeTestCommand.class,
        RunTestCommand.class,
        MakeMigrationCommand.class,
        MigrateCommand.class,
        MigrateRollbackCommand.class,
        MigrateStatusCommand.class,
        MigrateValidateCommand.class,
        MigrateRepairCommand.class,
        MigrateFreshCommand.class
    }
)
public class SpringHexCli implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SpringHexCli())
            .setExecutionStrategy(new CommandLine.RunAll())
            .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
