/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.clearlydefined.domain;

import com.philips.research.bombase.PackageUrl;
import com.philips.research.bombase.clearlydefined.ClearlyDefinedService;
import com.philips.research.bombase.meta.Field;
import com.philips.research.bombase.meta.MetaService;
import com.philips.research.bombase.meta.Origin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClearlyDefinedInteractorTest {
    private static final String TYPE = "type";
    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final PackageUrl PURL = new PackageUrl(String.format("pkg:%s/%s/%s@%s", TYPE, NAMESPACE, NAME, VERSION));
    private static final URI HOMEPAGE = URI.create("https://example.com/home");
    private static final URI DOWNLOAD_LOCATION = URI.create("https://example.com/download");
    private static final URI SOURCE_LOCATION = URI.create("git+https://github.com/source");
    private static final List<String> ATTRIBUTION = List.of("Attribution");
    private static final String DECLARED_LICENSE = "Declared";
    private static final String DETECTED_LICENSE = "Detected";
    private static final String SHA1 = "Sha1";
    private static final String SHA256 = "Sha256";

    private final MetaService metaService = mock(MetaService.class);
    private final ClearlyDefinedClient client = mock(ClearlyDefinedClient.class);
    private final ClearlyDefinedService interactor = new ClearlyDefinedInteractor(metaService, client);

    @Nested
    class MetadataUpdate {
        private MetaService.PackageListener listener;

        @BeforeEach
        void beforeEach() {
            doAnswer(p -> listener = p.getArgument(1)).when(metaService).addListener(any(), any());
            interactor.init();
        }

        @Test
        void registersWithMetaService() {
            verify(metaService).addListener(eq(Origin.CLEARLY_DEFINED), any());
            assertThat(listener).isNotNull();
        }

        @Test
        void harvestsNewPackage() {
            final var task = listener.onUpdated(PURL, Set.of(), Map.of()).orElseThrow();
            task.run();

            verify(client).getPackageDefinition(TYPE, TYPE, NAMESPACE, NAME, VERSION);
        }

        @Test
        void updatesHarvestedMetadata() {
            final var meta = new PackageMetadata()
                    .setHomePage(HOMEPAGE)
                    .setAttribution(ATTRIBUTION)
                    .setDownloadLocation(DOWNLOAD_LOCATION)
                    .setSourceLocation(SOURCE_LOCATION)
                    .setDeclaredLicense(DECLARED_LICENSE)
                    .setDetectedLicense(DETECTED_LICENSE)
                    .setSha1(SHA1)
                    .setSha256(SHA256);
            when(client.getPackageDefinition(TYPE, TYPE, NAMESPACE, NAME, VERSION)).thenReturn(Optional.of(meta));

            final var task = listener.onUpdated(PURL, Set.of(), Map.of()).orElseThrow();
            task.run();

            verify(metaService).update(Origin.CLEARLY_DEFINED, PURL, Map.of(
                    Field.HOME_PAGE, HOMEPAGE,
                    Field.ATTRIBUTION, ATTRIBUTION,
                    Field.DOWNLOAD_LOCATION, DOWNLOAD_LOCATION,
                    Field.SOURCE_LOCATION, SOURCE_LOCATION,
                    Field.DECLARED_LICENSE, DECLARED_LICENSE,
                    Field.DETECTED_LICENSE, DETECTED_LICENSE,
                    Field.SHA1, SHA1,
                    Field.SHA256, SHA256
            ));
        }

        @Test
        void skipsUnavailableMetadata() {
            final var meta = new PackageMetadata();
            when(client.getPackageDefinition(TYPE, TYPE, NAMESPACE, NAME, VERSION)).thenReturn(Optional.of(meta));

            final var task = listener.onUpdated(PURL, Set.of(), Map.of()).orElseThrow();
            task.run();

            verify(metaService).update(Origin.CLEARLY_DEFINED, PURL, Map.of());
        }
    }
}
