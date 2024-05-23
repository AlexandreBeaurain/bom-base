/*
 * Copyleft (c) 2024, Alexandre Beaurain
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.debian.domain;

import com.philips.research.bombase.core.meta.AbstractRepoHarvester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DebianHarvester extends AbstractRepoHarvester {
    @Autowired
    DebianHarvester(DebianClient client) {
        super(client::getPackageMetadata);
    }

    @Override
    protected boolean isSupportedType(String type) {
        return type.equals("deb");
    }
}
