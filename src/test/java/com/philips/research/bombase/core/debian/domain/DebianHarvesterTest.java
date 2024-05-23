/*
 * Copyleft (c) 2024, Alexandre Beaurain
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.debian.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DebianHarvesterTest {
    private final DebianClient client = mock(DebianClient.class);
    private final DebianHarvester harvester = new DebianHarvester(client);

    @Test
    void triggersForSupportedType() {
        assertThat(harvester.isSupportedType("generic")).isFalse();
        assertThat(harvester.isSupportedType("deb")).isTrue();
    }
}
