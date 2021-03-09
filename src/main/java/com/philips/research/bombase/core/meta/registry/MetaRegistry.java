/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.meta.registry;

import com.philips.research.bombase.PackageUrl;
import com.philips.research.bombase.core.UnknownPackageException;
import com.philips.research.bombase.core.meta.MetaStore;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class MetaRegistry {
    private final MetaStore store;
    private final QueuedTaskRunner runner;
    private final Set<PackageListener> listeners = new HashSet<>();

    public MetaRegistry(MetaStore store, QueuedTaskRunner runner) {
        this.store = store;
        this.runner = runner;
    }

    /**
     * Registers an observer for metadata value changes.
     *
     * @param listener observer
     */
    public void addListener(PackageListener listener) {
        listeners.add(listener);
    }

    public void edit(PackageUrl purl, Consumer<PackageModifier> consumer) {
        final var pkg = getOrCreatePackage(purl);
        final var modifier = new PackageModifier(pkg);
        consumer.accept(modifier);
        notifyValueListeners(pkg, modifier.getModifiedFields(), valuesOf(pkg));
    }

    private Package getOrCreatePackage(PackageUrl purl) {
        return store.findPackage(purl).orElseGet(() -> store.createPackage(purl));
    }

    private Package validPackage(PackageUrl purl) {
        return store.findPackage(purl)
                .orElseThrow(() -> new UnknownPackageException(purl));
    }

    private Map<Field, Object> valuesOf(Package pkg) {
        return pkg.getAttributes()
                .filter(attr -> attr.getValue().isPresent())
                .collect(Collectors.toMap(Attribute::getField, attr -> attr.getValue().get()));
    }

    private void notifyValueListeners(Package pkg, Set<Field> fields, Map<Field, ?> values) {
        listeners.forEach(l -> l.onUpdated(pkg.getPurl(), fields, values)
                .ifPresent(task -> runner.execute(pkg.getPurl(), task)));
    }

    /**
     * Callbacks to optionally create an asynchronous task.
     */
    public interface PackageListener {
        /**
         * Notifies given fields were updated.
         *
         * @param purl    package id
         * @param updated modified fields
         * @param values  current package metadata
         * @return (optional) operation to queue for execution
         */
        Optional<Consumer<PackageModifier>> onUpdated(PackageUrl purl, Set<Field> updated, Map<Field, ?> values);
    }
}
