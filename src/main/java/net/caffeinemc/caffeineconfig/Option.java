package net.caffeinemc.caffeineconfig;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;

/**
 * <p>A config option that can be enabled or disabled.</p>
 * 
 * <p>An option can be toggled by the user setting the name in the config file, 
 * or by a mod by declaring it in their {@code fabric.mod.json} file as a custom
 * value inside a custom value map with this mod's name.</p>
 *
 */
public final class Option {
    private final String name;

    private Object2BooleanLinkedOpenHashMap<Option> dependencies;
    private Set<String> modDefined = null;
    private boolean enabled;
    private boolean userDefined;

    Option(String name, boolean enabled, boolean userDefined) {
        this.name = name;
        this.enabled = enabled;
        this.userDefined = userDefined;
    }

    /**
     * @return The name of this option
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return {@code true} if this option is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @return {@code true} if this option is overridden, {@code false} otherwise
     */
    public boolean isOverridden() {
        return this.isUserDefined() || this.isModDefined();
    }

    /**
     * @return {@code true} if this option's override is user defined, {@code false} otherwise
     */
    public boolean isUserDefined() {
        return this.userDefined;
    }

    /**
     * @return {@code true} if this option's override is mod defined, {@code false} otherwise
     */
    public boolean isModDefined() {
        return this.modDefined != null;
    }

    /**
     * @return An unmodifiable {@link Collection} of the mods that have defined overrides for this option
     */
    public Collection<String> getDefiningMods() {
        return this.modDefined != null ? Collections.unmodifiableCollection(this.modDefined) : Collections.emptyList();
    }

    void setEnabled(boolean enabled, boolean userDefined) {
        this.enabled = enabled;
        this.userDefined = userDefined;
    }

    void addModOverride(boolean enabled, String modId) {
        this.enabled = enabled;

        if (this.modDefined == null) {
            this.modDefined = new LinkedHashSet<>();
        }

        this.modDefined.add(modId);
    }

    void clearModsDefiningValue() {
        this.modDefined = null;
    }

    void addDependency(Option dependencyOption, boolean requiredValue) {
        if (this.dependencies == null) {
            this.dependencies = new Object2BooleanLinkedOpenHashMap<>(1);
        }
        this.dependencies.put(dependencyOption, requiredValue);
    }

    boolean disableIfDependenciesNotMet(Logger logger) {
        if (this.dependencies != null && this.isEnabled()) {
            for (Object2BooleanMap.Entry<Option> dependency : this.dependencies.object2BooleanEntrySet()) {
                Option option = dependency.getKey();
                boolean requiredValue = dependency.getBooleanValue();
                if (option.isEnabled() != requiredValue) {
                    this.enabled = false;
                    logger.warn("Option '{}' requires '{}={}' but found '{}'. Setting '{}={}'.", this.name, option.name, requiredValue, option.isEnabled(), this.name, this.enabled);
                    return true;
                }
            }
        }
        return false;
    }
}
