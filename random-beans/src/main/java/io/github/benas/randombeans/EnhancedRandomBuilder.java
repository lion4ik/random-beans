/**
 * The MIT License
 *
 *   Copyright (c) 2016, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package io.github.benas.randombeans;

import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.benas.randombeans.api.EnhancedRandomParameters;
import io.github.benas.randombeans.api.Randomizer;
import io.github.benas.randombeans.api.RandomizerRegistry;
import io.github.benas.randombeans.randomizers.registry.CustomRandomizerRegistry;
import io.github.benas.randombeans.randomizers.registry.ExclusionRandomizerRegistry;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Supplier;

import static io.github.benas.randombeans.FieldDefinitionBuilder.field;
import static io.github.benas.randombeans.RandomizerProxy.asRandomizer;
import static java.lang.String.format;

/**
 * Builder to create {@link EnhancedRandom} instances.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class EnhancedRandomBuilder {

    private final CustomRandomizerRegistry customRandomizerRegistry;

    private final ExclusionRandomizerRegistry exclusionRandomizerRegistry;

    private final Set<RandomizerRegistry> userRegistries;

    private final EnhancedRandomParameters parameters;

    /**
     * Create a new {@link EnhancedRandomBuilder}.
     */
    public EnhancedRandomBuilder() {
        customRandomizerRegistry = new CustomRandomizerRegistry();
        exclusionRandomizerRegistry = new ExclusionRandomizerRegistry();
        userRegistries = new LinkedHashSet<>();
        parameters = new EnhancedRandomParameters();
    }

    /**
     * Create a new {@link EnhancedRandomBuilder}.
     *
     * @return a new {@link EnhancedRandomBuilder}
     */
    public static EnhancedRandomBuilder aNewEnhancedRandomBuilder() {
        return new EnhancedRandomBuilder();
    }

    /**
     * Create a new {@link EnhancedRandom} instance with default parameters.
     *
     * @return a new {@link EnhancedRandom}
     */
    public static EnhancedRandom aNewEnhancedRandom() {
        return new EnhancedRandomBuilder().build();
    }

    /**
     * Register a custom randomizer for a given field.
     *
     * <strong>The field type MUST be provided in the field definition</strong>
     *
     * @param fieldDefinition definition of the field to randomize
     * @param randomizer      the custom {@link Randomizer} to use
     * @param <T> The target class type
     * @param <F> The target field type
     * @param <R> The type generated by the randomizer
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public <T, F, R> EnhancedRandomBuilder randomize(FieldDefinition<T, F> fieldDefinition, Randomizer<R> randomizer) {
        if (fieldDefinition.getType() == null) {
            throw new IllegalArgumentException(format("Ambiguous field definition: %s." +
                    " Field type is mandatory to register a custom randomizer: %s", fieldDefinition, randomizer));
        }
        customRandomizerRegistry.registerRandomizer(fieldDefinition, randomizer);
        return this;
    }

    /**
     * Register a supplier as randomizer for a given field.
     *
     * <strong>The field type MUST be provided in the field definition</strong>
     *
     * @param fieldDefinition definition of the field to randomize
     * @param supplier        the custom {@link Supplier} to use
     * @param <T> The target class type
     * @param <F> The target field type
     * @param <R> The type generated by the supplier
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public <T, F, R> EnhancedRandomBuilder randomize(FieldDefinition<T, F> fieldDefinition, Supplier<R> supplier) {
        return randomize(fieldDefinition, asRandomizer(supplier));
    }

    /**
     * Register a custom randomizer for a given type.
     *
     * @param type       class of the type to randomize
     * @param randomizer the custom {@link Randomizer} to use
     * @param <T> The field type
     * @param <R> The type generated by the randomizer
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public <T, R> EnhancedRandomBuilder randomize(Class<T> type, Randomizer<R> randomizer) {
        customRandomizerRegistry.registerRandomizer(type, randomizer);
        return this;
    }

    /**
     * Register a supplier as randomizer for a given type.
     *
     * @param type     class of the type to randomize
     * @param supplier the custom {@link Supplier} to use
     * @param <T> The field type
     * @param <R> The type generated by the supplier
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public <T, R> EnhancedRandomBuilder randomize(Class<T> type, Supplier<R> supplier) {
        return randomize(type, asRandomizer(supplier));
    }

    /**
     * Exclude a field from being populated.
     *
     * @param fieldDefinition definition of the field to exclude
     * @param <T> The target class type
     * @param <F> The target field type
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public <T, F> EnhancedRandomBuilder exclude(FieldDefinition<T, F> fieldDefinition) {
        exclusionRandomizerRegistry.addFieldDefinition(fieldDefinition);
        return this;
    }

    /**
     * Exclude types from being populated.
     *
     * @param types the types to exclude
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder exclude(Class<?>... types) {
        for (Class<?> type : types) {
            exclusionRandomizerRegistry.addFieldDefinition(field().ofType(type).get());
        }
        return this;
    }

    /**
     * Set the initial random seed.
     *
     * @param seed the initial seed
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder seed(final long seed) {
        parameters.setSeed(seed);
        return this;
    }

    /**
     * Set the minimum collection size.
     *
     * @param minCollectionSize the minimum collection size
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder minCollectionSize(final int minCollectionSize) {
        if (minCollectionSize < 0) {
            throw new IllegalArgumentException("minCollectionSize must be >= 0");
        }
        parameters.setMinCollectionSize(minCollectionSize);
        return this;
    }

    /**
     * Set the maximum collection size.
     *
     * @param maxCollectionSize the maximum collection size
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder maxCollectionSize(final int maxCollectionSize) {
        parameters.setMaxCollectionSize(maxCollectionSize);
        return this;
    }

    /**
     * Set the maximum string length.
     *
     * @param maxStringLength the maximum string length
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder maxStringLength(final int maxStringLength) {
        parameters.setMaxStringLength(maxStringLength);
        return this;
    }

    /**
     * Set the minimum string length.
     *
     * @param minStringLength the minimum string length
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder minStringLength(final int minStringLength) {
        parameters.setMinStringLength(minStringLength);
        return this;
    }

    /**
     * Set the maximum number of different objects to generate for a type.
     *
     * @param maxObjectPoolSize the maximum number of objects
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder maxObjectPoolSize(final int maxObjectPoolSize) {
        parameters.setMaxObjectPoolSize(maxObjectPoolSize);
        return this;
    }

    /**
     * Set the maximum randomization depth for objects tree.
     *
     * @param maxRandomizationDepth the maximum randomization depth
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder maxRandomizationDepth(final int maxRandomizationDepth) {
        parameters.setMaxRandomizationDepth(maxRandomizationDepth);
        return this;
    }

    /**
     * Set the charset to use for character based fields.
     *
     * @param charset the charset to use
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder charset(final Charset charset) {
        parameters.setCharset(charset);
        return this;
    }

    /**
     * Set the date range.
     *
     * @param min date
     * @param max date
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder dateRange(final LocalDate min, final LocalDate max) {
        parameters.setDateRange(min, max);
        return this;
    }

    /**
     * Set the time range.
     *
     * @param min time
     * @param max time
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder timeRange(final LocalTime min, final LocalTime max) {
        parameters.setTimeRange(min, max);
        return this;
    }

    /**
     * Register a {@link RandomizerRegistry}.
     *
     * @param registry the {@link RandomizerRegistry} to register
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder registerRandomizerRegistry(final RandomizerRegistry registry) {
        userRegistries.add(registry);
        return this;
    }

    /**
     * Should the classpath be scanned for concrete types when a field with an interface or abstract
     * class type is encountered?
     * 
     * Deactivated by default.
     *
     * @param scanClasspathForConcreteTypes whether to scan the classpath or not
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder scanClasspathForConcreteTypes(boolean scanClasspathForConcreteTypes) {
        parameters.setScanClasspathForConcreteTypes(scanClasspathForConcreteTypes);
        return this;
    }

    /**
     * Should default initialization of field values be overridden?
     * E.g. should the values of the {@code strings} and {@code integers} fields below be kept untouched
     *  or should they be randomized.
     * 
     * <pre>
     * {@code
     * public class Bean {
     *     Set<String> strings = new HashSet<>();
     *     List<Integer> integers;
     * 
     *     public Bean() {
     *         integers = Arrays.asList(1, 2, 3);
     *     }
     * }}
     * </pre>
     * 
     * Deactivated by default.
     *
     * @param overrideDefaultInitialization whether to override default initialization of field values or not
     * @return a pre configured {@link EnhancedRandomBuilder} instance
     */
    public EnhancedRandomBuilder overrideDefaultInitialization(boolean overrideDefaultInitialization) {
        parameters.setOverrideDefaultInitialization(overrideDefaultInitialization);
        return this;
    }

    /**
     * Build a {@link EnhancedRandom} instance.
     *
     * @return a configured {@link EnhancedRandom} instance
     */
    public EnhancedRandom build() {
        int minCollectionSize = parameters.getMinCollectionSize();
        int maxCollectionSize = parameters.getMaxCollectionSize();
        if (minCollectionSize > maxCollectionSize) {
            throw new IllegalArgumentException(format("minCollectionSize (%s) must be <= than maxCollectionSize (%s)",
                    minCollectionSize, maxCollectionSize));
        }
        LinkedHashSet<RandomizerRegistry> registries = setupRandomizerRegistries();
        return setupEnhancedRandom(registries);
    }

    private EnhancedRandomImpl setupEnhancedRandom(LinkedHashSet<RandomizerRegistry> registries) {
        EnhancedRandomImpl enhancedRandom = new EnhancedRandomImpl(registries);
        enhancedRandom.setParameters(parameters);
        return enhancedRandom;
    }

    private LinkedHashSet<RandomizerRegistry> setupRandomizerRegistries() {
        LinkedHashSet<RandomizerRegistry> registries = new LinkedHashSet<>();
        registries.add(customRandomizerRegistry);
        registries.add(exclusionRandomizerRegistry);
        registries.addAll(userRegistries);
        registries.addAll(loadRegistries());
        registries.forEach(registry -> registry.init(parameters));
        return registries;
    }

    private Collection<RandomizerRegistry> loadRegistries() {
        List<RandomizerRegistry> registries = new ArrayList<>();
        ServiceLoader.load(RandomizerRegistry.class).forEach(registries::add);
        return registries;
    }

}
