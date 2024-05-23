/*
 * Copyleft (c) 2024, Alexandre Beaurain
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.debian.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.packageurl.PackageURL;
import com.philips.research.bombase.core.meta.PackageMetadata;
import com.philips.research.bombase.core.debian.DebianException;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Map;

@Component
public class DebianClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final DebianAPI rest;

    private static final String baseApiUrl = "https://api.launchpad.net/1.0/";

    DebianClient() {
        this(URI.create(baseApiUrl));
    }

    DebianClient(URI uri) {
        final var retrofit = new Retrofit.Builder()
                .baseUrl(uri.toASCIIString())
                .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                .build();
        rest = retrofit.create(DebianAPI.class);
    }

    Optional<PackageMetadata> getPackageMetadata(PackageURL purl) {
        final Map<String, String> qualifiers = purl.getQualifiers();
        final String distro = "ubuntu";
        final String packageName = purl.getName();
        final var allSeries = query(rest.series(distro)).get().entries;
        if (allSeries.size() == 0) {
            return Optional.empty();
        }
        String seriesName = "noble";
        for ( var serie : allSeries ) {
            if (serie.active && serie.datereleased != null) {
                seriesName = serie.name;
                break;
            }
        }
        String arch = qualifiers != null ? qualifiers.getOrDefault("arch", "amd64") : "amd64";
        if (arch.equals("all")) {
            arch = "amd64";
        }
        final String distroArchSeries = baseApiUrl + distro + "/" + seriesName + "/" + arch;
        final var sourcePackagesCollection = query(rest.getSourcePackages(distro, '"' + distroArchSeries + '"', '"' + packageName + '"'));
        final var sourcePackagesEntries = sourcePackagesCollection.get().entries;
        if (sourcePackagesEntries.size() == 0) {
            return Optional.empty();
        }
        final String sourceName = sourcePackagesEntries.get(0).source_package_name;
        final var source = query(rest.getSource(distro, seriesName, sourceName));
        if (source.isEmpty() || source.get() == null || source.get().productseries_link == null) {
            return Optional.empty();
        }
        final String projectName = source.get().productseries_link.toString().replace(baseApiUrl, "").replaceAll("/.*", "");
        return query(rest.getProject(projectName));
    }

    private <T> Optional<T> query(Call<? extends T> query) {
        try {
            final var response = query.execute();
            if (response.code() == 404) {
                return Optional.empty();
            }
            if (!response.isSuccessful()) {
                throw new DebianException("Debian server responded with status " + response.code());
            }
            return Optional.ofNullable(response.body());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON formatting error", e);
        } catch (IOException e) {
            throw new DebianException("Debian is not reachable");
        }
    }
}
