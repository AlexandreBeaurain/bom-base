/*
 * Copyleft (c) 2024, Alexandre Beaurain
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.debian.domain;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.philips.research.bombase.core.meta.PackageMetadata;
import com.philips.research.bombase.core.debian.DebianException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import pl.tlinkowski.annotation.basic.NullOr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebianClientTest {
    private static final int PORT = 1084;
    private static final PackageURL PURL = createPurl("pkg:deb/debian/package-name@version");
    private static final String HOMEPAGE_URL = "https://example.com/home-page";
    private static final String DOWNLOAD_URL = "https://example.com/download";
    private static final String API = "https://api.launchpad.net/1.0/";
    
    private static final String DESCRIPTION = "Description";
    private static final String HTTP_ETAG = "\"ac6176d8d018519021e077f08a9c39fb2fa6257e-4405102f75bbae89c324c528d4e205ebf4bae82b\"";
    private static final String LICENSE = "MIT";

    private static final String PROJECT_NAME = "project-name";
    private static final String SERIE_NAME = "noble";
    private static final String PACKAGE_NAME = "package-name";
    private static final String SOURCE_NAME = "source-name";
    private static final String VERSION = "version";

    private final DebianClient client = new DebianClient(URI.create("http://localhost:" + PORT));
    private final MockWebServer mockServer = new MockWebServer();

    static PackageURL createPurl(String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        mockServer.start(PORT);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    void enqueueSeriesMock() throws JSONException {
        List<JSONObject> entries = new ArrayList<JSONObject>();
        entries.add(new JSONObject()
            .put("self_link", API + "ubuntu/noble")
            .put("datereleased", "2024-04-25T16:04:56.109664+00:00")
            .put("active", true)
            .put("name", SERIE_NAME));
        mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                .put("start", 0)
                .put("total_size", 1)
                .put("entries", new JSONArray(entries))
                .toString()));
    }

    void enqueueSourcePackagesMock() throws JSONException {
        List<JSONObject> entries = new ArrayList<JSONObject>();
        entries.add(new JSONObject()
            .put("self_link", API + "ubuntu/+archive/primary/+binarypub/200881569")
            .put("source_package_name", PACKAGE_NAME)
            .put("source_package_version", VERSION)
            .put("binary_package_name", SOURCE_NAME)
            .put("binary_package_version", VERSION));
        mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                .put("start", 0)
                .put("total_size", 1)
                .put("entries", new JSONArray(entries))
                .toString()));
    }

    void enqueueSourceMock() throws JSONException {
        mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                .put("self_link", API + "ubuntu/" + SERIE_NAME + "/+source/" + SOURCE_NAME)
                .put("name", SOURCE_NAME)
                .put("productseries_link", API + PROJECT_NAME + "/head")
                .toString()));
    }

    void enqueueProjectMock() throws JSONException {
        List<String> licenses = new ArrayList<String>();
        licenses.add(LICENSE);
        mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                .put("self_link", "")
                .put("name", PROJECT_NAME)
                .put("description", DESCRIPTION)
                .put("homepage_url", HOMEPAGE_URL)
                .put("download_url", DOWNLOAD_URL)
                .put("licenses", new JSONArray(licenses))
                .put("http_etag", HTTP_ETAG)
                .toString()));
    }

    @Nested
    class ApiNotWorkingProperly {

        @Test
        void noSeriesAnswer() throws JSONException {
            mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("start", 0)
                    .put("total_size", 0)
                    .put("entries", new JSONArray(new ArrayList<JSONObject>()))
                    .toString()));
            assertThat(client.getPackageMetadata(PURL)).isEmpty();
        }

        @Test
        void noSourcePackagesAnswer() throws Exception {
            enqueueSeriesMock();
            mockServer.enqueue(new MockResponse().setBody(new JSONObject()
                    .put("start", 0)
                    .put("total_size", 0)
                    .put("entries", new JSONArray(new ArrayList<JSONObject>()))
                    .toString()));
            assertThat(client.getPackageMetadata(PURL)).isEmpty();
        }

        @Test
        void noSourceAnwser() throws Exception {
            enqueueSeriesMock();
            enqueueSourcePackagesMock();
            mockServer.enqueue(new MockResponse().setResponseCode(404).setBody("Object: <DistroSeries '" + SERIE_NAME + "'>, name: '" + SOURCE_NAME + "'"));
            assertThat(client.getPackageMetadata(PURL)).isEmpty();
        }

        @Test
        void noProjectAnwser() throws Exception {
            enqueueSeriesMock();
            enqueueSourcePackagesMock();
            enqueueSourceMock();
            mockServer.enqueue(new MockResponse().setResponseCode(404).setBody("Object: <lp.systemhomes.WebServiceApplication object at 0x7f7cc57a7370>, name: '" + PROJECT_NAME + "'"));
            assertThat(client.getPackageMetadata(PURL)).isEmpty();
        }

        @Test
        void throws_serverNotReachable() {
            var serverlessClient = new DebianClient(URI.create("http://localhost:1234"));
            assertThatThrownBy(() -> serverlessClient.getPackageMetadata(PURL))
                    .isInstanceOf(DebianException.class)
                    .hasMessageContaining("not reachable");
        }
    }

    @Nested
    class WithMetaData {

        @BeforeEach
        void setUp() throws JSONException {
            enqueueSeriesMock();
            enqueueSourcePackagesMock();
            enqueueSourceMock();
            enqueueProjectMock();
        }

        @Test
        void getsInitialMetaDataFromServer() throws Exception {
            final var definition = client.getPackageMetadata(PURL).orElseThrow();

            assertThat(definition.getTitle()).contains(PROJECT_NAME);
            assertThat(definition.getDescription()).contains(DESCRIPTION);
            assertThat(definition.getHomepage()).contains(URI.create(HOMEPAGE_URL));
            assertThat(definition.getSourceLocation()).contains(DOWNLOAD_URL);
            assertThat(definition.getDeclaredLicense()).contains(LICENSE);
            assertThat(definition.getDownloadLocation()).contains(URI.create(DOWNLOAD_URL));
        }
    }
}
