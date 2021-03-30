/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.controller;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.philips.research.bombase.core.MetaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/packages")
public class PackagesRoute {
    private final MetaService service;

    PackagesRoute(MetaService service) {
        this.service = service;
    }

    @GetMapping("{purl}")
    PackageJson getPackage(@PathVariable String purl) {
        final var pkgUrl = packageUrl(purl);
        final var attributes = service.getAttributes(pkgUrl);
        if (attributes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No metadata (yet)");
        }
        return new PackageJson(pkgUrl, attributes);
    }

    @GetMapping()
    ResultJson<PackageJson> findPackages(@RequestParam(required = false) @NullOr String type,
                                         @RequestParam(required = false) @NullOr String ns,
                                         @RequestParam(required = false) @NullOr String name,
                                         @RequestParam(required = false) @NullOr String version) {
        final var found = (type == null && ns == null && name == null && version == null)
                ? service.latestScans()
                : service.search(orEmpty(type), orEmpty(ns), orEmpty(name), orEmpty(version));
        return new ResultJson<>(PackageJson.fromDtoList(found));
    }

    private String orEmpty(@NullOr String string) {
        return (string != null) ? string : "";
    }

    private PackageURL packageUrl(String purl) {
        try {
            return new PackageURL(URLDecoder.decode(purl, StandardCharsets.UTF_8));
        } catch (MalformedPackageURLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Package URL parameter: " + purl);
        }
    }
}
