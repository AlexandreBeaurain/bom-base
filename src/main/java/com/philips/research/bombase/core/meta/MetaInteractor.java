/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.meta;

import com.github.packageurl.PackageURL;
import com.philips.research.bombase.core.MetaService;
import com.philips.research.bombase.core.clearlydefined.domain.ClearlyDefinedListener;
import com.philips.research.bombase.core.meta.registry.Field;
import com.philips.research.bombase.core.meta.registry.MetaRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import pl.tlinkowski.annotation.basic.NullOr;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetaInteractor implements MetaService {
    private final MetaRegistry registry;
    private final MetaStore store;
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ApplicationContext context;

    public MetaInteractor(MetaRegistry registry, MetaStore store) {
        this.registry = registry;
        this.store = store;
    }

    @PostConstruct
    void init() {
        registry.addListener(context.getBean(ClearlyDefinedListener.class));
    }

    @Override
    public Map<String, Object> getAttributes(PackageURL purl) {
        return store.findPackage(purl)
                .map(pkg -> pkg.getAttributes()
                        .filter(a -> a.getValue().isPresent())
                        .collect(Collectors.toMap(a -> a.getField().name().toLowerCase(), a -> a.getValue().get())))
                .orElse(Map.of());
    }

    @Override
    public void setAttributes(PackageURL purl, Map<String, @NullOr Object> values) {
        registry.edit(purl, pkg -> values.forEach((key, value) -> {
            final var field = Field.valueOf(Field.class, key.toUpperCase());
            pkg.update(field, 100, value);
        }));
    }
}
