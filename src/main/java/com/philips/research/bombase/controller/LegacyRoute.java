/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.controller;

import com.philips.research.bombase.meta.MetaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping()
public class LegacyRoute {
    private final MetaService service;

    public LegacyRoute(MetaService service) {
        this.service = service;
    }

    @PostMapping("/packages")
    LicenseJson getLicense(@RequestBody RequestJson body) {
        service.update(body.purl, Map.of());
        return new LicenseJson();
    }

    static class RequestJson {
        URI purl;
    }

    static class LicenseJson {
        String id;
        String license;
    }
}
