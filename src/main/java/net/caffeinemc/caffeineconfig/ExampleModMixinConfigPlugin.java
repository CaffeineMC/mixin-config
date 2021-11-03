package net.caffeinemc.caffeineconfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;

public class ExampleModMixinConfigPlugin extends AbstractCaffeineConfigMixinPlugin {
    private static final Logger LOGGER = LogManager.getLogger("ExampleMod");

    @Override
    protected CaffeineConfig createConfig() {
        return CaffeineConfig.builder("ExampleMod")
                .addMixinOption("ai", true)
                .addMixinOption("ai.brain", true)
                .addMixinOption("ai.goal", true)
                .addMixinOption("block.hopper", true)
                .addOptionDependency("block.hopper", "ai", true)
                .withInfoUrl("https://example.org")
                .build(FabricLoader.getInstance().getConfigDir().resolve("examplemod.properties"));
    }
    
    @Override
    protected Logger logger() {
        return LOGGER;
    }
    
    @Override
    protected String mixinPackageRoot() {
        return "org.example.mod.mixins";
    }
}
