/*
 * Copyright 2015 Jason Fehr
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.maven.report;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.pitest.maven.report.generator.ReportGenerationContext;
import org.pitest.maven.report.generator.ReportGenerationManager;
import org.pitest.util.PitError;

/**
 * Generates a report of the pit mutation testing.
 */
@Mojo(name = "report", defaultPhase = LifecyclePhase.SITE)
public class PitReportMojo extends AbstractMavenReport {

  @Component
  private Renderer                siteRenderer;

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject            project;

  /**
   * When set to true, indicates that generation of the site report should be
   * skipped.
   */
  @Parameter(property = "pit.report.skip", defaultValue = "false")
  private boolean                 skip;

  /**
   * Base directory where all pit reports are written to by the mutationCoverage
   * goal. If timestampedReports is true (the default), then the actual reports
   * will be contained in a subdirectory within this directory. If
   * timestampedReports is false, the actual reports will be in this directory.
   */
  @Parameter(property = "reportsDirectory", defaultValue = "${project.build.directory}/pit-reports")
  private File                    reportsDirectory;

  /**
   * Determines what type of data file should be used to generate the report.
   * Currently, only "HTML" is supported which does nothing more than copy the
   * HTML report generated by the mutationCoverage goal into the site directory.
   * However, the plan is to accept any source format that can be specified in
   * the outputFormats parameter of the mutationCoverage goal.
   */
  @Parameter(property = "pit.report.sourceDataFormats", defaultValue = "HTML")
  private List<String>            sourceDataFormats;

  /**
   * Determines the name of the report that is displayed under the site's
   * Project Reports section
   */
  @Parameter(property = "pit.report.name", defaultValue = "PIT Test Report")
  private String                  siteReportName;

  /**
   * Determines the description of the report that is displayed under the
   * description of the site's Project Reports section
   */
  @Parameter(property = "pit.report.description", defaultValue = "Report of the pit test coverage")
  private String                  siteReportDescription;

  /**
   * Specifies the directory under ${project.reporting.outputDirectory} where
   * the pit reports will be written. The value of this parameter will be
   * relative to ${project.reporting.outputDirectory}. For example, if this
   * parameter is set to "foo", then the pit reports will be located in
   * ${project.reporting.outputDirectory}/foo.
   * 
   */
  @Parameter(property = "pit.report.outputdir", defaultValue = "pit-reports")
  private String                  siteReportDirectory;

  @Parameter(property = "pit.sourceEncoding", defaultValue = "${project.build.sourceEncoding}")
  private String sourceEncoding;

  @Parameter(property = "pit.outputEncoding", defaultValue = "${project.reporting.outputEncoding}")
  private String outputEncoding;

  private ReportGenerationManager reportGenerationManager;

  public PitReportMojo() {
    super();

    this.reportGenerationManager = new ReportGenerationManager();
  }

  @Override
  public String getOutputName() {
    return this.siteReportDirectory + File.separator + "index";
  }

  @Override
  public String getName(Locale locale) {
    return this.siteReportName;
  }

  @Override
  public String getDescription(Locale locale) {
    return this.siteReportDescription;
  }

  @Override
  protected Renderer getSiteRenderer() {
    return this.siteRenderer;
  }

  @Override
  protected String getOutputDirectory() {
    return this.reportsDirectory.getAbsolutePath();
  }

  @Override
  protected MavenProject getProject() {
    return this.project;
  }

  @Override
  protected void executeReport(Locale locale) throws MavenReportException {
    this.getLog().debug("PitReportMojo - starting");

    if (!this.reportsDirectory.exists()) {
      throw new PitError("could not find reports directory ["
          + this.reportsDirectory + "]");
    }

    if (!this.reportsDirectory.canRead()) {
      throw new PitError("reports directory [" + this.reportsDirectory
          + "] not readable");
    }

    if (!this.reportsDirectory.isDirectory()) {
      throw new PitError("reports directory [" + this.reportsDirectory
          + "] is actually a file, it must be a directory");
    }

    this.reportGenerationManager.generateSiteReport(this
        .buildReportGenerationContext(locale));

    this.getLog().debug("PitReportMojo - ending");
  }

  @Override
  public boolean canGenerateReport() {
    return !skip;
  }

  @Override
  public boolean isExternalReport() {
    return true;
  }

  public boolean isSkip() {
    return skip;
  }

  public File getReportsDirectory() {
    return reportsDirectory;
  }

  public List<String> getSourceDataFormats() {
    return this.sourceDataFormats;
  }

  public Charset getSourceEncoding() {
    if (sourceEncoding != null) {
      return Charset.forName(sourceEncoding);
    }
    return Charset.defaultCharset();
  }

  public Charset getOutputEncoding() {
    if (outputEncoding != null) {
      return Charset.forName(outputEncoding);
    }
    return Charset.defaultCharset();
  }

  private ReportGenerationContext buildReportGenerationContext(Locale locale) {
    return new ReportGenerationContext(locale, this.getSink(),
        reportsDirectory, new File(this.getReportOutputDirectory()
            .getAbsolutePath() + File.separator + this.siteReportDirectory),
        this.getLog(), this.getSourceDataFormats());
  }

}
