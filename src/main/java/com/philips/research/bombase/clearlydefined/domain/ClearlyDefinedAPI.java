/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.clearlydefined.domain;

import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.net.URI;
import java.util.*;

public interface ClearlyDefinedAPI {
    @GET("definitions/{type}/{provider}/{namespace}/{name}/{revision}")
    Call<ResponseJson> getDefinition(@Path("type") String type, @Path("provider") String provider, @Path("namespace") String namespace,
                                     @Path("name") String name, @Path("revision") String revision);

    @SuppressWarnings("NotNullFieldNotInitialized")
    class ResponseJson {
        DescribedJson described;
        LicensedJson licensed;
    }

    class DescribedJson {
        @NullOr SourceLocationJson sourceLocation;
        Map<String, URI> urls = new HashMap<>();
        Map<String, String> hashes = new HashMap<>();
        @NullOr URI projectWebsite;

        public Optional<URI> getSourceLocation() {
            return Optional.ofNullable((sourceLocation != null && sourceLocation.url != null) ? sourceLocation.url : null);
        }
    }

    class SourceLocationJson {
        @NullOr URI url;
    }

    class LicensedJson {
        @SuppressWarnings("NotNullFieldNotInitialized")
        String declared;
        Map<String, FacetJson> facets = new HashMap<>();
    }

    class FacetJson {
       @NullOr AttributionJson attribution;
        @NullOr DiscoveredJson discovered;
    }

    class AttributionJson {
        List<String> parties = new ArrayList<>();
    }

    class DiscoveredJson {
        List<String> expressions = new ArrayList<>();
    }

}
