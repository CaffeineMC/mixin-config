# CaffeineConfig

CaffeineConfig is a mixin configuration manager that allows both the user and other mods to configure what mixins should
apply and which shouldn't in a simple manner, and without the mods having to depend on each other. It is used in 
the [Sodium](https://github.com/CaffeineMC/sodium-fabric) and [Lithium](https://github.com/CaffeineMC/lithium-fabric) mods.

## Usage

### Adding CaffeineConfig as a dependency to your project

To use `CaffeineConfig` you'll first need to add the CaffeineMC maven to the repositories block of your `build.gradle` file, and then add
CaffeineConfig as a dependency. You should include CaffeineConfig as Jar-in-Jar with your mod.

Adding the CaffeineMC maven to your `build.gradle`:

```groovy
repositories {
    ...
    // TODO this when I know the url
}
```

Adding CaffeineConfig as a dependency and as a Jar-in-Jar in your mod:

```groovy
dependencies {
    ...
    modImplementation 'net.caffeinemc:CaffeineConfig:1.0.0'
    include 'net.caffeinemc:CaffeineConfig:1.0.0'
}
```

While not strictly necessary, you should also declare the dependency in your `fabric.mod.json`:

```json
{
    ...
    "depends": {
        ...
        "caffeineconfig": ">=1.0.0"
    }

}
```

### Extending AbstractCaffeineConfigMixinPlugin

In order to use the core functionality, you'll need a class that extends `AbstractCaffeineConfigMixinPlugin`,
where you'll need to implement the `createConfig()` and `mixinPackageRoot()` methods.

The mixin package root is the deepest common package between all mixins. For example, if the mod has mixins in `org.example.mod.mixin.feature`
and `org.example.mod.mixin.bugfixes`, the package root would be `org.example.mod.mixin`.

Note that while `createConfig()` will only be called once, the `mixinPackageRoot()` method will be called each time
those are needed.

An example implementation of the `AbstractCaffeineConfigMixinPlugin` is the following:

```java
public class ExampleModMixinConfigPlugin extends AbstractCaffeineConfigMixinPlugin {
    private static final Logger LOGGER = LogManager.getLogger("ExampleMod");

    @Override
    protected CaffeineConfig createConfig() {
        // see next section
    }
    
    @Override
    protected String mixinPackageRoot() {
        return "org.example.mod.mixin";
    }
}

```

### Creating a CaffeineConfig object

In order to create a `CaffeineConfig` object you'll first need to get a `Builder` by using the `CaffeineConfig.builder(modName)`
method. By default, this will get a logger with the mod's name and `Config` appended to it and use it as its logger,
and set the JSON key for other mods to disable settings as `lowercase(modName):options`. You can override those by calling the
`withLogger(Logger)` and `withSettingsKey(String)` methods of the builder.

You can also provide a url to a resource with more information about your config file by calling the `withInfoUrl(String)` method.

Then you have to add your various mixin options, using `addMixinOption(name, default)`. The options don't have to point directly to a
mixin file, but can point to a package instead, and that will make disabling the rule disable all mixins in the package. You have to skip the
mixin package root from before when defining options. You can provide multiple options for a package and subpackages if desired, where disabling the
top packet will disable all children.

If one of the options depends on another option to be enabled/disabled, you can define dependencies between options by calling
`addOptionDependency(optionThatDepends, dependency, requiredValue)`. Doing that, if any of the dependencies of an option is not
the required value, the option will be disabled.

Once you're ready to build your `CaffeineConfig`, you'll need to pass the `Path` to the config file to create or read in the `build()` method. 
Doing that will already populate option overrides.

An example for creating a `CaffeineConfig` instance for use in a class extending `AbstractCaffeineConfigMixinPlugin` is the following:

```java
    @Override
    protected CaffeineConfig createConfig() {
        return CaffeineConfig.builder("ExampleMod")
                .addMixinOption("ai", true) // ai can be disabled and it will disable all subpackages
                .addMixinOption("ai.brain", true)
                .addMixinOption("ai.goal", true)
                .addMixinOption("block.hopper", true)
                .addOptionDependency("block.hopper", "ai", true) // block.hopper will be disabled if ai is disabled
                .withInfoUrl("https://example.org")
                .build(FabricLoader.getInstance().getConfigDir().resolve("examplemod.properties"));
    }

```

### Using your MixinConfigPlugin

In order for mixin to use your class that extends `AbstractCaffeineConfigMixinPlugin`, you'll need to reference it in your
mixins json file by mapping a field named `plugin` to a string being the fully-qualified name of your class.

An example is the following:

```json
{
  "package": "org.example.mod.mixin",
  "required": true,
  "compatibilityLevel": "JAVA_16",
  "plugin": "org.example.mod.mixin.ExampleModMixinConfigPlugin",
  "injectors": {
    "defaultRequire": 1
  },
  "mixins": [
    ...
  ]
}
```

### Notes

Any mixin added to the config (the config being the mixins JSON file) must have a valid option related to it, else the mixin plugin
will throw an `IllegalStateException` when applying it. This is to ensure mixins are added to the config and to prevent hard to
debug problems where mixins silently don't apply because they weren't assigned to any option.

If you want to add mixins that are not configurable, you should add them to a different mixin config, that is, a different mixin json
file that is therefore another entry in your `fabric.mod.json`.
