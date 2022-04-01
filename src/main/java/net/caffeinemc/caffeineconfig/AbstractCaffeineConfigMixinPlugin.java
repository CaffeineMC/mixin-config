package net.caffeinemc.caffeineconfig;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public abstract class AbstractCaffeineConfigMixinPlugin implements IMixinConfigPlugin {
    private CaffeineConfig config;

    @Override
    public void onLoad(String mixinPackage) {
        this.config = createConfig();
        logger().info("Loaded configuration file for {}: {} options available, {} override(s) found",
                config.getModName(), this.config.getOptionCount(), this.config.getOptionOverrideCount());
    }

    /**
     * <p>Creates a {@link CaffeineConfig} to be checked against in this mixin plugin</p>
     * <p>This method will only be called once, on mixin plugin load</p>
     */
    protected abstract CaffeineConfig createConfig();

    /**
     * @return The root package where mixins are defined, ending with a dot
     */
    protected abstract String mixinPackageRoot();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(mixinPackageRoot())) {
            throw new IllegalStateException(String.format("Expected mixin '%s' to start with package root '%s'!", mixinClassName, mixinPackageRoot()));
        }

        String mixin = mixinClassName.substring(mixinPackageRoot().length());
        Option option = this.config.getEffectiveOptionForMixin(mixin);

        if (option == null) {
            throw new IllegalStateException(String.format("No options matched mixin '%s'! Mixins in this config must be under a registered option name", mixin));
        }

        if (option.isOverridden()) {
            String source = "[unknown]";

            if (option.isUserDefined()) {
                source = "user configuration";
            } else if (option.isModDefined()) {
                source = "mods [" + String.join(", ", option.getDefiningMods()) + "]";
            }

            if (option.isEnabled()) {
                logger().warn("Force-enabling mixin '{}' as option '{}' (added by {}) enables it", mixin,
                        option.getName(), source);
            } else {
                logger().warn("Force-disabling mixin '{}' as option '{}' (added by {}) disables it and children", mixin,
                        option.getName(), source);
            }
        }

        return option.isEnabled();
    }

    private Logger logger() {
        return config.getLogger();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

}
