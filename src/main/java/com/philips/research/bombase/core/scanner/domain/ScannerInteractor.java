/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.scanner.domain;

import com.philips.research.bombase.core.downloader.DownloadService;
import com.philips.research.bombase.core.scanner.ScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScannerInteractor implements ScannerService {
    private final DownloadService downloader;
    private final ScanCodeScanner scanner;

    @Autowired
    public ScannerInteractor(DownloadService downloader) {
        this(downloader, new ScanCodeScanner());
    }

    ScannerInteractor(DownloadService downloader, ScanCodeScanner scanner) {
        this.downloader = downloader;
        this.scanner = scanner;
    }

    @Override
    public ScanResult scan(Path directory) {
        return scanner.scan(directory);
    }

    @Override
    public List<String> scanLicenses(URI uri) {
        return downloader.download(uri, scanner::scan)
                .getLicenses().stream()
                .map(LicenseResult::getExpression)
                .collect(Collectors.toList());
    }
}
