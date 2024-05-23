/*
 * Copyleft (c) 2024, Alexandre Beaurain
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombase.core.debian.domain;

import com.philips.research.bombase.core.meta.PackageMetadata;
import com.philips.research.bombase.core.meta.registry.Field;
import com.philips.research.bombase.core.meta.registry.Trust;
import pl.tlinkowski.annotation.basic.NullOr;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DebianAPI {
    @GET("{distro}/series")
    Call<SeriesCollection> series(
        @Path("distro") String distro
    );

    @GET("{distro}/+archive/primary?exact_match=true&ws.op=getPublishedBinaries")
    Call<SourcePackageCollection> getSourcePackages(
        @Path("distro") String distro,
        @Query("distro_arch_series") String distro_arch_series,
        @Query("binary_name") String binary_name
    );

    @GET("{distro}/{series}/+source/{source}")
    Call<Source> getSource(@Path("distro") String distro, @Path("series") String series, @Path("source") String source);

    @GET("{project}")
    Call<ResponseJson> getProject(@Path("project") String project);

    @SuppressWarnings("NotNullFieldNotInitialized")
    class Series {
        URI self_link;
        @NullOr URI web_link;
        @NullOr URI resource_type_link;
        @NullOr String bug_reporting_guidelines;
        @NullOr String bug_reported_acknowledgement;
        @NullOr List<String> official_bug_tags;
        @NullOr URI active_milestones_collection_link;
        @NullOr URI all_milestones_collection_link;
        @NullOr boolean active;
        @NullOr String summary;
        @NullOr URI drivers_collection_link;
        String name;
        @NullOr String displayname;
        @NullOr String fullseriesname;
        @NullOr String title;
        @NullOr String description;
        @NullOr String version;
        @NullOr URI distribution_link;
        @NullOr List<String> component_names;
        @NullOr List<String> suite_names;
        @NullOr String status;
        @NullOr Date datereleased;
        @NullOr URI parent_series_link;
        @NullOr URI registrant_link;
        @NullOr URI owner_link;
        @NullOr Date date_created;
        @NullOr URI driver_link;
        @NullOr String changeslist;
        @NullOr URI nominatedarchindep_link;
        @NullOr boolean language_pack_full_export_requested;
        @NullOr boolean backports_not_automatic;
        @NullOr boolean proposed_not_automatic;
        @NullOr boolean include_long_descriptions;
        @NullOr List<String> index_compressors;
        @NullOr boolean publish_by_hash;
        @NullOr boolean advertise_by_hash;
        @NullOr boolean publish_i18n_index;
        @NullOr URI main_archive_link;
        @NullOr boolean supported;
        @NullOr URI architectures_collection_link;
        @NullOr String http_etag;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class SeriesCollection {
        int start;
        int total_size;
        List<Series> entries;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class SourcePackageEntry {
        URI self_link;
        @NullOr URI resource_type_link;
        @NullOr String display_name;
        @NullOr String component_name;
        @NullOr String section_name;
        String source_package_name;
        String source_package_version;
        @NullOr URI distro_arch_series_link;
        @NullOr String phased_update_percentage;
        @NullOr Date date_published;
        @NullOr Date scheduled_deletion_date;
        @NullOr String status;
        @NullOr String pocket;
        @NullOr URI creator_link;
        @NullOr Date date_created;
        @NullOr Date date_superseded;
        @NullOr Date date_made_pending;
        @NullOr Date date_removed;
        @NullOr URI archive_link;
        @NullOr URI copied_from_archive_link;
        @NullOr URI removed_by_link;
        @NullOr String removal_comment;
        String binary_package_name;
        String binary_package_version;
        @NullOr URI build_link;
        @NullOr boolean architecture_specific;
        @NullOr String priority_name;
        @NullOr String http_etag;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class SourcePackageCollection {
        int start;
        int total_size;
        List<SourcePackageEntry> entries;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class Source {
        URI self_link;
        @NullOr URI web_link;
        @NullOr URI resource_type_link;
        @NullOr String bug_reporting_guidelines;
        @NullOr String bug_reported_acknowledgement;
        @NullOr List<String> official_bug_tags;
        String name;
        @NullOr String displayname;
        @NullOr URI distribution_link;
        @NullOr URI distroseries_link;
        URI productseries_link;
        @NullOr String latest_published_component_name;
        @NullOr String http_etag;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    class ResponseJson implements PackageMetadata {
        URI self_link;
        @NullOr URI web_link;
        @NullOr URI resource_type_link;
        @NullOr boolean official_answers;
        @NullOr boolean official_blueprints;
        @NullOr boolean official_codehosting;
        @NullOr boolean official_bugs;
        @NullOr String information_type;
        @NullOr boolean active;
        @NullOr String bug_reporting_guidelines;
        @NullOr String bug_reported_acknowledgement;
        @NullOr List<String> official_bug_tags;
        @NullOr URI recipes_collection_link;
        @NullOr URI webhooks_collection_link;
        @NullOr URI bug_supervisor_link;
        @NullOr URI active_milestones_collection_link;
        @NullOr URI all_milestones_collection_link;
        @NullOr boolean qualifies_for_free_hosting;
        @NullOr String reviewer_whiteboard;
        @NullOr String is_permitted;
        @NullOr String project_reviewed;
        @NullOr String license_approved;
        @NullOr String display_name;
        @NullOr URI icon_link;
        @NullOr URI logo_link;
        String name;
        @NullOr URI owner_link;
        @NullOr URI project_group_link;
        @NullOr String title;
        @NullOr URI registrant_link;
        @NullOr URI driver_link;
        @NullOr String summary;
        @NullOr String description;
        @NullOr Date date_created;
        @NullOr URI homepage_url;
        @NullOr URI wiki_url;
        @NullOr URI screenshots_url;
        @NullOr URI download_url;
        @NullOr String programming_language;
        @NullOr String sourceforge_project;
        @NullOr String freshmeat_project;
        @NullOr URI brand_link;
        @NullOr boolean private_bugs;
        List<String> licenses;
        @NullOr String license_info;
        @NullOr URI bug_tracker_link;
        @NullOr String date_next_suggest_packaging;
        @NullOr URI series_collection_link;
        @NullOr URI development_focus_link;
        @NullOr URI releases_collection_link;
        @NullOr URI translation_focus_link;
        @NullOr URI commercial_subscription_link;
        @NullOr boolean commercial_subscription_is_due;
        @NullOr String remote_product;
        @NullOr String security_contact;
        @NullOr String vcs;
        @NullOr String http_etag;

        @Override
        public Trust trust(Field field) {
            return Trust.LIKELY;
        }

        @Override
        public Optional<String> getTitle() {
            return Optional.ofNullable(name);
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public Optional<List<String>> getAuthors() {
            return Optional.of(new ArrayList<String>());
        }

        @Override
        public Optional<URI> getHomepage() {
            return Optional.ofNullable(homepage_url);
        }

        @Override
        public Optional<String> getDeclaredLicense() {
            if (licenses == null || licenses.size() == 0) {
                return null;
            } 
            return Optional.ofNullable(licenses.get(0).toString());
        }

        @Override
        public Optional<String> getSourceLocation() {
            return Optional.ofNullable(download_url != null ? download_url.toString() : null);
        }

        @Override
        public Optional<URI> getDownloadLocation() {
            return Optional.ofNullable(download_url);
        }
    }
}
