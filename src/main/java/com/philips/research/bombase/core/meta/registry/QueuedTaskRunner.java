/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.meta.registry;

import com.philips.research.bombase.PackageUrl;
import com.philips.research.bombase.core.meta.MetaStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class QueuedTaskRunner {
    private final MetaStore store;

    public QueuedTaskRunner(MetaStore store) {
        this.store = store;
    }

    /**
     * Queues tasks to run up to a configured maximum in parallel.
     *
     * @param task execution unit
     */
    @Async("taskExecutor")
    public
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    void execute(PackageUrl purl, Consumer<PackageModifier> task) {
        store.findPackage(purl).ifPresent(pkg -> task.accept(new PackageModifier(pkg)));
    }
}
