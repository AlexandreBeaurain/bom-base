/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

class Package {
  Package({required this.id, Uri? purl, this.updated})
      : purl = purl ?? Uri.parse(Uri.decodeFull(Uri.decodeFull(id)));

  final String id;
  final Uri purl;
  final DateTime? updated;
  final Map<String, dynamic> attributes = {};

  String get title => attributes['title'] ?? '(Untitled)';

  String get description => attributes['description'] ?? '';

  Iterable<String> get authors =>
      (attributes['attribution'] as List<dynamic>? ?? [])
          .map((value) => value as String);

  String get declaredLicense =>
      attributes['declared_license'] ?? '(Unlicensed)';

  Iterable<String> get detectedLicenses =>
      (attributes['detected_licenses'] as List<dynamic>? ?? [])
          .map((lic) => lic as String)
          .where((lic) => lic.isNotEmpty);
}
