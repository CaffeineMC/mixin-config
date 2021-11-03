# CaffeineConfig

CaffeineConfig is a mixin configuration manager that allows both the user and other mods to configure what mixins should
apply and which shouldn't in a simple manner. It is used in the [Sodium](https://github.com/CaffeineMC/sodium-fabric) and
[Lithium](https://github.com/CaffeineMC/lithium-fabric) mods.

## Usage

### Adding CaffeineConfig as a dependency to your project

For this you'll need to first add the CaffeineMC maven to the repositories block of your `build.gradle` file, and then add
CaffeineConfig as an dependency. You should include CaffeineConfig as Jar-in-Jar with your mod.

//TODO this better

### Extending AbstractCaffeineConfigMixinPlugin

In order to use the core functionality, you'll need a class that extends `AbstractCaffeineConfigMixinPlugin`,
where you'll need to implement the `createConfig()`, `logger()` and `mixinPackageRoot()` methods.

The mixin package root is the deepest common package between all mixins. For example, if the mod has mixins in `org.example.mod.mixin.feature`
and `org.example.mod.mixin.bugfixes`, the package root would be `org.example.mod.mixin`.

The mixin package root can be skipped when when defining options for the `CaffeineConfig` object.

Note that while `createConfig()` will only be called once, the `logger()` and `mixinPackageRoot()` methods will be called each time
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
    protected Logger logger() {
        return LOGGER;
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

Once you're ready to buld your `CaffeineConfig`, you'll need to pass the `Path` to the config file to create or read in the `build()` method. 
Doing that will already populate option overrides.

An example for creating a `CaffeineConfig` instance for use in a class extending `AbstractCaffeineConfigMixinPlugin` is the following:

```java
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