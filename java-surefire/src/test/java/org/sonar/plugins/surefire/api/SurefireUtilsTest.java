/*
 * SonarQube Java
 * Copyright (C) 2012-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.surefire.api;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SurefireUtilsTest {

  @Rule
  public LogTester logTester = new LogTester();

  @Test
  public void should_get_report_paths_from_property() {
    Settings settings = new MapSettings();
    settings.setProperty("sonar.junit.reportPaths", "target/surefire,submodule/target/surefire");

    DefaultFileSystem fs = new DefaultFileSystem(new File("src/test/resources/org/sonar/plugins/surefire/api/SurefireUtilsTest/shouldGetReportsPathFromProperty"));
    PathResolver pathResolver = new PathResolver();

    assertThat(logTester.logs(LoggerLevel.INFO)).isEmpty();

    List<File> directories = SurefireUtils.getReportsDirectories(settings, fs, pathResolver);

    assertThat(directories).hasSize(2);
    File directory1 = directories.get(0);
    assertThat(directory1.exists()).isTrue();
    assertThat(directory1.isDirectory()).isTrue();
    File directory2 = directories.get(1);
    assertThat(directory2.exists()).isTrue();
    assertThat(directory2.isDirectory()).isTrue();
    assertThat(logTester.logs(LoggerLevel.INFO)).isEmpty();
  }

  @Test
  public void should_get_reports_path_from_deprecated_property() {
    Settings settings = new MapSettings();
    settings.setProperty("sonar.junit.reportsPath", "target/surefire");

    DefaultFileSystem fs = new DefaultFileSystem(new File("src/test/resources/org/sonar/plugins/surefire/api/SurefireUtilsTest/shouldGetReportsPathFromDeprecatedProperty"));
    PathResolver pathResolver = new PathResolver();

    assertThat(logTester.logs(LoggerLevel.INFO)).isEmpty();

    List<File> directories = SurefireUtils.getReportsDirectories(settings, fs, pathResolver);

    assertThat(directories).hasSize(1);
    File directory = directories.get(0);
    assertThat(directory.exists()).isTrue();
    assertThat(directory.isDirectory()).isTrue();
    assertThat(logTester.logs(LoggerLevel.INFO)).contains("Property 'sonar.junit.reportsPath' is deprecated. Use property 'sonar.junit.reportPaths' instead.");
  }

  @Test
  public void should_only_use_new_property_if_both_set() {
    Settings settings = new MapSettings();
    settings.setProperty("sonar.junit.reportsPath", "../shouldGetReportsPathFromDeprecatedProperty/target/surefire");
    settings.setProperty("sonar.junit.reportPaths", "target/surefire,submodule/target/surefire");

    DefaultFileSystem fs = new DefaultFileSystem(new File("src/test/resources/org/sonar/plugins/surefire/api/SurefireUtilsTest/shouldGetReportsPathFromProperty"));
    PathResolver pathResolver = new PathResolver();

    assertThat(logTester.logs(LoggerLevel.INFO)).isEmpty();

    List<File> directories = SurefireUtils.getReportsDirectories(settings, fs, pathResolver);

    assertThat(directories).hasSize(2);
    File directory1 = directories.get(0);
    assertThat(directory1.exists()).isTrue();
    assertThat(directory1.isDirectory()).isTrue();
    File directory2 = directories.get(1);
    assertThat(directory2.exists()).isTrue();
    assertThat(directory2.isDirectory()).isTrue();
    assertThat(logTester.logs(LoggerLevel.INFO))
      .contains("Property 'sonar.junit.reportsPath' is deprecated and will be ignored, as property 'sonar.junit.reportPaths' is also set.");
  }

  @Test
  public void return_default_value_if_property_unset() throws Exception {
    Settings settings = mock(Settings.class);
    DefaultFileSystem fs = new DefaultFileSystem(new File("src/test/resources/org/sonar/plugins/surefire/api/SurefireUtilsTest"));
    PathResolver pathResolver = new PathResolver();

    assertThat(logTester.logs(LoggerLevel.INFO)).isEmpty();

    List<File> directories = SurefireUtils.getReportsDirectories(settings, fs, pathResolver);

    assertThat(directories).hasSize(1);
    File directory = directories.get(0);
    assertThat(directory.getCanonicalPath()).endsWith("target"+File.separator+"surefire-reports");
    assertThat(directory.exists()).isFalse();
    assertThat(directory.isDirectory()).isFalse();
    assertThat(logTester.logs(LoggerLevel.INFO)).isEmpty();
  }
}